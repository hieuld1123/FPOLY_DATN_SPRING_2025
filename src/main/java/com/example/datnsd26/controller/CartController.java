package com.example.datnsd26.controller;

import com.example.datnsd26.Dto.HoaDonBinhRequest;
import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.*;
import com.example.datnsd26.services.BinhMailService;
import com.example.datnsd26.services.VoucherService;
import com.example.datnsd26.services.cart.GioHangService;
import com.example.datnsd26.services.cart.HoaDonService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        session.setAttribute("selectedIds", selectedIds);
        model.addAttribute("tongTamTinh", tongTamTinh);
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

        // Lấy thông tin người dùng nếu đã đăng nhập
        TaiKhoan taiKhoan = null;
        KhachHang khachHang = null;
        DiaChi diaChiMacDinh = null;

        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            taiKhoan = taiKhoanRepository.findByEmail(email).orElse(null);
            if (taiKhoan != null) {
                khachHang = khachHangRepository.findByTaiKhoan(taiKhoan);
                if(khachHang != null) {
                    diaChiMacDinh = khachHang.getDiaChi()
                            .stream()
                            .filter(DiaChi::getTrangThai) // hoặc .getTrangThai() nếu không dùng lombok getter
                            .findFirst()
                            .orElse(null);
                }


            }
        }

        List<Integer> selectedIds = (List<Integer>) session.getAttribute("selectedIds");
        if (selectedIds == null || selectedIds.isEmpty()) {
            model.addAttribute("errorMessage", "Bạn chưa chọn sản phẩm nào để thanh toán. Vui lòng quay lại giỏ hàng.");
            model.addAttribute("cart", new ArrayList<>());  // hiển thị sản phẩm da chon
            model.addAttribute("tongTamTinh", 0);
            model.addAttribute("khachHang", khachHang);
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            return "shop/checkout"; // quay lại trang giỏ hàng
        }
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

        if (!danhSachLoi.isEmpty()) {
            System.out.println("VaoDay roiiii");
            model.addAttribute("errorMessage", "Một số sản phẩm không hợp lệ:");
            model.addAttribute("errors", danhSachLoi);
            model.addAttribute("cart", danhSachThanhToan);  // hiển thị sản phẩm da chon
            model.addAttribute("tongTamTinh", tongTamTinh);
            model.addAttribute("khachHang", khachHang);
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            return "shop/checkout";
        }

        // Validate thông tin giao hàng
        if (auth == null && bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin");
//            model.addAttribute("errors", danhSachLoi);
            model.addAttribute("cart", danhSachThanhToan);  // hiển thị sản phẩm da chon
            model.addAttribute("tongTamTinh", tongTamTinh);
            model.addAttribute("khachHang", khachHang);
            model.addAttribute("hoaDonBinhRequest", hoaDonBinhRequest);
            return "shop/checkout";
        }

        Voucher voucher = null;
        float giamGia = 0f;

        if (hoaDonBinhRequest.getIdVoucher() != null) {
            voucher = voucherRepository.findById(hoaDonBinhRequest.getIdVoucher()).orElse(null);
            System.out.println(voucher);
            if (voucher != null && voucher.getSoLuong() > 0) {
                giamGia = voucherService.tinhGiamGia(tongTamTinh, voucher);

                voucher.setSoLuong(voucher.getSoLuong() - 1);
                voucherRepository.save(voucher);
            }
        }

        float phiShip = 0f;
        if(tongTamTinh <= 1000000) {
             phiShip = 30000.0f;
        }

        // Tạo Hóa Đơn
        HoaDon hoaDon = HoaDon.builder()
                .tenNguoiNhan(khachHang != null ? khachHang.getTenKhachHang() : hoaDonBinhRequest.getTenNguoiNhan())
                .sdtNguoiNhan(khachHang != null ? khachHang.getTaiKhoan().getSdt() : hoaDonBinhRequest.getSdtNguoiNhan())
                .email(khachHang != null ? khachHang.getTaiKhoan().getEmail() : hoaDonBinhRequest.getEmail())
                .tinh(khachHang != null && diaChiMacDinh != null ? diaChiMacDinh.getTinh() : hoaDonBinhRequest.getTinh())
                .quan(khachHang != null && diaChiMacDinh != null ? diaChiMacDinh.getHuyen() : hoaDonBinhRequest.getQuan())
                .xa(khachHang != null && diaChiMacDinh != null ? diaChiMacDinh.getXa() : hoaDonBinhRequest.getXa())
                .diaChiNguoiNhan(khachHang != null && diaChiMacDinh != null ? diaChiMacDinh.getDiaChiCuThe() : hoaDonBinhRequest.getDiaChiNguoiNhan())
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
                .nhanVien(null)
                .giamGia(giamGia)
                .thanhTien(tongTamTinh - giamGia + phiShip)
                .voucher(voucher) // nếu entity HoaDon có quan hệ @ManyToOne với Voucher
                .build();

        hoaDonService.saveHoaDon(hoaDon);
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Đặt hàng").hoaDon(hoaDon).build());
        lichSuHoaDonRepository.save(LichSuHoaDon.builder().trangThai("Chờ xác nhận").hoaDon(hoaDon).build());

        // Lưu chi tiết hóa đơn và cập nhật tồn kho
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();
        for (GioHangChiTiet item : danhSachThanhToan) {
            SanPhamChiTiet sanPham = sanPhamChiTietRepository.findById(item.getSanPhamChiTiet().getId()).orElseThrow();

            sanPham.setSoLuong(sanPham.getSoLuong() - item.getSoLuong());
            sanPhamChiTietRepository.save(sanPham);

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

        // Gửi email
        try {
            mailService.sendOrderConfirmation(hoaDon);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        for (Integer id : selectedIds) {
            gioHangService.xoaSanPhamKhoiGioHang(id);
        }

        // Xóa session selectedIds sau khi đặt hàng xong
        session.removeAttribute("selectedIds");

        model.addAttribute("successMessage", "Đặt hàng thành công. Vui lòng kiểm tra email. Chúng tôi sẽ liên hệ với bạn qua số điện thoại để xác nhận đơn hàng.");
        model.addAttribute("cart", new ArrayList<>());
        model.addAttribute("tongTamTinh", 0);
        model.addAttribute("hoaDonBinhRequest", new HoaDonBinhRequest());
        model.addAttribute("khachHang", khachHang);
        return "shop/checkout";
    }

}
