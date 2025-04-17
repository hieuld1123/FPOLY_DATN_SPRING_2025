package com.example.datnsd26.controller.mausac;

import com.example.datnsd26.models.MauSac;
import com.example.datnsd26.repository.MauSacRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MauSacRestController {
    @Autowired
    MauSacRepository mauSacRepository;

    @GetMapping("/updateMauSac/{id}")
    public ResponseEntity<MauSac> getMauSac(@PathVariable Integer id) {
        MauSac mauSac = mauSacRepository.findById(id).orElse(null);
        return ResponseEntity.ok(mauSac);
    }

    @PutMapping("/api/updateMauSac/{id}")
    public ResponseEntity<String> updateMauSac(@PathVariable Integer id, @RequestBody MauSac updatedMauSac) {
        MauSac existingMauSac = mauSacRepository.findById(id).orElse(null);
        if (existingMauSac == null) {
            return ResponseEntity.notFound().build();
        }
        String trimmedTenMauSac = (updatedMauSac.getTen() != null)
                ? updatedMauSac.getTen().trim().replaceAll("\\s+", " ")
                : null;
        String trimmedUpdatedTenMauSac = (updatedMauSac.getTenMauSac() != null)
                ? updatedMauSac.getTenMauSac().trim().replaceAll("\\s+", " ")
                : null;
        existingMauSac.setTenMauSac(trimmedUpdatedTenMauSac);
        existingMauSac.setTen(trimmedTenMauSac);
        mauSacRepository.save(existingMauSac);
        return ResponseEntity.ok("redirect:/listMauSac");
    }

    @GetMapping("/checkTenMauSac")
    public ResponseEntity<Boolean> checkTenMauSac(@RequestParam String ten) {
        boolean exists = mauSacRepository.existsByTen(ten);
        return ResponseEntity.ok(exists);
    }
}
