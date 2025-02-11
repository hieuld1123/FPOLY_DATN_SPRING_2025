package com.example.datnsd26.Api;

import com.example.datnsd26.Entity.NhanVien;
import com.example.datnsd26.Respository.NhanVienRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class NhanVienController {
    @Autowired
    NhanVienRepository nhanVienRepository;


    @GetMapping("/hien-thi/nhan-vien")
    public ResponseEntity<?> hienThiNhanVien() {
        return ResponseEntity.ok(nhanVienRepository.findAll());
    }

    @PostMapping("/them/nhan-vien")
    public ResponseEntity<?> themNhanVien(@RequestBody @Valid NhanVien nhanVien) {
        return ResponseEntity.ok(nhanVienRepository.save(nhanVien));
    }

    @PostMapping("/sua/nhan-vien")
    public ResponseEntity<?> suaNhanVien(@RequestBody NhanVien nhanVien) {
        return ResponseEntity.ok(nhanVienRepository.save(nhanVien));
    }
    @GetMapping("/xoa/nhan-vien")
    public String xoaNhanVien(@RequestParam("id") Integer id) {
        nhanVienRepository.deleteById(id);
        return "Xóa thành công";
    }

}
