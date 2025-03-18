package com.example.datnsd26.controller.chatlieu;

import com.example.datnsd26.models.ChatLieu;
import com.example.datnsd26.repository.ChatLieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatLieuRestController {
    @Autowired
    ChatLieuRepository chatLieuRepository;

    @GetMapping("/updateChatLieu/{id}")
    public ResponseEntity<ChatLieu> getChatLieu(@PathVariable Integer id) {
        ChatLieu chatLieu = chatLieuRepository.findById(id).orElse(null);
        return ResponseEntity.ok(chatLieu);
    }

    @PutMapping("/updateChatLieu/{id}")
    public ResponseEntity<String> updateChatLieu(@PathVariable Integer id, @RequestBody ChatLieu updatedChatLieu) {
        ChatLieu existingChatLieu = chatLieuRepository.findById(id).orElse(null);
        if (updatedChatLieu == null) {
            return ResponseEntity.notFound().build();
        }
        String trimmedTenChatLieu = (updatedChatLieu.getTen() != null)
                ? updatedChatLieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        existingChatLieu.setTen(trimmedTenChatLieu);
        chatLieuRepository.save(existingChatLieu);
        return ResponseEntity.ok("redirect:/chatlieu");
    }

    @GetMapping("/checkTenChatLieu")
    public ResponseEntity<Boolean> checkTenChatLieu(@RequestParam String ten) {
        boolean exists = chatLieuRepository.existsByTen(ten);
        return ResponseEntity.ok(exists);
    }

}
