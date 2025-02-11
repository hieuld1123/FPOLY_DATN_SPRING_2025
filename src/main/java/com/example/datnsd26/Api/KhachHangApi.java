package com.example.datnsd26.Api;

import com.example.datnsd26.Entity.KhachHang;
import com.example.datnsd26.Respository.KhachHangRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class KhachHangController {
    @Autowired
    KhachHangRepository khachHangRepository;

    @GetMapping("/hien-thi/khach-hang")
    public ResponseEntity<?> hienThiKhachHang() {
        return ResponseEntity.ok(khachHangRepository.findAll());
    }

    @PostMapping("/them/khach-hang")
    public ResponseEntity<?> themKhachHang(@RequestBody @Valid KhachHang khachHang) {
        return ResponseEntity.ok(khachHangRepository.save(khachHang));
    }

    //    @PostMapping("/admin/sua-khach-hang")
//    public ResponseEntity<?> sua(@RequestParam("id") Integer id, @RequestBody KhachHang khachHang) {
//        return ResponseEntity.ok(khachHangRepository.save(khachHang, id));
//    }
//    @GetMapping("/xoa/khach-hang")
//    public String xoaKhachHang(@RequestParam("id") Integer id) {
//        khachHangRepository.deleteById(id);
//        return "Xóa thành công";
//    }

}
