package com.example.datnsd26.controller;

import com.example.datnsd26.Dto.DiaChiDTO;
import com.example.datnsd26.Dto.KhachHangDto;
import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.services.TaiKhoanService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Optional;


@Controller
public class TaiKhoanController {
    @Autowired
    TaiKhoanService taiKhoanService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/doi-mat-khau")
    public String hienThiFormDoiMatKhau(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "/tai-khoan/doi-mat-khau";
    }

    @PostMapping("/doi-mat-khau")
    public String doiMatKhau(@RequestParam String email,
                             @RequestParam String matKhauCu,
                             @RequestParam String matKhauMoi,
                             RedirectAttributes redirectAttributes) {
        TaiKhoan taiKhoan = taiKhoanService.findByEmail(email);

        System.out.println("Mật khẩu nhập vào: " + matKhauCu);
        System.out.println("Mật nhập vào mã hóa: " + passwordEncoder.encode(matKhauCu));
        System.out.println("Mật khẩu trong database: " + taiKhoan.getMatKhau());
        System.out.println("Kết quả kiểm tra: " + passwordEncoder.matches(matKhauCu, taiKhoan.getMatKhau()));

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
        redirectAttributes.addFlashAttribute("successMessage", "Vui lòng đăng nhập");

        return "redirect:/dang-nhap";
    }

    @GetMapping("/dang-nhap")
    public String dangNhap(Model model) {
        model.addAttribute("taiKhoan", new TaiKhoan());
        return "/tai-khoan/dang-nhap";
    }


    @GetMapping("user/dang-ky")
    public String dangKy(Model model) {
        KhachHangDto khachHangDto = new KhachHangDto();
        khachHangDto.setListDiaChi(new ArrayList<>());
        khachHangDto.getListDiaChi().add(new DiaChiDTO());
        model.addAttribute("taiKhoan", taiKhoanService.getAll());
        model.addAttribute("khachHangDto", khachHangDto);
        return "/tai-khoan/dang-ky";
    }

    @PostMapping("user/dang-ky")
    public String dangKy2(@ModelAttribute("khachHangDto") KhachHangDto khachHangDto) throws MessagingException {
        taiKhoanService.dangKy(khachHangDto);
        return "/tai-khoan/dang-nhap";
    }
    @GetMapping("/quen-mat-khau")
    public String hienThiQuenMatKhau() {
        return "tai-khoan/quen-mat-khau";
    }

    @PostMapping("/quen-mat-khau")
    public String xuLyQuenMatKhau(@RequestParam("email") String email, Model model) throws MessagingException {
        boolean exists = taiKhoanService.kiemTraEmailTonTai(email);
        if (!exists) {
            model.addAttribute("error", "Email không tồn tại !");
            return "tai-khoan/quen-mat-khau";
        }
        taiKhoanService.guiEmailDatLaiMatKhau(email);
        model.addAttribute("success", "Hãy kiểm tra email để đặt lại mật khẩu!");
        return "tai-khoan/quen-mat-khau";
    }

    @GetMapping("/dat-lai-mat-khau")
    public String hienThiFormDatLaiMatKhau(@RequestParam("token") String token, Model model) {
        Optional<TaiKhoan> optionalTaiKhoan = taiKhoanService.timTaiKhoanBangToken(token);
        if (optionalTaiKhoan.isPresent()) {
            model.addAttribute("token", token);
            return "tai-khoan/dat-lai-mat-khau";
        } else {
            model.addAttribute("error", "Liên kết không hợp lệ hoặc đã hết hạn!");
            return "tai-khoan/dat-lai-mat-khau";
        }
    }
    @PostMapping("/dat-lai-mat-khau")
    public String xuLyDatLaiMatKhau(@RequestParam("token") String token,
                                    @RequestParam("matKhauMoi") String matKhauMoi,
                                    Model model) {
        boolean success = taiKhoanService.datLaiMatKhau(token, matKhauMoi);
        if (success) {
            return "tai-khoan/dang-nhap";
        } else {
            model.addAttribute("error", "Liên kết không hợp lệ hoặc đã hết hạn!");
            return "tai-khoan/dat-lai-mat-khau";
        }
    }
}

