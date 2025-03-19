package com.example.datnsd26.services.impl;

import com.example.datnsd26.controller.request.PaymentRequest;
import com.example.datnsd26.controller.response.HoaDonChiTietResponse;
import com.example.datnsd26.controller.response.HoaDonResponse;
import com.example.datnsd26.controller.response.SanPhamResponse;
import com.example.datnsd26.exception.EntityNotFound;
import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.models.HoaDonChiTiet;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.HoaDonChiTietRepository;
import com.example.datnsd26.repository.HoaDonRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.services.BanHangService;
import com.example.datnsd26.utilities.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.example.datnsd26.utilities.CommonUtils.generateInvoiceCode;

@Service
@Slf4j(topic = "BAN-HANG-SERVICE")
@RequiredArgsConstructor
public class BanHangServiceImpl implements BanHangService {
    private final HoaDonRepository hoaDonRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final AuthUtil authUtil;

    @Override
    public List<HoaDonResponse> getHoaDon() {
        return this.hoaDonRepository.findAllInvoiceByStatus("Đang xử lý").stream().map(s -> HoaDonResponse.builder()
                .id(s.getId())
                .maHoaDon(s.getMaHoaDon())
                .tranThai(s.getTrangThai())
                .build()).toList();
    }

    @Override
    public HoaDonChiTietResponse getHoaDonChiTiet(int id) {
        var hoaDon = this.findHoaDonById(id);
        List<SanPhamResponse> listSanPham = hoaDon.getDanhSachSanPham().stream().map(sp ->
                SanPhamResponse.builder()
                        .id(sp.getId())
                        .maSanPham(sp.getSanPhamChiTiet().getSanPham().getMaSanPham())
                        .tenSanPham(String.format("%s [%s - %s]", sp.getSanPhamChiTiet().getSanPham().getTenSanPham(), sp.getSanPhamChiTiet().getMauSac().getTen(), sp.getSanPhamChiTiet().getKichCo().getTen()))
                        .gia(sp.getSanPhamChiTiet().getGiaBan())
                        .soLuong(sp.getSoLuong())
                        .soLuongTonKho(sp.getSanPhamChiTiet().getSoLuong())
                        .hinhAnh("https://th.bing.com/th/id/OIP.8tQmmY_ccVpcxBxu0Z0mzwHaE8?rs=1&pid=ImgDetMain") // TODO
                        .build()
        ).toList();
        return HoaDonChiTietResponse.builder()
                .tongTien(hoaDon.getTongTien())
                .ghiChu(hoaDon.getGhiChu() == null ? null : hoaDon.getGhiChu().trim())
                .listSanPham(listSanPham)
                .build();
    }

    @Override
    public Integer createHoaDon() {
        HoaDon hoaDon = HoaDon.builder()
                .maHoaDon(generateInvoiceCode())
                .trangThai("Đang xử lý")
                .tongTien(0f)
                .ngayTao(new Date())
                .ngayCapNhat(new Date())
                .build();
        return this.hoaDonRepository.save(hoaDon).getId();
    }

    @Override
    public List<SanPhamResponse> getSanPhamByName(String keyword) {
        log.info("get product keyword: {}", keyword);
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return this.sanPhamChiTietRepository.findByName(keyword).stream().map(sp -> SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getSanPham().getMaSanPham())
                .tenSanPham(String.format("%s [%s - %s]", sp.getSanPham().getTenSanPham(), sp.getMauSac().getTen(), sp.getKichCo().getTen()))
                .gia(sp.getGiaBan())
                .soLuong(sp.getSoLuong())
                .hinhAnh("https://th.bing.com/th/id/OIP.8tQmmY_ccVpcxBxu0Z0mzwHaE8?rs=1&pid=ImgDetMain") // TODO
                .build()).toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public int addToCart(int productId, int invoiceId) {
        log.info("addToCart productId: {}, invoiceId: {}", productId, invoiceId);
        Optional<HoaDonChiTiet> invoice = this.hoaDonChiTietRepository.findHoaDonChiTietByIdAndProductId(invoiceId, productId);
        SanPhamChiTiet product = this.findSanPhamChiTietById(productId);
        HoaDon hoaDon = this.findHoaDonById(invoiceId);
        product.setSoLuong(product.getSoLuong() - 1);
        this.sanPhamChiTietRepository.save(product);
        if (invoice.isPresent()) {
            log.info("update invoice id: {}", invoiceId);
            HoaDonChiTiet hoaDonChiTiet = invoice.get();
            hoaDonChiTiet.setSoLuong(hoaDonChiTiet.getSoLuong() + 1);
            hoaDon.setTongTien(hoaDon.getTongTien() + product.getGiaBan());
            return this.hoaDonChiTietRepository.save(hoaDonChiTiet).getId();
        }
        log.info("addToCart invoice id: {}", invoiceId);
        HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                .giaTienSauGiam(product.getGiaBan())
                .sanPhamChiTiet(product)
                .hoaDon(hoaDon)
                .soLuong(1)
                .build();
        hoaDon.setTongTien(hoaDon.getTongTien() + product.getGiaBan()); // NOTE: Price
        return this.hoaDonChiTietRepository.save(hoaDonChiTiet).getId();
    }

