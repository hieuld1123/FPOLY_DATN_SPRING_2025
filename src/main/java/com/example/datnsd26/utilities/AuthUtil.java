package com.example.datnsd26.utilities;

import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final NhanVienRepository nhanVienRepository;

    public NhanVien getNhanVien(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return nhanVienRepository.findByEmail(email).orElse(null);
    }
}
