package com.example.datnsd26.services.impl;

import com.example.datnsd26.controller.request.InvoiceParamRequest;
import com.example.datnsd26.controller.response.InvoiceInformation;
import com.example.datnsd26.controller.response.InvoicePageResponse;
import com.example.datnsd26.exception.EntityNotFound;
import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.models.LichSuHoaDon;
import com.example.datnsd26.repository.HoaDonRepository;
import com.example.datnsd26.repository.LichSuHoaDonRepository;
import com.example.datnsd26.repository.customizeQuery.InvoiceCustomizeQuery;
import com.example.datnsd26.services.HoaDonService;
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


    @Override
    public InvoicePageResponse getInvoices(InvoiceParamRequest request) {
        log.info("GET/hoa-don");
        return invoiceCustomizeQuery.getInvoices(request);
    }

    @Override
    public InvoiceInformation getInvoice(String code) {
        log.info("GET/hoa-don/{}", code);
        HoaDon hd = getHoaDonByCode(code);
        List<InvoiceInformation.StatusTimeline> statusTimeline = new ArrayList<>();

        statusTimeline.add(InvoiceInformation.StatusTimeline.builder()
                .status("Đặt hàng")
                .time(new Date())
                .completed(true)
                .build());
        if (hd.getHinhThucMuaHang().equalsIgnoreCase("Offline") && hd.isThanhToan()) {
            statusTimeline.add(InvoiceInformation.StatusTimeline.builder()
                    .status("Hoàn thành")
                    .time(new Date())
                    .completed(true)
                    .build());
        }

        if (hd.getHinhThucMuaHang().equalsIgnoreCase("Có giao hàng")) {
            statusTimeline.add(InvoiceInformation.StatusTimeline.builder()
                    .status("Đã xác nhận")
                    .time(new Date())
                    .completed(true)
                    .build());
            statusTimeline.add(InvoiceInformation.StatusTimeline.builder()
                    .status("Đã giao cho đơn vị vận chuyển")
                    .time(new Date())
                    .completed(true)
                    .build());
            statusTimeline.add(InvoiceInformation.StatusTimeline.builder()
                    .status("Hoàn thành")
                    .time(new Date())
                    .completed(hd.isThanhToan())
                    .build());
        }

        boolean isOk = false;
        if (hd.getHinhThucMuaHang().equalsIgnoreCase("Online")) {
            isOk = hd.getTrangThai().equalsIgnoreCase("Đã giao cho đơn vị vận chuyển") || hd.getTrangThai().equalsIgnoreCase("Hoàn thành");
            statusTimeline.add(InvoiceInformation.StatusTimeline.builder()
                    .status("Chờ xác nhận")
                    .time(new Date())
                    .completed(true)
                    .build());
            statusTimeline.add(InvoiceInformation.StatusTimeline.builder()
                    .status("Đã xác nhận")
                    .time(new Date())
                    .completed(isOk)
                    .build());
            statusTimeline.add(InvoiceInformation.StatusTimeline.builder()
                    .status("Đã giao cho đơn vị vận chuyển")
                    .time(new Date())
                    .completed(isOk)
                    .build());
            statusTimeline.add(InvoiceInformation.StatusTimeline.builder()
                    .status("Hoàn thành")
                    .time(new Date())
                    .completed(isOk && hd.isThanhToan())
                    .build());
        }

        boolean confirm = this.lichSuHoaDonRepository.findByStatusAndInvoice("Đã xác nhận", hd.getId()).isEmpty();
        boolean delivery = this.lichSuHoaDonRepository.findByStatusAndInvoice("Đã giao cho đơn vị vận chuyển", hd.getId()).isEmpty();
        return InvoiceInformation.builder()
                .isConfirm(hd.getHinhThucMuaHang().equalsIgnoreCase("offline") ? false : confirm)
                .isDelivery(hd.getHinhThucMuaHang().equalsIgnoreCase("offline") ? false : (!confirm && delivery))
                .order_id(hd.getMaHoaDon())
                .seller(hd.getNhanVien() == null ? "N/A" : hd.getNhanVien().getTenNhanVien())
                .order_date(hd.getNgayTao())
                .note(hd.getGhiChu() == null ? "Không có ghi chú nào" : hd.getGhiChu())
                .status_timeline(hd.getLichSuHoaDon().stream().map(tl -> InvoiceInformation.StatusTimeline.builder()
                        .status(tl.getTrangThai())
                        .time(tl.getThoiGian())
                        .completed(true)
                        .build()).collect(Collectors.toList()))
                .customer(InvoiceInformation.Customer.builder()
                        .name(hd.getKhachHang() == null ? "Khách lẻ" : hd.getKhachHang().getTenKhachHang())
                        .phone(hd.getSdtNguoiNhan())
                        .delivery_address(String.format("%s, %s, %s, %s", hd.getDiaChiNguoiNhan(), hd.getXa(), hd.getQuan(), hd.getTinh()))
                        .build())
                .payment(InvoiceInformation.Payment.builder()
                        .total_amount(hd.getTongTien())
                        .paid_amount(hd.isThanhToan() ? hd.getTongTien() : 0)
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
                        .build()).collect(Collectors.toList()))
                .summary(InvoiceInformation.Summary.builder()
                        .subtotal(hd.getTongTien())
                        .shipping_fee(hd.getPhiVanChuyen())
                        .discount(0)
                        .total(hd.getTongTien())
                        .build())
                .build();
    }

    @Override
    public void confirmInvoice(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai("Đã xác nhận");
        hd.setNgayCapNhat(new Date());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Đã xác nhận").hoaDon(hd).build());
        this.hoaDonRepository.save(hd);
    }

    @Override
    public void payment(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai(hd.getTrangThai().equalsIgnoreCase("Chờ xác nhận") ? hd.getTrangThai() : "Hoàn thành");
        hd.setThanhToan(true);
        hd.setNgayCapNhat(new Date());
        boolean isExistsInvoice = lichSuHoaDonRepository.findByStatusAndInvoice("Đã giao cho đơn vị vận chuyển", hd.getId()).isPresent();
        if (isExistsInvoice && hd.isThanhToan()) {
            lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Hoàn thành").hoaDon(hd).build());
        }
        this.hoaDonRepository.save(hd);
    }

    @Override
    public void confirmDelivery(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai(hd.isThanhToan() ? "Hoàn thành" : "Đã giao cho đơn vị vận chuyển");
        hd.setNgayCapNhat(new Date());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Đã giao cho đơn vị vận chuyển").hoaDon(hd).build());
        if (hd.isThanhToan()) {
            lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Hoàn thành").hoaDon(hd).build());
        }
        this.hoaDonRepository.save(hd);
    }

    private HoaDon getHoaDonByCode(String code) {
        return this.hoaDonRepository.findHoaDonByMaHoaDon(code).orElseThrow(() -> new EntityNotFound("Not found!"));
    }
}
