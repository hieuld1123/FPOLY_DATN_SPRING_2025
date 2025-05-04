package com.example.datnsd26.controller;

import com.example.datnsd26.Dto.HoaDonBinhRequest;
import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.*;
import com.example.datnsd26.services.BinhMailService;
import com.example.datnsd26.services.VoucherService;
import com.example.datnsd26.services.cart.GioHangService;
import com.example.datnsd26.services.cart.HoaDonService;
import com.example.datnsd26.utilities.CommonUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop/")
public class CartController {
    private final GioHangService gioHangService;
    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonService hoaDonService;
    private final BinhMailService mailService;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final KichCoRepository kichCoRepository;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final DiaChiRepository diaChiRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherService voucherService;
    private final NhanVienRepository nhanVienRepository;

    @GetMapping("/shop")
    public String shopPage(Model model) {
        model.addAttribute("productDetailsList", sanPhamChiTietRepository.findAll());
        return "/shop/shop";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, Authentication auth) {
        GioHang gioHang = gioHangService.getGioHangHienTai(auth);

        float tongTamTinh = gioHang.getChiTietList().stream()
                .mapToInt(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam().intValue())
                .sum();
        model.addAttribute("gioHang", gioHang.getChiTietList());
        model.addAttribute("tongTamTinh", tongTamTinh);
        return "/shop/cart"; // Trả về trang Thymeleaf để hiển thị giỏ hàng
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Integer sanPhamChiTietId,
                            @RequestParam("quantity") int soLuongThem,
                            Authentication auth,
                            RedirectAttributes redirect) {
        final long LIMIT_GIO_HANG = 100000000L; // 100 triệu

        try {
            // Lấy thông tin sản phẩm chi tiết
            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(sanPhamChiTietId).orElse(null);
            if (spct == null) {
                redirect.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
                return "redirect:/shop";
            }
            // Kiểm tra trạng thái sản phẩm trước khi thêm vào giỏ hàng
            if (!spct.getTrangThai()) {
                redirect.addFlashAttribute("errorMessage", "Sản phẩm này đã ngừng kinh doanh.");
                return "redirect:/shop/product/details/" + spct.getId(); // Trả về trang chi tiết
            }
            // Lấy giỏ hàng hiện tại của người dùng
            GioHang gioHang = gioHangService.getGioHangHienTai(auth);

            // Tính số lượng đã có sẵn trong giỏ hàng
            int daCoTrongGio = gioHangService.getSoLuongSanPhamTrongGio(gioHang, spct);// Giả sử đã có hàm này
            int soLuongTon = spct.getSoLuong();

            // Nếu tổng vượt quá số lượng tồn
            if (soLuongThem + daCoTrongGio > soLuongTon) {
                redirect.addFlashAttribute("errorMessage",
                        "Bạn đã thêm tối đa số lượng còn lại của sản phẩm này vào giỏ. " +
                                "Liên hệ 1900 6680 để được hỗ trợ đặt hàng với số lượng lớn hơn.");
                return "redirect:/shop/product/details/" + spct.getId();
            }

            // --- VALIDATE GIÁ TRỊ SẢN PHẨM THÊM VÀO (KHÔNG dùng BigDecimal) ---
            long giaTriSanPhamThem = (long) (spct.getGiaBanSauGiam() * soLuongThem);
            if (giaTriSanPhamThem > LIMIT_GIO_HANG) {
                redirect.addFlashAttribute("errorMessage",
                        "Giá trị sản phẩm bạn muốn thêm (" + String.format("%,d", giaTriSanPhamThem) + " VND) vượt quá giới hạn 100 triệu đồng cho mỗi lần thêm. Vui lòng liên hệ 1900 6680 để được hỗ trợ.");
                return "redirect:/shop/product/details/" + spct.getId();
            }

            // --- VALIDATE GIÁ TRỊ TOÀN BỘ GIỎ HÀNG (KHÔNG dùng BigDecimal) ---
            float giaTriGioHangHienTaiFloat = gioHangService.tinhTongGiaTriGioHangFloat(gioHang); // Giả sử có hàm này trả về float
            float giaTriSanPhamThemFloat = spct.getGiaBanSauGiam() * soLuongThem;

            // Chuyển sang long để so sánh an toàn hơn
            long tongGiaTriDuKien = (long) (giaTriGioHangHienTaiFloat + giaTriSanPhamThemFloat);

            if (tongGiaTriDuKien > LIMIT_GIO_HANG) {
                redirect.addFlashAttribute("errorMessage",
                        "Giá trị giỏ hàng hiện tại cộng với sản phẩm bạn muốn thêm sẽ vượt quá giới hạn 100 triệu đồng. Vui lòng kiểm tra lại giỏ hàng hoặc liên hệ 1900 6680 để được hỗ trợ.");
                return "redirect:/shop/product/details/" + spct.getId();
            }

            // Nếu hợp lệ thì thêm vào giỏ hàng
            gioHangService.themSanPhamVaoGioHang(sanPhamChiTietId, soLuongThem, auth);
            return "redirect:/shop/cart";

        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/shop/product/details/" + sanPhamChiTietId;
        }
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("chiTietId") Integer chiTietId) {
        gioHangService.xoaSanPhamKhoiGioHang(chiTietId);
        return "redirect:/shop/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(Authentication auth) {
        gioHangService.xoaToanBoGioHang(auth);
        return "redirect:/shop/cart";
    }

