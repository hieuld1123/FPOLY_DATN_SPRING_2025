package com.example.datnsd26.controller.mausac;

import com.example.datnsd26.models.MauSac;
import com.example.datnsd26.info.ThuocTinhInfo;
import com.example.datnsd26.repository.MauSacRepository;
import com.example.datnsd26.services.impl.MauSacImp;
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
public class MauSacController {
    @Autowired
    MauSacImp mauSacImp;

    @Autowired
    MauSacRepository mauSacRepository;

    @GetMapping("/mau-sac")
    public String listMauSac(@RequestParam(defaultValue = "0") int p, @ModelAttribute("mausac") MauSac mauSac, @ModelAttribute("tim") ThuocTinhInfo info, Model model) {
        List<MauSac> page;
        String trimmedKey = (info.getKey() != null) ? info.getKey().trim().replaceAll("\\s+", " ") : null;
        boolean isKeyEmpty = (trimmedKey == null || trimmedKey.isEmpty());
        boolean isTrangThaiNull = (info.getTrangThai() == null);

        if (isKeyEmpty && isTrangThaiNull) {
            page = mauSacRepository.findAllByOrderByNgayTaoDesc();
        } else {
            page = mauSacRepository.findByTenAndTrangThai("%" + trimmedKey + "%", info.getTrangThai());
        }
        model.addAttribute("fillSearch", info.getKey());
        model.addAttribute("fillTrangThai", info.getTrangThai());
        model.addAttribute("list", page);
        return "admin/qlmausac";
    }

    @PostMapping("/mausac/updateTrangThai/{id}")
    public String updateTrangThaiMauSac(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        MauSac existingMauSac = mauSacRepository.findById(id).orElse(null);
        if (existingMauSac != null) {
            existingMauSac.setTrangThai(!existingMauSac.getTrangThai());
            mauSacRepository.save(existingMauSac);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/admin/mau-sac";
    }

    @PostMapping("/addSaveMauSac")
    @CacheEvict(value = "mausacCache", allEntries = true)
    public String addSave(@ModelAttribute("mausac") MauSac mauSac, @ModelAttribute("ms") ThuocTinhInfo info, Model model, HttpSession session) {
        String trimmedTenMauSac = (mauSac.getTen() != null) ? mauSac.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();
        mauSac.setTen(trimmedTenMauSac);
        mauSac.setTenMauSac(mauSac.getTenMauSac());
        mauSac.setTrangThai(true);
        mauSac.setNgayTao(currentTime);
        mauSac.setNgayCapNhat(currentTime);
        mauSacRepository.save(mauSac);
        return "redirect:/admin/mau-sac";
    }

    @PostMapping("/addMauSacModal")
    public String addMauSacModal(@ModelAttribute("mausac") MauSac mauSac, HttpSession session) {
        String trimmedTenMauSac = (mauSac.getTen() != null) ? mauSac.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();
        mauSac.setTen(trimmedTenMauSac);
        mauSac.setTenMauSac(mauSac.getTenMauSac());
        mauSac.setTrangThai(true);
        mauSac.setNgayTao(currentTime);
        mauSac.setNgayCapNhat(currentTime);
        mauSacImp.addMauSac(mauSac);
        return "redirect:/viewaddSPGET";
    }

    @PostMapping("/addMauSacSua")
    public String addMauSacSua(@ModelAttribute("mausac") MauSac mauSac, @RequestParam("spctId") Integer spctId, HttpSession session) {
        String trimmedTenMauSac = (mauSac.getTen() != null) ? mauSac.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();
        mauSac.setTen(trimmedTenMauSac);
        mauSac.setTrangThai(true);
        mauSac.setNgayTao(currentTime);
        mauSac.setNgayCapNhat(currentTime);
        mauSacImp.addMauSac(mauSac);
        return "redirect:/updateCTSP/" + spctId;
    }

    @PostMapping("/addMauSacSuaAll")
    public String addMauSacSuaAll(@ModelAttribute("mausac") MauSac mauSac, @RequestParam("spctId") Integer spctId, HttpSession session) {
        String trimmedTenMauSac = (mauSac.getTen() != null) ? mauSac.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();
        mauSac.setTen(trimmedTenMauSac);
        mauSac.setTrangThai(true);
        mauSac.setNgayTao(currentTime);
        mauSac.setNgayCapNhat(currentTime);
        mauSacImp.addMauSac(mauSac);
        return "redirect:/updateAllCTSP/" + spctId;
    }
}
