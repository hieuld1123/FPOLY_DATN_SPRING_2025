package com.example.datnsd26.controller.thuonghieu;

import com.example.datnsd26.models.ThuongHieu;
import com.example.datnsd26.repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ThuongHieuRestController {
    @Autowired
    ThuongHieuRepository thuongHieuRepository;

    @GetMapping("/updateThuongHieu/{id}")
    public ResponseEntity<ThuongHieu> getThuongHieu(@PathVariable Integer id) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id).orElse(null);
        return ResponseEntity.ok(thuongHieu);
    }

    @PutMapping("/api/updateThuongHieu/{id}")
    public ResponseEntity<String> updateDeGiay(@PathVariable Integer id, @RequestBody ThuongHieu updatedThuongHieu) {
        ThuongHieu existingThuongHieu = thuongHieuRepository.findById(id).orElse(null);
        if (updatedThuongHieu == null) {
            return ResponseEntity.notFound().build();
        }
        String trimmedTenThuongHieu = (updatedThuongHieu.getTen() != null)
                ? updatedThuongHieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        existingThuongHieu.setTen(trimmedTenThuongHieu);
        thuongHieuRepository.save(existingThuongHieu);
        return ResponseEntity.ok("redirect:/listthuonghieu");
    }

    @GetMapping("/checkTenThuongHieu")
    public ResponseEntity<Boolean> checkTenThuongHieu(@RequestParam String ten) {
        boolean exists = thuongHieuRepository.existsByTen(ten);
        return ResponseEntity.ok(exists);
    }

}
