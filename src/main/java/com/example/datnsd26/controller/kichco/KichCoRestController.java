package com.example.datnsd26.controller.kichco;

import com.example.datnsd26.models.KichCo;
import com.example.datnsd26.repository.KichCoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class KichCoRestController {
    @Autowired
    KichCoRepository kichCoRepository;

    @GetMapping("/updateKichCo/{id}")
    public ResponseEntity<KichCo> getKichCo(@PathVariable Integer id) {
        KichCo kichCo = kichCoRepository.findById(id).orElse(null);
        return ResponseEntity.ok(kichCo);
    }

    @PutMapping("/api/updateKichCo/{id}")
    public ResponseEntity<String> updateKichCo(@PathVariable Integer id, @RequestBody KichCo updatedKicCo) {
        KichCo existingKichCo = kichCoRepository.findById(id).orElse(null);
        if (existingKichCo == null) {
            return ResponseEntity.notFound().build();
        }
        String trimmedTenKichCo = (updatedKicCo.getTen() != null)
                ? updatedKicCo.getTen().trim().replaceAll("\\s+", " ")
                : null;
        existingKichCo.setTen(trimmedTenKichCo);
        kichCoRepository.save(existingKichCo);
        return ResponseEntity.ok("redirect:/admin/kich-co");
    }

    @GetMapping("/checkTenKichCo")
    public ResponseEntity<Boolean> checkTenKichCo(@RequestParam String ten) {
        boolean exists = kichCoRepository.existsByTen(ten);
        return ResponseEntity.ok(exists);
    }
}