    @PostMapping("/checkout")
    public String goToCheckout(@RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds,
                               Model model,
                               Authentication auth,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {

        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        boolean isCustomer = false;

        if (isAuthenticated) {
            isCustomer = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
        }

        List<GioHangChiTiet> gioHangHienTai = gioHangService.getGioHangHienTai(auth).getChiTietList();
        if (gioHangHienTai == null || gioHangHienTai.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn hiện đang trống.");
            return "redirect:/shop/cart";
        }

        if (selectedIds == null || selectedIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 sản phẩm để thanh toán.");
            return "redirect:/shop/cart"; // quay lại trang giỏ hàng
        }

        if (!model.containsAttribute("hoaDonBinhRequest")) {
            model.addAttribute("hoaDonBinhRequest", new HoaDonBinhRequest());
        }

        List<GioHangChiTiet> danhSachThanhToan = gioHangChiTietRepository.findAllById(selectedIds);
        List<String> danhSachLoi = new ArrayList<>();
        List<GioHangChiTiet> danhSachHopLe = new ArrayList<>();

        for (GioHangChiTiet gioHangChiTiet : danhSachThanhToan) {
            SanPhamChiTiet spct = gioHangChiTiet.getSanPhamChiTiet();

            String tenSanPhamFull = spct.getSanPham().getTenSanPham()
                    + " - màu " + spct.getMauSac().getTenMauSac()
                    + " - size " + spct.getKichCo().getTen();

            if (!spct.getTrangThai()) {
                danhSachLoi.add("Sản phẩm \"" + tenSanPhamFull + "\" hiện đã ngưng kinh doanh.");
                continue;
            }

            if (gioHangChiTiet.getSoLuong() > spct.getSoLuong()) {
                danhSachLoi.add("Sản phẩm \"" + tenSanPhamFull + "\" chỉ còn " + spct.getSoLuong() + " sản phẩm trong hệ thống.");
                continue;
            }

            // Nếu hợp lệ thì thêm vào danh sách hiển thị
            danhSachHopLe.add(gioHangChiTiet);
        }

        if (!danhSachLoi.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Một số sản phẩm không hợp lệ:");
            redirectAttributes.addFlashAttribute("errors", danhSachLoi);
            return "redirect:/shop/cart";
        }

        float tongTamTinh = danhSachHopLe.stream()
                .mapToInt(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam().intValue())
                .sum();

        List<Voucher> allValidVouchers = voucherRepository.findValidVouchers(LocalDateTime.now(), tongTamTinh);


        // 4. Xử lý Khách Hàng
        KhachHang khachHang = null;
        DiaChi diaChiMacDinh = null;
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            TaiKhoan taiKhoan = taiKhoanRepository.findByEmail(email).orElse(null);

            if (taiKhoan != null) {
                khachHang = khachHangRepository.findByTaiKhoan(taiKhoan);
                if (khachHang != null && khachHang.getDiaChi() != null) {
                    diaChiMacDinh = khachHang.getDiaChi()
                            .stream()
                            .filter(DiaChi::getTrangThai) // hoặc .getTrangThai() nếu không dùng lombok getter
                            .findFirst()
                            .orElse(null);
                }
            }
        }

        // Sau khi xử lý danhSachHopLe thành công trong controller /checkout:
        float phiShip = 0f;
        if (tongTamTinh < 1000000) {
            phiShip = 30000.0f;
        }

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("isCustomer", isCustomer);
        session.setAttribute("selectedIds", selectedIds);
        model.addAttribute("tongTamTinh", tongTamTinh);
        model.addAttribute("phiShip", phiShip);
        model.addAttribute("cart", danhSachHopLe);
        model.addAttribute("khachHang", khachHang); // null nếu là khách vãng lai
        model.addAttribute("diaChiMacDinh", diaChiMacDinh);
        model.addAttribute("vouchers", allValidVouchers);
        return "shop/checkout";
    }


