package com.example.datnsd26.Api;


import com.example.datnsd26.Entity.TaiKhoan;
import com.example.datnsd26.Respository.TaiKhoanRespository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaiKhoanController {
    @Autowired
    TaiKhoanRespository taiKhoanRespository;

    @GetMapping("/hien-thi/tai-khoan")
    public ResponseEntity<?> hienThiTaiKhoan() {
        return ResponseEntity.ok(taiKhoanRespository.findAll());
    }

    @PostMapping("/them/tai-khoan")
    public ResponseEntity<?> themTaiKhoan(@RequestBody @Valid TaiKhoan taiKhoan) {
        return ResponseEntity.ok(taiKhoanRespository.save(taiKhoan));
    }

    @PostMapping("/sua/tai-khoan")
    public ResponseEntity<?> suaTaiKhoan(@RequestBody @Valid TaiKhoan taiKhoan) {
        return ResponseEntity.ok(taiKhoanRespository.save(taiKhoan));
    }

//    @GetMapping("/xoa/tai-khoan")
//    public String xoaTaiKhoan(@RequestParam("id") Integer id) {
//        taiKhoanRespository.deleteById(id);
//        return "Xóa thành công";
//    }
}
