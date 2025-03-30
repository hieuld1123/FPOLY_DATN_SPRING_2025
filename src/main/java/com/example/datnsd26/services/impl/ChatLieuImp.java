package com.example.datnsd26.services.impl;

import com.example.datnsd26.models.ChatLieu;
import com.example.datnsd26.repository.ChatLieuRepository;
import com.example.datnsd26.services.ChatLieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatLieuImp implements ChatLieuService {
    @Autowired
    ChatLieuRepository chatLieuRepository;

    @Override
    public List<ChatLieu> findAll() {
        return chatLieuRepository.findAll();
    }

    @Override
    public ChatLieu add(ChatLieu chatLieu) {
       return chatLieuRepository.save(chatLieu);
    }

    @Override
    public List<ChatLieu> findByTen(String ten, Boolean trangthai) {
        return chatLieuRepository.findByTenVaTrangThai(ten, trangthai);
    }

    @Override
    public void deleteById(int id) {
        chatLieuRepository.deleteById(id);
    }

    @Override
    public ChatLieu getById(int id) {
        return chatLieuRepository.getReferenceById(id);
    }

}
