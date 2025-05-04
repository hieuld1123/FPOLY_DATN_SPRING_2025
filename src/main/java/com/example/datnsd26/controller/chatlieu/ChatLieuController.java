package com.example.datnsd26.controller.chatlieu;

import com.example.datnsd26.info.ThuocTinhInfo;
import com.example.datnsd26.models.ChatLieu;
import com.example.datnsd26.repository.ChatLieuRepository;
import com.example.datnsd26.services.ChatLieuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/admin")
@Controller
public class ChatLieuController {
    @Autowired
    ChatLieuRepository chatLieuRepository;
    @Autowired
    ChatLieuService chatLieuService;

    @GetMapping("/chat-lieu")
    public String display(@ModelAttribute("search") ThuocTinhInfo info, @ModelAttribute("chatlieu") ChatLieu chatLieu, Model model) {
        List<ChatLieu> list;
        String trimmedKey = (info.getKey() != null) ? info.getKey().trim().replaceAll("\\s+", " ") : null;
        boolean isKeyEmpty = (trimmedKey == null || trimmedKey.trim().isEmpty());
        boolean isTrangThaiNull = (info.getTrangThai() == null);
        if (isKeyEmpty && isTrangThaiNull) {
            list = chatLieuRepository.findAllByOrderByNgayTaoDesc();
        } else {
            list = chatLieuRepository.findByTenAndTrangThai("%" + trimmedKey + "%", info.getTrangThai());
        }
        model.addAttribute("fillSearch", info.getKey());
        model.addAttribute("fillTrangThai", info.getTrangThai());
        model.addAttribute("lstChatLieu", list);
        return "admin/qlchatlieu";
    }

    @PostMapping("/chatlieu/updateTrangThai/{id}")
    public String updateTrangThaiChatLieu(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        ChatLieu existingChatLieu = chatLieuRepository.findById(id).orElse(null);
        if (existingChatLieu != null) {
            existingChatLieu.setTrangThai(!existingChatLieu.getTrangThai());
            chatLieuRepository.save(existingChatLieu);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/admin/chat-lieu";
    }

    @PostMapping("/updateChatLieu/{id}")
    public String updateChatLieu(@PathVariable Integer id) {
        chatLieuRepository.updateTrangThaiToFalseById(id);
        return "redirect:/admin/chat-lieu";
    }

    @PostMapping("/add")
    public String add(Model model, @ModelAttribute("chatlieu") ChatLieu chatLieu, HttpSession session) {
        String trimmedTenChatLieu = (chatLieu.getTen() != null)
                ? chatLieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        chatLieu.setTen(trimmedTenChatLieu);
        chatLieu.setTrangThai(true);
        chatLieu.setNgayTao(currentTime);
        chatLieu.setNgayCapNhat(currentTime);
        chatLieuService.add(chatLieu);
        return "redirect:/admin/chat-lieu";
    }

    @PostMapping("/addChatLieuModal")
    public String addChatLieuModal(Model model, @ModelAttribute("chatlieu") ChatLieu chatLieu, HttpSession session) {
        String trimmedTenChatLieu = (chatLieu.getTen() != null)
                ? chatLieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        chatLieu.setTen(trimmedTenChatLieu);
        chatLieu.setTrangThai(true);
        chatLieu.setNgayTao(currentTime);
        chatLieu.setNgayCapNhat(currentTime);
        chatLieuService.add(chatLieu);
        return "redirect:/admin/viewaddSPGET";
    }

    @PostMapping("/addChatLieuSua")
    public String addChatLieuSua(Model model, @ModelAttribute("chatlieu") ChatLieu chatLieu, @RequestParam("spctId") Integer spctId, HttpSession session) {
        String trimmedTenChatLieu = (chatLieu.getTen() != null)
                ? chatLieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        chatLieu.setTen(trimmedTenChatLieu);
        chatLieu.setTrangThai(true);
        chatLieu.setNgayTao(currentTime);
        chatLieu.setNgayCapNhat(currentTime);
        chatLieuService.add(chatLieu);
        return "redirect:/updateCTSP/" + spctId;
    }

    @PostMapping("/addChatLieuSuaAll")
    public String addChatLieuSuaAll(Model model, @ModelAttribute("chatlieu") ChatLieu chatLieu, @RequestParam("spctId") Integer spctId, HttpSession session) {
        String trimmedTenChatLieu = (chatLieu.getTen() != null)
                ? chatLieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        chatLieu.setTen(trimmedTenChatLieu);
        chatLieu.setTrangThai(true);
        chatLieu.setNgayTao(currentTime);
        chatLieu.setNgayCapNhat(currentTime);
        chatLieuService.add(chatLieu);
        return "redirect:/updateAllCTSP/" + spctId;
    }

    @GetMapping("/chatlieu/delete/{id}")
    public String delete(@PathVariable int id) {
        chatLieuService.deleteById(id);
        return "redirect:/admin/chat-lieu";
    }
}
