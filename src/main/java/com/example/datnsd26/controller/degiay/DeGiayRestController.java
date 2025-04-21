package com.example.datnsd26.controller.degiay;

import com.example.datnsd26.models.DeGiay;
import com.example.datnsd26.repository.DeGiayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DeGiayRestController {
    @Autowired
    DeGiayRepository deGiayRepository;

    @GetMapping("/updateDeGiay/{id}")
    public ResponseEntity<DeGiay> getDeGiay(@PathVariable Integer id) {
        DeGiay degiay = deGiayRepository.findById(id).orElse(null);
        return ResponseEntity.ok(degiay);
    }

    @PutMapping("/api/updateDeGiay/{id}")
    public ResponseEntity<String> updateDeGiay(@PathVariable Integer id, @RequestBody DeGiay updatedDeGiay) {
        DeGiay existingDeGiay = deGiayRepository.findById(id).orElse(null);
        if (existingDeGiay == null) {
            return ResponseEntity.notFound().build();
        }
        String trimmedTenDeGiay = (updatedDeGiay.getTen() != null)
                ? updatedDeGiay.getTen().trim().replaceAll("\\s+", " ")
                : null;
        existingDeGiay.setTen(trimmedTenDeGiay);
        deGiayRepository.save(existingDeGiay);
        return ResponseEntity.ok("redirect:/admin/de-giay");
    }

    @GetMapping("/checkTenDeGiay")
    public ResponseEntity<Boolean> checkTenDeGiay(@RequestParam String ten) {
        boolean exists = deGiayRepository.existsByTen(ten);
        return ResponseEntity.ok(exists);
    }

}
