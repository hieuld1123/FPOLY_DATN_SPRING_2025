package com.example.datnsd26.services.impl;

import com.example.datnsd26.controller.request.AddressRequest;
import com.example.datnsd26.controller.request.PaymentRequest;
import com.example.datnsd26.controller.request.StoreCustomerRequest;
import com.example.datnsd26.controller.response.*;
import com.example.datnsd26.exception.EntityNotFound;
import com.example.datnsd26.exception.InvalidDataException;
import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.*;
import com.example.datnsd26.services.BanHangService;
import com.example.datnsd26.services.EmailService;
import com.example.datnsd26.utilities.AuthUtil;
import com.example.datnsd26.utilities.CommonUtils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.datnsd26.utilities.CommonUtils.generateInvoiceCode;

@Service
@Slf4j(topic = "BAN-HANG-SERVICE")
@RequiredArgsConstructor
public class BanHangServiceImpl implements BanHangService {
    private final HoaDonRepository hoaDonRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final AuthUtil authUtil;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final DiaChiRepository diaChiRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

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
                        .maSanPham(sp.getSanPhamChiTiet().getMaSanPhamChiTiet())
                        .tenSanPham(String.format("%s [%s - %s]", sp.getSanPhamChiTiet().getSanPham().getTenSanPham(), sp.getSanPhamChiTiet().getMauSac().getTenMauSac(), sp.getSanPhamChiTiet().getKichCo().getTen()))
                        .gia(sp.getSanPhamChiTiet().getGiaBanSauGiam())
                        .soLuong(sp.getSoLuong())
                        .soLuongTonKho(sp.getSanPhamChiTiet().getSoLuong())
                        .hinhAnh("https://th.bing.com/th/id/OIP.8tQmmY_ccVpcxBxu0Z0mzwHaE8?rs=1&pid=ImgDetMain") // TODO
                        .build()
        ).toList();
        KhachHang kh = hoaDon.getKhachHang();
        return HoaDonChiTietResponse.builder()
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
                .build();
    }

    @Override
    public Integer createHoaDon() {
        HoaDon hoaDon = HoaDon.builder()
                .maHoaDon(generateInvoiceCode())
                .trangThai("Đang xử lý")
                .tongTien(0f)
                .phiVanChuyen(0f)
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
        return this.sanPhamChiTietRepository.findByNameOrCode(keyword).stream().map(sp -> SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPhamChiTiet())
                .tenSanPham(String.format("%s [%s - %s]", sp.getSanPham().getTenSanPham(), sp.getMauSac().getTenMauSac(), sp.getKichCo().getTen()))
                .gia(sp.getGiaBanSauGiam()) // Note: update
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
            hoaDon.setTongTien(hoaDon.getTongTien() + product.getGiaBanSauGiam()); // Note
            return this.hoaDonChiTietRepository.save(hoaDonChiTiet).getId();
        }
        log.info("addToCart invoice id: {}", invoiceId);
        HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                .giaTienSauGiam(product.getGiaBanSauGiam()) // Note
                .sanPhamChiTiet(product)
                .hoaDon(hoaDon)
                .soLuong(1)
                .build();
        hoaDon.setTongTien(hoaDon.getTongTien() + product.getGiaBanSauGiam()); // Note
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
                        .mapToDouble(sp -> sp.getSoLuong() * sp.getSanPhamChiTiet().getGiaBanSauGiam()) // Note
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
        List<HoaDonChiTiet> danhSachSanPham = hoaDon.getDanhSachSanPham();
        danhSachSanPham.forEach(sp -> {
            if(Boolean.FALSE.equals(sp.getSanPhamChiTiet().getTrangThai())){
                throw new InvalidDataException(String.format("Sản phẩm %s đã ngừng kinh doanh", sp.getSanPhamChiTiet().getMaSanPhamChiTiet()));
            }
        });
        hoaDon.setNhanVien(authUtil.getNhanVien());
        hoaDon.setHinhThucMuaHang(paymentRequest.getType());
        hoaDon.setPhiVanChuyen(0f);
        hoaDon.setPhiVanChuyen(paymentRequest.getShippingFee());
        hoaDon.setTrangThai(paymentRequest.getType().equalsIgnoreCase("Offline") ? "Hoàn thành" : "Chờ xác nhận");
        if (hoaDon.getHinhThucMuaHang().equalsIgnoreCase("Có giao hàng")) {
            hoaDon.setTrangThai("Đã xác nhận");
        }
        if (hoaDon.getHinhThucMuaHang().equalsIgnoreCase("Có giao hàng") || hoaDon.getHinhThucMuaHang().equalsIgnoreCase("Online")) {
            hoaDon.setPhuongThucThanhToan(paymentRequest.getPaymentMethod());
        }
        hoaDon.setThanhToan(true);
        hoaDon.setNgayCapNhat(new Date());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Đặt hàng").hoaDon(hoaDon).build());

        if (hoaDon.getHinhThucMuaHang().equalsIgnoreCase("Offline") && hoaDon.isThanhToan()) {
            lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Hoàn thành").hoaDon(hoaDon).build());
        }
        if (hoaDon.getHinhThucMuaHang().equalsIgnoreCase("Có giao hàng")) {
            lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Đã xác nhận").hoaDon(hoaDon).build());
        }
        this.hoaDonRepository.save(hoaDon);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void cancelInvoice(int invoiceId) {
        HoaDon hoaDon = findHoaDonById(invoiceId);
        this.lichSuHoaDonRepository.deleteByInvoiceCode(hoaDon.getMaHoaDon());
        hoaDon.getDanhSachSanPham().forEach(hdct -> {
            SanPhamChiTiet spct = findSanPhamChiTietById(hdct.getSanPhamChiTiet().getId());
            spct.setSoLuong(spct.getSoLuong() + hdct.getSoLuong());
            this.sanPhamChiTietRepository.save(spct);
            this.hoaDonChiTietRepository.delete(hdct);
        });
        this.hoaDonRepository.delete(hoaDon);
    }

    @Override
    public List<CustomerResponse> getCustomerByInfo(String keyword) {
        return this.khachHangRepository.findByNameOrCodeOrPhone(keyword).stream().map(khachHang -> CustomerResponse.builder()
                .id(khachHang.getId())
                .tenKhachHang(khachHang.getTenKhachHang())
                .maKhachHang(khachHang.getMaKhachHang())
                .soDienThoai(khachHang.getTaiKhoan().getSdt())
                .build()).collect(Collectors.toList());
    }

    @Override
    public void addCustomerToInvoice(Integer invoiceId, Integer idKhachHang) {
        KhachHang kh = findKhachHangById(idKhachHang);
        HoaDon hd = findHoaDonById(invoiceId);
        if (kh != null && hd != null) {
            DiaChi dc = kh.getDiaChi().get(0);
            hd.setKhachHang(kh);
            hd.setXa(dc.getXa());
            hd.setQuan(dc.getHuyen());
            hd.setTinh(dc.getTinh());
            hd.setDiaChiNguoiNhan(dc.getDiaChiCuThe());
            hd.setSdtNguoiNhan(kh.getTaiKhoan().getSdt());
            hd.setTenNguoiNhan(kh.getTenKhachHang());
            hd.setEmail(kh.getTaiKhoan().getEmail());
            this.hoaDonRepository.save(hd);
            log.info("Update success");
        }
    }

    @Override
    public void removeCustomer(Integer invoiceId) {
        HoaDon hd = findHoaDonById(invoiceId);
        hd.setKhachHang(null);
        hd.setXa(null);
        hd.setQuan(null);
        hd.setTinh(null);
        hd.setDiaChiNguoiNhan(null);
        hd.setSdtNguoiNhan(null);
        hd.setTenNguoiNhan(null);
        hd.setEmail(null);
        this.hoaDonRepository.save(hd);
        log.info("Remove success");
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Integer createCustomer(Integer invoiceId, StoreCustomerRequest request) throws MessagingException {
        TaiKhoan tk = TaiKhoan.builder()
                .sdt(request.getPhone_number())
                .trangThai(true)
                .vaiTro(TaiKhoan.Role.CUSTOMER)
                .build();

        // NOTE: If the email is null, the password can't be viewed. You must forget your password to be able to login.
        if (StringUtils.hasLength(request.getEmail())) {
            Optional<TaiKhoan> byEmail = this.taiKhoanRepository.findByEmail(request.getEmail());
            if (byEmail.isPresent()) {
                throw new InvalidDataException("Email đã tồn tại");
            }
            String password = CommonUtils.generateRandomPassword(10);
            tk.setEmail(request.getEmail());
            tk.setMatKhau(this.passwordEncoder.encode(password));
            emailService.sendNewCustomerAccountEmail(request.getEmail(), password);
        }

        tk = this.taiKhoanRepository.save(tk);

        KhachHang kh = KhachHang.builder()
                .taiKhoan(tk)
                .gioiTinh(true)
                .tenKhachHang(request.getRecipient_name())
                .maKhachHang(CommonUtils.generateCustomerCode())
                .ngayTao(new Timestamp(System.currentTimeMillis()))
                .ngayCapNhat(new Timestamp(System.currentTimeMillis()))
                .trangThai(true)
                .build();

        kh = this.khachHangRepository.save(kh);

        DiaChi dc = DiaChi.builder()
                .tinh(request.getProvince())
                .huyen(request.getDistrict())
                .xa(request.getWard())
                .diaChiCuThe(request.getAddressDetail())
                .khachHang(kh)
                .trangThai(true)
                .build();

        this.diaChiRepository.save(dc);

        HoaDon hd = findHoaDonById(invoiceId);
        if (hd != null) {
            hd.setKhachHang(kh);
            hd.setXa(dc.getXa());
            hd.setQuan(dc.getHuyen());
            hd.setTinh(dc.getTinh());
            hd.setDiaChiNguoiNhan(dc.getDiaChiCuThe());
            hd.setTenNguoiNhan(kh.getTaiKhoan().getSdt());
            hd.setEmail(kh.getTaiKhoan().getEmail());
            hd.setSdtNguoiNhan(kh.getTaiKhoan().getEmail());
            this.hoaDonRepository.save(hd);
        }
        return kh.getId();
    }

    @Override
    public List<CustomerAddressResponse> customerAddresses(Integer customerId) {
        return findKhachHangById(customerId).getDiaChi().stream().map(dc -> CustomerAddressResponse.builder()
                .province(dc.getTinh())
                .district(dc.getHuyen())
                .ward(dc.getXa())
                .addressDetail(dc.getDiaChiCuThe())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateAddress(Integer customerId, Integer invoiceId, AddressRequest request) {
        KhachHang kh = findKhachHangById(customerId);
        DiaChi dc;
        Optional<DiaChi> firstByValues = this.diaChiRepository.findFirstByValues(customerId,
                request.getProvince(),
                request.getDistrict(),
                request.getWard(),
                request.getAddressDetail());
        if (firstByValues.isPresent()) {
            dc = firstByValues.get();
        }else{
            dc = DiaChi.builder()
                    .tinh(request.getProvince())
                    .huyen(request.getDistrict())
                    .xa(request.getWard())
                    .diaChiCuThe(request.getAddressDetail())
                    .khachHang(kh)
                    .trangThai(true)
                    .build();
            dc = this.diaChiRepository.save(dc);
        }
        HoaDon hd = findHoaDonById(invoiceId);
        hd.setXa(dc.getXa());
        hd.setQuan(dc.getHuyen());
        hd.setTinh(dc.getTinh());
        hd.setDiaChiNguoiNhan(dc.getDiaChiCuThe());
        this.hoaDonRepository.save(hd);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updatePhone(Integer customerId, String phoneNumber, Integer invoiceId) {
        KhachHang kh = findKhachHangById(customerId);
        TaiKhoan tk = kh.getTaiKhoan();
        tk.setSdt(phoneNumber);
        this.taiKhoanRepository.save(tk);
        HoaDon hd = findHoaDonById(invoiceId);
        hd.setSdtNguoiNhan(phoneNumber);
        this.hoaDonRepository.save(hd);

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

    private KhachHang findKhachHangById(Integer id) {
        return this.khachHangRepository.findById(id).orElseThrow(() -> new EntityNotFound("Không tìm khách hàng!"));
    }
}
