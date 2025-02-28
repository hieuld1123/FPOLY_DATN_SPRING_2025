package com.example.datnsd26.controller;

import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class LoginController {
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final TaiKhoanRepository taiKhoanRepository;

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
    public String loginPage() {
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
