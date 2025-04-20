package com.example.datnsd26.controller.kichco;

import com.example.datnsd26.info.ThuocTinhInfo;
import com.example.datnsd26.models.KichCo;
import com.example.datnsd26.repository.KichCoRepository;
import com.example.datnsd26.services.impl.KichCoImp;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/admin")
@Controller
public class KichCoController {
    @Autowired
    private KichCoImp kichCoImp;

    @Autowired
    private KichCoRepository kichCoRepository;

    @GetMapping("/kich-co")
    public String listKichCo(Model model, @ModelAttribute("kichco") KichCo kichCo, @ModelAttribute("tim") ThuocTinhInfo info) {
        List<KichCo> list;
        String trimmedKey = (info.getKey() != null) ? info.getKey().trim().replaceAll("\\s+", " ") : null;
        boolean isKeyEmpty = (trimmedKey == null || trimmedKey.isEmpty());
        boolean isTrangThaiNull = (info.getTrangThai() == null);

        if (isKeyEmpty && isTrangThaiNull) {
            list = kichCoRepository.findAllByOrderByNgayTaoDesc();
        } else {
            list = kichCoRepository.findByTenAndTrangThai("%" + trimmedKey + "%", info.getTrangThai());
        }
        model.addAttribute("fillSearch", info.getKey());
        model.addAttribute("fillTrangThai", info.getTrangThai());
        model.addAttribute("list", list);
        return "admin/qlkichco";
    }

    @PostMapping("/kichco/updateTrangThai/{id}")
    public String updateTrangThaiKichCo(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        KichCo existingKichCo = kichCoRepository.findById(id).orElse(null);
        if (existingKichCo != null) {
            existingKichCo.setTrangThai(!existingKichCo.getTrangThai());
            kichCoRepository.save(existingKichCo);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/admin/kich-co";
    }

    @PostMapping("/updateKichCo/{id}")
    public String updateKichCo(@PathVariable Integer id) {
        kichCoRepository.updateTrangThaiToFalseById(id);
        return "redirect:/admin/kich-co";
    }

    @PostMapping("/addSaveKichCo")
    @CacheEvict(value = "kichcoCache", allEntries = true)
    public String addSave(@ModelAttribute("kichco") KichCo kichCo, Model model) {
        String trimmedTenKichCo = (kichCo.getTen() != null) ? kichCo.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();
        kichCo.setTen(trimmedTenKichCo);
        kichCo.setTrangThai(true);
        kichCo.setNgayTao(currentTime);
        kichCo.setNgayCapNhat(currentTime);
        kichCoRepository.save(kichCo);
        return "redirect:/admin/kich-co";
    }

    @PostMapping("/addKichCoModal")
    public String addKichCoModal(@ModelAttribute("kichco") KichCo kichCo) {
        String trimmedTenKichCo = (kichCo.getTen() != null) ? kichCo.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();
        kichCo.setTen(trimmedTenKichCo);
        kichCo.setTrangThai(true);
        kichCo.setNgayTao(currentTime);
        kichCo.setNgayCapNhat(currentTime);
        kichCoImp.addKichCo(kichCo);
        return "redirect:/viewaddSPGET";
    }

    @PostMapping("/addKichCoSua")
    public String addKichCoSua(@ModelAttribute("kichco") KichCo kichCo, @RequestParam("spctId") Integer spctId) {
        String trimmedTenKichCo = (kichCo.getTen() != null) ? kichCo.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();
        kichCo.setTen(trimmedTenKichCo);
        kichCo.setTrangThai(true);
        kichCo.setNgayTao(currentTime);
        kichCo.setNgayCapNhat(currentTime);
        kichCoImp.addKichCo(kichCo);
        return "redirect:/updateCTSP/" + spctId;
    }

    @PostMapping("/addKichCoSuaAll")
    public String addKichCoSuaAll(@ModelAttribute("kichco") KichCo kichCo, @RequestParam("spctId") Integer spctId) {
        String trimmedTenKichCo = (kichCo.getTen() != null) ? kichCo.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();
        kichCo.setTen(trimmedTenKichCo);
        kichCo.setTrangThai(true);
        kichCo.setNgayTao(currentTime);
        kichCo.setNgayCapNhat(currentTime);
        kichCoImp.addKichCo(kichCo);
        return "redirect:/updateAllCTSP/" + spctId;
    }
}
