package com.example.datnsd26.controller;

import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.models.LichSuHoaDon;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.HoaDonRepository;
import com.example.datnsd26.repository.LichSuHoaDonRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class LoginController {
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final HoaDonRepository hoaDonRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;

//    @GetMapping("/shop")
//    public String shopPage(Model model) {
//        model.addAttribute("productDetailsList", sanPhamChiTietRepository.findAll());
//        return "/shop/shop";
//    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "admin";
    }

    @GetMapping("/admin/get-all-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String userManagePage(Model model) {
        List<TaiKhoan> users = taiKhoanRepository.findAll();
        model.addAttribute("users", users);
        return "/admin/user/manage-user";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không đúng!");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Đăng xuất thành công!");
        }

        return "login";
    }

    @GetMapping("/debug-password")
    @ResponseBody
    public String debugPassword() {
        return taiKhoanRepository.findByEmail("admin@example.com")
                .map(user -> "Stored Hashed Password: " + user.getMatKhau())
                .orElse("User not found");
    }

}