    @PostMapping("place-order")
    public String placeOrder(
            @Valid @ModelAttribute HoaDonBinhRequest hoaDonBinhRequest,
            BindingResult bindingResult,
            Model model,
            Authentication auth,
            HttpSession session) {

        // 1, Lấy thông tin người dùng nếu đã đăng nhập
        TaiKhoan taiKhoan = null;
        KhachHang khachHang = null;
        NhanVien nhanVien = null;
        DiaChi diaChiMacDinh = null;

        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            taiKhoan = taiKhoanRepository.findByEmail(email).orElse(null);
            if (taiKhoan != null) {
                if (taiKhoan.getVaiTro() == TaiKhoan.Role.CUSTOMER) {
                    khachHang = khachHangRepository.findByTaiKhoan(taiKhoan);
                    if (khachHang != null) {
                        diaChiMacDinh = khachHang.getDiaChi()
                                .stream()
                                .filter(DiaChi::getTrangThai)
                                .findFirst()
                                .orElse(null);
                    }
                } else if (taiKhoan.getVaiTro() == TaiKhoan.Role.EMPLOYEE || taiKhoan.getVaiTro() == TaiKhoan.Role.ADMIN) {
                    nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan);
                }
            }
        }

        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        boolean isCustomer = false;

        if (isAuthenticated) {
            isCustomer = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
        }

        // 2, Lấy danh sách sản phẩm đã chọn từ session, nếu không có thì thông báo lỗi
        List<Integer> selectedIds = (List<Integer>) session.getAttribute("selectedIds");
        if (selectedIds == null || selectedIds.isEmpty()) {
            model.addAttribute("isAuthenticated", isAuthenticated);
            model.addAttribute("isCustomer", isCustomer);
            model.addAttribute("errorMessage", "Bạn chưa chọn sản phẩm nào để thanh toán. Vui lòng quay lại giỏ hàng.");
            model.addAttribute("cart", new ArrayList<>()); // hiển thị sản phẩm da chon
            model.addAttribute("tongTamTinh", 0);
            model.addAttribute("khachHang", khachHang);
            model.addAttribute("phiShip", 0);
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            return "shop/checkout"; // quay lại trang giỏ hàng
        }

        // 3, Validate sản phẩm người dùng đã chọn, check xem có còn đủ hàng hay có sản phẩm nào đã ngừng kinh doanh không
        List<GioHangChiTiet> danhSachThanhToan = gioHangChiTietRepository.findAllById(selectedIds);
        List<String> danhSachLoi = new ArrayList<>();

        for (GioHangChiTiet gioHangChiTiet : danhSachThanhToan) {
            SanPhamChiTiet spct = gioHangChiTiet.getSanPhamChiTiet();

            String tenSanPhamFull = spct.getSanPham().getTenSanPham()
                    + " - màu " + spct.getMauSac().getTenMauSac()
                    + " - size " + spct.getKichCo().getTen();

            if (!spct.getTrangThai()) {
                danhSachLoi.add("Sản phẩm \"" + tenSanPhamFull + "\" hiện đã ngưng kinh doanh.");
                continue;
            }

            if (gioHangChiTiet.getSoLuong() > spct.getSoLuong()) {
                danhSachLoi.add("Sản phẩm \"" + tenSanPhamFull + "\" chỉ còn " + spct.getSoLuong() + " sản phẩm trong hệ thống.");
                continue;
            }
        }

        float tongTamTinh = (float) danhSachThanhToan.stream()
                .mapToDouble(item -> item.getSoLuong() * item.getSanPhamChiTiet().getGiaBanSauGiam())
                .sum();

        float phiShip = 0f;
        if (tongTamTinh < 1000000) {
            phiShip = 30000.0f;
        }

        // 4, Nếu có lỗi trong danh sách sản phẩm từ bước (3), hiển thị thông báo lỗi
        if (!danhSachLoi.isEmpty()) {
            model.addAttribute("isAuthenticated", isAuthenticated);
            model.addAttribute("isCustomer", isCustomer);
            model.addAttribute("errorMessage", "Một số sản phẩm không hợp lệ:");
            model.addAttribute("errors", danhSachLoi);
            model.addAttribute("cart", danhSachThanhToan); // hiển thị sản phẩm da chon
            model.addAttribute("tongTamTinh", tongTamTinh);
            model.addAttribute("khachHang", khachHang);
            model.addAttribute("phiShip", phiShip);
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            model.addAttribute("vouchers", voucherRepository.findValidVouchers(LocalDateTime.now(), tongTamTinh)); // Load lại danh sách voucher hợp lệ
            return "shop/checkout";
        }

        // 5, Validate thông tin giao hàng
        // Nếu là khách vãng lai (auth == null) HOẶC là nhân viên/admin (nhanVien != null), thì validate form
        if (auth == null || nhanVien != null) {
            if (bindingResult.hasErrors()) {
                model.addAttribute("isAuthenticated", isAuthenticated);
                model.addAttribute("isCustomer", isCustomer);
                model.addAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin");
                model.addAttribute("cart", danhSachThanhToan); // hiển thị sản phẩm da chon
                model.addAttribute("tongTamTinh", tongTamTinh);
                model.addAttribute("phiShip", phiShip);
                model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
                model.addAttribute("vouchers", voucherRepository.findValidVouchers(LocalDateTime.now(), tongTamTinh)); // Load lại danh sách voucher hợp lệ
                return "shop/checkout";
            }
        }

        // --- BƯỚC KIỂM TRA GIÁ TRỊ ĐƠN HÀNG MỚI ---
        if (tongTamTinh > 100000000L) { // 100tr đồng
            model.addAttribute("isAuthenticated", isAuthenticated);
            model.addAttribute("isCustomer", isCustomer);
            model.addAttribute("errorMessage", "Đơn hàng có giá trị vượt quá ngưỡng cho phép giao dịch trực tuyến. Quý khách vui lòng liên hệ trực tiếp bộ phận Chăm sóc Khách hàng theo số hotline: 1900 6680 để được hỗ trợ và tư vấn chi tiết.");
            model.addAttribute("cart", danhSachThanhToan); // hiển thị sản phẩm da chon
            model.addAttribute("tongTamTinh", tongTamTinh);
            model.addAttribute("phiShip", phiShip);
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            model.addAttribute("vouchers", voucherRepository.findValidVouchers(LocalDateTime.now(), tongTamTinh)); // Load lại danh sách voucher hợp lệ
            return "shop/checkout"; // Trở lại trang checkout với thông báo lỗi
        }
        // --- KẾT THÚC BƯỚC KIỂM TRA GIÁ TRỊ ĐƠN HÀNG MỚI ---

        // 6, Validate voucher được sửa dụng (tính giá được giảm, cập nhật số lượng voucher)
        Voucher voucher = null;
        float giamGia = 0f;

        if (hoaDonBinhRequest.getIdVoucher() != null) {
            voucher = voucherRepository.findById(hoaDonBinhRequest.getIdVoucher()).orElse(null);
            if (voucher == null || voucher.getTrangThai() != 1 || voucher.getSoLuong() <= 0 ||
                    voucher.getNgayBatDau().isAfter(LocalDateTime.now()) || voucher.getNgayKetThuc().isBefore(LocalDateTime.now()) ||
                    !voucher.getCongKhai() || tongTamTinh < voucher.getGiaTriGiamToiThieu()) {
                model.addAttribute("isAuthenticated", isAuthenticated);
                model.addAttribute("isCustomer", isCustomer);
                model.addAttribute("errorMessage", "Voucher bạn chọn không hợp lệ hoặc đã hết lượt sử dụng.");
                model.addAttribute("cart", danhSachThanhToan);
                model.addAttribute("tongTamTinh", tongTamTinh);
                model.addAttribute("phiShip", phiShip);
                model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
                model.addAttribute("vouchers", voucherRepository.findValidVouchers(LocalDateTime.now(), tongTamTinh)); // Load lại danh sách voucher hợp lệ
                return "shop/checkout";
            }
            // Nếu voucher hợp lệ, tiếp tục áp dụng giảm giá và cập nhật số lượng
            giamGia = voucherService.tinhGiamGia(tongTamTinh, voucher);
            voucher.setSoLuong(voucher.getSoLuong() - 1);
            if (voucher.getSoLuong() <= 0) {
                voucher.setTrangThai(2);
            }
            voucherRepository.save(voucher);
        }

        // 7, Tạo Hóa Đơn mới
        HoaDon.HoaDonBuilder hoaDonBuilder = HoaDon.builder()
                .maHoaDon(CommonUtils.generateInvoiceCode())
                .hinhThucMuaHang("Online")
                .ngayTao(new Date())
                .ngayCapNhat(new Date())
                .trangThai("Chờ xác nhận")
                .phiVanChuyen(phiShip)
                .tongTien(tongTamTinh)
                .phuongThucThanhToan("Thanh toán khi nhận hàng")
                .thanhToan(true)
                .ghiChu(hoaDonBinhRequest.getGhiChu())
                .khachHang(khachHang)
                .nhanVien(nhanVien)
                .giamGia(giamGia)
                .thanhTien(tongTamTinh - giamGia + phiShip)
                .voucher(voucher);

        // Thiết lập thông tin người nhận và địa chỉ
        if (khachHang != null && diaChiMacDinh != null) {
            hoaDonBuilder.tenNguoiNhan(khachHang.getTenKhachHang())
                    .sdtNguoiNhan(khachHang.getTaiKhoan().getSdt())
                    .email(khachHang.getTaiKhoan().getEmail())
                    .tinh(diaChiMacDinh.getTinh())
                    .quan(diaChiMacDinh.getHuyen())
                    .xa(diaChiMacDinh.getXa())
                    .diaChiNguoiNhan(diaChiMacDinh.getDiaChiCuThe());
        } else if (nhanVien != null) {
            hoaDonBuilder.tenNguoiNhan(hoaDonBinhRequest.getTenNguoiNhan())
                    .sdtNguoiNhan(hoaDonBinhRequest.getSdtNguoiNhan())
                    .email(hoaDonBinhRequest.getEmail())
                    .tinh(hoaDonBinhRequest.getTinh())
                    .quan(hoaDonBinhRequest.getQuan())
                    .xa(hoaDonBinhRequest.getXa())
                    .diaChiNguoiNhan(hoaDonBinhRequest.getDiaChiNguoiNhan());
        } else { // Khách vãng lai
            hoaDonBuilder.tenNguoiNhan(hoaDonBinhRequest.getTenNguoiNhan())
                    .sdtNguoiNhan(hoaDonBinhRequest.getSdtNguoiNhan())
                    .email(hoaDonBinhRequest.getEmail())
                    .tinh(hoaDonBinhRequest.getTinh())
                    .quan(hoaDonBinhRequest.getQuan())
                    .xa(hoaDonBinhRequest.getXa())
                    .diaChiNguoiNhan(hoaDonBinhRequest.getDiaChiNguoiNhan());
        }

        HoaDon hoaDon = hoaDonBuilder.build();
        hoaDonService.saveHoaDon(hoaDon);
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Đặt hàng").hoaDon(hoaDon).build());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Chờ xác nhận").hoaDon(hoaDon).build());

        // 8, Lưu chi tiết hóa đơn tương ứng với những gì KH đặt hàng
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();
        for (GioHangChiTiet item : danhSachThanhToan) {
            HoaDonChiTiet chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(hoaDon);
            chiTiet.setSanPhamChiTiet(item.getSanPhamChiTiet());
            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setGiaTienSauGiam(item.getSanPhamChiTiet().getGiaBanSauGiam());
            hoaDonChiTietRepository.save(chiTiet);
            chiTietList.add(chiTiet);
        }
        hoaDon.setDanhSachSanPham(chiTietList);
        hoaDonService.saveHoaDon(hoaDon);

        // 9, Gửi email cho KH
        try {
            mailService.sendOrderConfirmation(hoaDon);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        for (Integer id : selectedIds) {
            gioHangService.xoaSanPhamKhoiGioHang(id);
        }

        // 10, Xóa session selectedIds, mọi giá hiển thị về 0 sau khi đặt hàng xong
        session.removeAttribute("selectedIds");
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("isCustomer", isCustomer);
        model.addAttribute("successMessage", "Đặt hàng thành công. Vui lòng kiểm tra email. Chúng tôi sẽ liên hệ với bạn qua số điện thoại để xác nhận đơn hàng.");
        model.addAttribute("cart", new ArrayList<>());
        model.addAttribute("tongTamTinh", 0);
        model.addAttribute("phiShip", 0);
        model.addAttribute("hoaDonBinhRequest", new HoaDonBinhRequest());
        model.addAttribute("khachHang", khachHang);
        return "shop/checkout";
    }

}
