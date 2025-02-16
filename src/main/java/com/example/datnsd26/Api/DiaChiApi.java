package com.example.datnsd26.Api;

import com.example.datnsd26.Entity.DiaChi;
import com.example.datnsd26.Respository.DiaChiRespository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class DiaChiController {
    @Autowired
    DiaChiRespository diaChiRespository;

    @GetMapping("/hien-thi/dia-chi")
    public ResponseEntity<?> hienThiDiaChi() {
        return ResponseEntity.ok(diaChiRespository.findAll());
    }

    @PostMapping("/them/dia-chi")
    public ResponseEntity<?> themDiaChi(@RequestBody @Valid DiaChi diaChi) {
        return ResponseEntity.ok(diaChiRespository.save(diaChi));
    }

    @PostMapping("/sua/dia-chi")
    public ResponseEntity<?> suaDiaChi(@RequestBody @Valid DiaChi diaChi) {
        return ResponseEntity.ok(diaChiRespository.save(diaChi));
    }

    @GetMapping("/xoa/dia-chi")
    public String xoaDiaChi(@RequestParam("id") UUID id) {
        diaChiRespository.deleteById(id);
        return "Xóa thành công";
    }

}
