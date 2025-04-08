package com.example.datnsd26.services.impl;

import com.example.datnsd26.controller.request.InvoiceParamRequest;
import com.example.datnsd26.controller.response.InvoiceInformation;
import com.example.datnsd26.controller.response.InvoicePageResponse;
import com.example.datnsd26.exception.EntityNotFound;
import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.models.LichSuHoaDon;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.HoaDonRepository;
import com.example.datnsd26.repository.LichSuHoaDonRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.customizeQuery.InvoiceCustomizeQuery;
import com.example.datnsd26.services.HoaDonService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "HOA-DON-SERVICE")
public class HoaDonServiceImp implements HoaDonService {

    private final InvoiceCustomizeQuery invoiceCustomizeQuery;

    private final HoaDonRepository hoaDonRepository;

    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    private static final String STATUS_CONFIRMED = "Đã xác nhận";
    private static final String STATUS_DELIVERED = "Đã giao cho đơn vị vận chuyển";
    private static final String STATUS_CANCELED = "Đã hủy";
    private static final String STATUS_COMPLETED = "Hoàn thành";


    @Override
    public InvoicePageResponse getInvoices(InvoiceParamRequest request) {
        log.info("GET/hoa-don");
        return invoiceCustomizeQuery.getInvoices(request);
    }

    @Override
    public InvoiceInformation getInvoice(String code) {
        log.info("GET/hoa-don/{}", code);
        HoaDon hd = getHoaDonByCode(code);
        boolean confirm = this.lichSuHoaDonRepository.findByStatusAndInvoice(STATUS_CONFIRMED, hd.getId()).isEmpty();
        boolean delivery = this.lichSuHoaDonRepository.findByStatusAndInvoice(STATUS_DELIVERED, hd.getId()).isEmpty();
        boolean isCancel = this.lichSuHoaDonRepository.findByStatusAndInvoice(STATUS_CANCELED, hd.getId()).isEmpty();
        boolean isCompleted = this.lichSuHoaDonRepository.findByStatusAndInvoice(STATUS_COMPLETED, hd.getId()).isEmpty();
        return InvoiceInformation.builder()
                .isConfirm(!hd.getHinhThucMuaHang().equalsIgnoreCase("offline") && confirm && isCancel)
                .isDelivery(!hd.getHinhThucMuaHang().equalsIgnoreCase("offline") && (!confirm && delivery) && isCancel)
                .allowCancel(delivery && isCancel && isCompleted)
                .isCompleted(!delivery && hd.isThanhToan() && isCompleted && isCancel)
                .seller(hd.getNhanVien() == null ? "N/A" : hd.getNhanVien().getTenNhanVien())
                .order_date(hd.getNgayTao())
                .order_id(hd.getMaHoaDon())
                .note(hd.getGhiChu() == null ? "Không có ghi chú nào" : hd.getGhiChu())
                .status_timeline(hd.getLichSuHoaDon().stream().map(tl -> InvoiceInformation.StatusTimeline.builder()
                        .status(tl.getTrangThai())
                        .time(tl.getThoiGian())
                        .completed(true)
                        .build()).toList())
                .customer(InvoiceInformation.Customer.builder()
                        .name(hd.getTenNguoiNhan() == null ? "Khách lẻ" : hd.getTenNguoiNhan())
                        .phone(hd.getSdtNguoiNhan())
                        .delivery_address(String.format("%s, %s, %s, %s", hd.getDiaChiNguoiNhan(), hd.getXa(), hd.getQuan(), hd.getTinh()))
                        .build())
                .payment(isCancel ? InvoiceInformation.Payment.builder()
                        .total_amount(hd.getTongTien())
                        .paid_amount(hd.isThanhToan() ? hd.getTongTien() : 0)
                        .remaining_amount(hd.getTongTien())
                        .build() : InvoiceInformation.Payment.builder()
                        .total_amount(hd.getTongTien())
                        .paid_amount(hd.getTongTien())
                        .remaining_amount(hd.getTongTien())
                        .build())
                .products(hd.getDanhSachSanPham().stream().map(s -> InvoiceInformation.Product.builder()
                        .id(s.getSanPhamChiTiet().getId())
                        .image("https://th.bing.com/th/id/OIP.8tQmmY_ccVpcxBxu0Z0mzwHaE8?rs=1&pid=ImgDetMain")
                        .code(s.getSanPhamChiTiet().getMaSanPhamChiTiet())
                        .name(String.format("%s [%s - %s]", s.getSanPhamChiTiet().getSanPham().getTenSanPham(), s.getSanPhamChiTiet().getMauSac().getTen(), s.getSanPhamChiTiet().getKichCo().getTen()))
                        .quantity(s.getSoLuong())
                        .unit_price(s.getSanPhamChiTiet().getGiaBanSauGiam())
                        .total_price(s.getSoLuong() * s.getGiaTienSauGiam())
                        .build()).toList())
                .summary(InvoiceInformation.Summary.builder()
                        .subtotal(hd.getTongTien())
                        .shipping_fee(hd.getPhiVanChuyen())
                        .discount(0)
                        .total(hd.getTongTien() + hd.getPhiVanChuyen())
                        .build())
                .build();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void confirmInvoice(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai(STATUS_CONFIRMED);
        hd.setNgayCapNhat(new Date());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai(STATUS_CONFIRMED).hoaDon(hd).build());
        this.hoaDonRepository.save(hd);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void payment(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai(hd.getTrangThai().equalsIgnoreCase("Chờ xác nhận") ? hd.getTrangThai() : STATUS_COMPLETED);
        hd.setThanhToan(true);
        hd.setNgayCapNhat(new Date());
        this.hoaDonRepository.save(hd);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void confirmDelivery(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai(STATUS_DELIVERED);
        hd.setNgayCapNhat(new Date());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai(STATUS_DELIVERED).hoaDon(hd).build());
        this.hoaDonRepository.save(hd);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void cancel(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai(STATUS_CANCELED);
        hd.getDanhSachSanPham().forEach(sp -> {
            SanPhamChiTiet ct = this.sanPhamChiTietRepository.findById(sp.getSanPhamChiTiet().getId()).orElseThrow(() -> new EntityNotFound("Product not found!"));
            ct.setSoLuong(ct.getSoLuong() + sp.getSoLuong());
            this.sanPhamChiTietRepository.save(ct);
        });
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai(STATUS_CANCELED).hoaDon(hd).build());
        this.hoaDonRepository.save(hd);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void edit(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai("Đang xử lý");
        hd.setPhiVanChuyen(0f);
        hd.setHinhThucMuaHang(null);
        this.hoaDonRepository.save(hd);
    }

    @Override
    public void completed(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai(STATUS_COMPLETED);
        hd.setNgayCapNhat(new Date());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai(STATUS_COMPLETED).hoaDon(hd).build());
        this.hoaDonRepository.save(hd);
    }

    private HoaDon getHoaDonByCode(String code) {
        return this.hoaDonRepository.findHoaDonByMaHoaDon(code).orElseThrow(() -> new EntityNotFound("Not found!"));
    }
}