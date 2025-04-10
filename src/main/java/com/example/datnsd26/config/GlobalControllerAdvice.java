package com.example.datnsd26.config;

import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.KhachHangRepository;
import com.example.datnsd26.repository.NhanVienRepository;
import com.example.datnsd26.repository.TaiKhoanRepository;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;

    @ModelAttribute
    public void addUserInfoToModel(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            TaiKhoan tk = taiKhoanRepository.findByEmail(email).orElse(null);

            if (tk != null) {
                if (tk.getVaiTro() == TaiKhoan.Role.ADMIN || tk.getVaiTro() == TaiKhoan.Role.EMPLOYEE) {
                    NhanVien nv = nhanVienRepository.findByTaiKhoan(tk);
                    if (nv != null) {
                        model.addAttribute("currentNhanVien", nv);
                    } else {
                        model.addAttribute("currentTaiKhoan", tk); // fallback nếu cần hiện vai trò
                    }
                } else if (tk.getVaiTro() == TaiKhoan.Role.CUSTOMER) {
                    KhachHang kh = khachHangRepository.findByTaiKhoan(tk);
                    model.addAttribute("currentKhachHang", kh);


                }
            }

        }
    }
}
