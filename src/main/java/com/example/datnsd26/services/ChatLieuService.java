package com.example.datnsd26.services;

import com.example.datnsd26.models.ChatLieu;

import java.util.List;

public interface ChatLieuService {
    List<ChatLieu> findAll();
    ChatLieu add(ChatLieu chatLieu);

    List<ChatLieu> findByTen(String ten, Boolean trangthai);

    void deleteById(int id);

    ChatLieu getById(int id);
}
