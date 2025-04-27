package com.example.datnsd26.services.impl;

import com.example.datnsd26.controller.request.InvoiceParamRequest;
import com.example.datnsd26.controller.request.InvoiceRecipientInfoRequest;
import com.example.datnsd26.controller.response.HoaDonChiTietResponse;
import com.example.datnsd26.controller.response.InvoiceInformation;
import com.example.datnsd26.controller.response.InvoicePageResponse;
import com.example.datnsd26.controller.response.SanPhamResponse;
import com.example.datnsd26.exception.EntityNotFound;
import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.HoaDonRepository;
import com.example.datnsd26.repository.LichSuHoaDonRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.VoucherRepository;
import com.example.datnsd26.repository.customizeQuery.InvoiceCustomizeQuery;
import com.example.datnsd26.services.HoaDonService;
import com.example.datnsd26.utilities.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "HOA-DON-SERVICE")
public class HoaDonServiceImp implements HoaDonService {

    private final InvoiceCustomizeQuery invoiceCustomizeQuery;

    private final HoaDonRepository hoaDonRepository;

    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    private final VoucherRepository voucherRepository;

    private final AuthUtil authUtil;

    private static final String STATUS_CONFIRMED = "Đã xác nhận";
    private static final String STATUS_DELIVERED = "Đã giao cho đơn vị vận chuyển";
    private static final String STATUS_CANCELED = "Đã hủy";
    private static final String STATUS_COMPLETED = "Hoàn thành";
    private static final String STATUS_EDIT = "Chỉnh sửa đơn hàng";


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
                        .image(s.getSanPhamChiTiet().getHinhAnh().get(0).getTenAnh())
                        .code(s.getSanPhamChiTiet().getMaSanPhamChiTiet())
                        .name(String.format("%s [%s - %s]", s.getSanPhamChiTiet().getSanPham().getTenSanPham(), s.getSanPhamChiTiet().getMauSac().getTenMauSac(), s.getSanPhamChiTiet().getKichCo().getTen()))
                        .quantity(s.getSoLuong())
                        .unit_price(s.getSanPhamChiTiet().getGiaBanSauGiam())
                        .total_price(s.getSoLuong() * s.getGiaTienSauGiam())
                        .build()).toList())
                .summary(InvoiceInformation.Summary.builder()
                        .subtotal(hd.getTongTien())
                        .shipping_fee(hd.getPhiVanChuyen())
                        .discount(hd.getGiamGia() == null ? 0f : hd.getGiamGia())
                        .total(hd.getThanhTien())
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
        if(hd.getVoucher() != null) {
            Voucher voucher = hd.getVoucher();
            voucher.setSoLuong(voucher.getSoLuong() + 1);
            this.voucherRepository.save(voucher);
        }
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
    public HoaDonChiTietResponse editInvoice(String code) {
        var hoaDon = this.hoaDonRepository.findHoaDonByMaHoaDon(code).orElse(new HoaDon());
        List<SanPhamResponse> listSanPham = hoaDon.getDanhSachSanPham().stream().map(sp ->
                SanPhamResponse.builder()
                        .id(sp.getId())
                        .maSanPham(sp.getSanPhamChiTiet().getMaSanPhamChiTiet())
                        .tenSanPham(String.format("%s [%s - %s]", sp.getSanPhamChiTiet().getSanPham().getTenSanPham(), sp.getSanPhamChiTiet().getMauSac().getTenMauSac(), sp.getSanPhamChiTiet().getKichCo().getTen()))
                        .gia(sp.getSanPhamChiTiet().getGiaBanSauGiam())
                        .soLuong(sp.getSoLuong())
                        .soLuongTonKho(sp.getSanPhamChiTiet().getSoLuong())
                        .hinhAnh(sp.getSanPhamChiTiet().getHinhAnh().get(0).getTenAnh())
                        .build()
        ).toList();
        KhachHang kh = hoaDon.getKhachHang();
        return HoaDonChiTietResponse.builder()
                .id(hoaDon.getId())
                .tongTien(hoaDon.getTongTien())
                .shippingFee(hoaDon.getPhiVanChuyen())
                .ghiChu(hoaDon.getGhiChu() == null ? null : hoaDon.getGhiChu().trim())
                .khachHang(kh == null ? null : HoaDonChiTietResponse.Customer.builder()
                        .id(kh.getId())
                        .maKhachHang(kh.getMaKhachHang())
                        .tenKhachHang(kh.getTenKhachHang())
                        .soDienThoai(kh.getTaiKhoan().getSdt())
                        .diaChi(String.format("%s, %s, %s, %s", hoaDon.getDiaChiNguoiNhan(), hoaDon.getXa(), hoaDon.getQuan(), hoaDon.getTinh()))
                        .build())
                .listSanPham(listSanPham)
                .seller(hoaDon.getNhanVien() == null ? "N/A" : hoaDon.getNhanVien().getTenNhanVien())
                .ngayTao(hoaDon.getNgayTao())
                .ngayCapNhat(hoaDon.getNgayCapNhat())
                .build();
    }

    @Override
    public void completed(String code) {
        HoaDon hd = getHoaDonByCode(code);
        hd.setTrangThai(STATUS_COMPLETED);
        hd.setNgayCapNhat(new Date());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai(STATUS_COMPLETED).hoaDon(hd).build());
        this.hoaDonRepository.save(hd);
    }

    @Override
    public void createHistoryModify(String invoiceCode) {
        HoaDon hoaDon = this.getHoaDonByCode(invoiceCode);
        Optional<LichSuHoaDon> history = this.lichSuHoaDonRepository.findByStatusAndInvoice(STATUS_EDIT, hoaDon.getId());
        if(history.isPresent()) {
            LichSuHoaDon lichSuHoaDon = history.get();
            lichSuHoaDon.setThoiGian(new Date());
            lichSuHoaDon.setTrangThai(STATUS_EDIT);
            this.lichSuHoaDonRepository.save(lichSuHoaDon);
            return;
        }
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai(STATUS_EDIT).hoaDon(hoaDon).build());
    }

    @Override
    public void updateRecipient(String orderId, InvoiceRecipientInfoRequest request) {
        HoaDon hoaDon = getHoaDonByCode(orderId);
        hoaDon.setTenNguoiNhan(request.getName());
        hoaDon.setSdtNguoiNhan(request.getPhone());
        hoaDon.setTinh(request.getProvince());
        hoaDon.setQuan(request.getDistrict());
        hoaDon.setXa(request.getWard());
        hoaDon.setDiaChiNguoiNhan(request.getSpecificAddress());
        hoaDon.setPhiVanChuyen(request.getShippingFee());
        hoaDon.setGhiChu(request.getOrderNote().isBlank() ? null : request.getOrderNote());
        hoaDon.setThanhTien(hoaDon.getTongTien() + hoaDon.getPhiVanChuyen() - (hoaDon.getGiamGia() == null ? 0 : hoaDon.getGiamGia()));
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai(String.format("Đã chỉnh sửa bởi %s", this.authUtil.getNhanVien().getTenNhanVien())).hoaDon(hoaDon).build());
        this.hoaDonRepository.save(hoaDon);
    }

    private HoaDon getHoaDonByCode(String code) {
        return this.hoaDonRepository.findHoaDonByMaHoaDon(code).orElseThrow(() -> new EntityNotFound("Not found!"));
    }
}