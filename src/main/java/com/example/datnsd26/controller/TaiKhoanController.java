package com.example.datnsd26.controller;

import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.services.TaiKhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class TaiKhoanController {
    @Autowired
    TaiKhoanService taiKhoanService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/doi-mat-khau")
    public String hienThiFormDoiMatKhau(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "/admin/tai-khoan/doi-mat-khau";
    }
    @PostMapping("/doi-mat-khau")
    public String doiMatKhau(@RequestParam String email,
                             @RequestParam String matKhauCu,
                             @RequestParam String matKhauMoi,
                             @RequestParam String nhapLaiMatKhauMoi,
                             RedirectAttributes redirectAttributes) {
        TaiKhoan taiKhoan = taiKhoanService.findByEmail(email);

        System.out.println("Mật khẩu nhập vào: " + matKhauCu);
        System.out.println("Mật nhập vào mã hóa: " + passwordEncoder.encode(matKhauCu));
        System.out.println("Mật khẩu trong database: " + taiKhoan.getMatKhau());
        System.out.println("Kết quả kiểm tra: " + passwordEncoder.matches(matKhauCu, taiKhoan.getMatKhau()));

        if (taiKhoan == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email không hợp lệ!");
            return "redirect:/doi-mat-khau?email=" + email;
        }

        if (!matKhauMoi.equals(nhapLaiMatKhauMoi)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không khớp!");
            return "redirect:/doi-mat-khau?email=" + email;
        }

        // Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(matKhauCu, taiKhoan.getMatKhau())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cũ không chính xác!");
            return "redirect:/doi-mat-khau?email=" + email;
        }

        // Kiểm tra mật khẩu mới có trùng mật khẩu cũ không
        if (passwordEncoder.matches(matKhauMoi, taiKhoan.getMatKhau())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không được trùng với mật khẩu cũ!");
            return "redirect:/doi-mat-khau?email=" + email;
        }

        // Cập nhật mật khẩu mới
        taiKhoan.setMatKhau(passwordEncoder.encode(matKhauMoi));
        taiKhoanService.save(taiKhoan);

        return "redirect:/nhan-vien/hien-thi";
    }
}

