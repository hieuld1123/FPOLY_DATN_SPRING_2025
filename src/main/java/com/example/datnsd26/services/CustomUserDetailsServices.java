package com.example.datnsd26.services;

import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.TaiKhoanRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsServices implements UserDetailsService {
    private final TaiKhoanRepository taiKhoanRepository;

    public CustomUserDetailsServices(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại"));
        return User.withUsername(taiKhoan.getEmail())
                .password(taiKhoan.getMatKhau())  // Ensure this is the hashed password
                .roles(taiKhoan.getVaiTro().name())  // Ensure correct role format
                .disabled(!taiKhoan.getTrangThai())
                .build();


    }
}