    @Override
    public void updateSoLuong(int invoiceDetailId, int newQuantity) {
        Optional<HoaDonChiTiet> invoiceDetailOpt = this.hoaDonChiTietRepository.findById(invoiceDetailId);

        if (invoiceDetailOpt.isPresent()) {
            HoaDonChiTiet hoaDonChiTiet = invoiceDetailOpt.get();
            SanPhamChiTiet sanPhamChiTiet = findSanPhamChiTietById(hoaDonChiTiet.getSanPhamChiTiet().getId());

            int currentStock = sanPhamChiTiet.getSoLuong();
            int oldQuantity = hoaDonChiTiet.getSoLuong();

            if (newQuantity > (currentStock + oldQuantity)) {
                throw new IllegalArgumentException("Số lượng vượt quá số lượng tồn kho!");
            }

            sanPhamChiTiet.setSoLuong(currentStock + oldQuantity - newQuantity);
            sanPhamChiTietRepository.save(sanPhamChiTiet);

            hoaDonChiTiet.setSoLuong(newQuantity);
            hoaDonChiTietRepository.save(hoaDonChiTiet);

            HoaDon hoaDon = hoaDonChiTiet.getHoaDon();
            if (hoaDon != null) {
                float newTotalPrice = (float) hoaDon.getDanhSachSanPham()
                        .stream()
                        .mapToDouble(sp -> sp.getSoLuong() * sp.getSanPhamChiTiet().getGiaBan())
                        .sum();
                hoaDon.setTongTien(newTotalPrice);
                hoaDonRepository.save(hoaDon);
            }
        }
    }


    @Override
    public void deleteItem(int itemId) {
        HoaDonChiTiet hoaDonChiTiet = findHoaDonChiTietById(itemId);
        SanPhamChiTiet product = hoaDonChiTiet.getSanPhamChiTiet();
        product.setSoLuong(product.getSoLuong() + hoaDonChiTiet.getSoLuong());
        this.sanPhamChiTietRepository.save(product);
        this.hoaDonChiTietRepository.delete(hoaDonChiTiet);
        HoaDon hoaDon = findHoaDonById(hoaDonChiTiet.getHoaDon().getId());
        hoaDon.setTongTien((float) hoaDon.getDanhSachSanPham()
                .stream()
                .mapToDouble(sp -> sp.getSoLuong() * sp.getSanPhamChiTiet().getGiaBan())
                .sum());
        hoaDonRepository.save(hoaDon);
    }

    @Override
    public void updateNote(int invoiceId, String note) {
        HoaDon hoaDon = findHoaDonById(invoiceId);
        hoaDon.setGhiChu(note);
        hoaDonRepository.save(hoaDon);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void payment(PaymentRequest paymentRequest) {
        HoaDon hoaDon = findHoaDonById(paymentRequest.getInvoiceId());
        hoaDon.setHinhThucMuaHang(paymentRequest.getType());
        // TODO Customer
        hoaDon.setNhanVien(authUtil.getNhanVien());
        hoaDon.setHinhThucMuaHang(paymentRequest.getType());
        hoaDon.setPhiVanChuyen(0f);
        hoaDon.setTrangThai(paymentRequest.getType().equalsIgnoreCase("Offline") ? "Hoàn thành" : "Đã giao cho đơn vị vận chuyển");
        hoaDon.setTenNguoiNhan(paymentRequest.getRecipient_name());
        hoaDon.setSdtNguoiNhan(paymentRequest.getPhone_number());
        hoaDon.setEmail(paymentRequest.getEmail());
        hoaDon.setTinh(paymentRequest.getProvince());
        hoaDon.setQuan(paymentRequest.getDistrict());
        hoaDon.setXa(paymentRequest.getWard());
        hoaDon.setDiaChiNguoiNhan(paymentRequest.getAddressDetail());
        hoaDon.setPhuongThucThanhToan(paymentRequest.getPaymentMethod());
        hoaDon.setThanhToan(paymentRequest.getPaymentMethod().equalsIgnoreCase("Thanh toán tại cửa hàng"));
        hoaDon.setNgayCapNhat(new Date());
        this.hoaDonRepository.save(hoaDon);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void cancelInvoice(int invoiceId) {
        HoaDon hoaDon = findHoaDonById(invoiceId);
        hoaDon.getDanhSachSanPham().forEach(hdct -> {
            SanPhamChiTiet spct = findSanPhamChiTietById(hdct.getSanPhamChiTiet().getId());
            spct.setSoLuong(spct.getSoLuong() + hdct.getSoLuong());
            this.sanPhamChiTietRepository.save(spct);
            this.hoaDonChiTietRepository.delete(hdct);
        });
        this.hoaDonRepository.delete(hoaDon);
    }

    private HoaDonChiTiet findHoaDonChiTietById(int id) {
        return this.hoaDonChiTietRepository.findById(id).orElseThrow(() -> new EntityNotFound("Không tìm thấy hóa đơn!"));
    }

    private HoaDon findHoaDonById(Integer id) {
        return this.hoaDonRepository.findById(id).orElseThrow(() -> new EntityNotFound("Không tìm thấy hóa đơn!"));
    }

    private SanPhamChiTiet findSanPhamChiTietById(Integer id) {
        return this.sanPhamChiTietRepository.findById(id).orElseThrow(() -> new EntityNotFound("Không tìm sản phẩm chi tiet!"));
    }
}
