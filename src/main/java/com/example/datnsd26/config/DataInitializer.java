package com.example.datnsd26.config;

import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final TaiKhoanRepository taiKhoanRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Kiểm tra nếu admin chưa tồn tại thì thêm
        if (taiKhoanRepository.findByEmail("admin@example.com").isEmpty()) {
            TaiKhoan admin = new TaiKhoan();
            admin.setEmail("admin@example.com");
            admin.setMatKhau(passwordEncoder.encode("Abc@123"));  // Hash mật khẩu
            admin.setTrangThai(true); // Active
            admin.setVaiTro(TaiKhoan.Role.ADMIN);
            taiKhoanRepository.save(admin);

            System.out.println("✅ Admin account created: admin@example.com / Abc@123");
        }

    }
}
