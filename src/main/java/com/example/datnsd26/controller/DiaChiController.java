package com.example.datnsd26.controller;

import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.services.DiaChiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class DiaChiController {
    @Autowired
    private DiaChiService diaChiService;

    @GetMapping("/dia-chi")
    public String shopPage() {
        return "/admin/diachi/quanlydiachi";
    }

    @GetMapping("/dia-chi/hien-thi")
    public String hienThiDiaChi(Model model) {
        model.addAttribute("listdiaChi", diaChiService.getAll());
        return "/admin/diachi/quanlydiachi";
    }

    @GetMapping("/dia-chi/them")
    public String hienThithemDiaChi(Model model) {
        model.addAttribute("diaChi", new DiaChi());
        return "/admin/diachi/themdiachi";
    }

    @PostMapping("/dia-chi/them")
    public String themDiaChi(@Valid DiaChi diaChi, BindingResult result) {
        if (result.hasErrors()) {
            return "/admin/diachi/themdiachi";
        }
        diaChiService.save(diaChi);
        return "redirect:/dia-chi/hien-thi";
    }
    @GetMapping("/dia-chi/xoa/{id}")
    public String deletePhongBan(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        diaChiService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Xóa địa chỉ thành công!");
        return "redirect:/dia-chi/hien-thi";
    }
}
