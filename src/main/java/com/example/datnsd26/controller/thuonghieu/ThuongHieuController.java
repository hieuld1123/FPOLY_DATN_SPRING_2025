package com.example.datnsd26.controller.thuonghieu;

import com.example.datnsd26.models.ThuongHieu;
import com.example.datnsd26.info.ThuocTinhInfo;
import com.example.datnsd26.repository.ThuongHieuRepository;
import com.example.datnsd26.services.impl.ThuongHieuImp;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ThuongHieuController {
    @Autowired
    ThuongHieuRepository thuongHieuRepository;

    @Autowired
    ThuongHieuImp thuongHieuImp;

    @GetMapping("/listthuonghieu")
    public String hienthi(@RequestParam(defaultValue = "0") int p, Model model, @ModelAttribute("thuonghieu") ThuongHieu thuongHieu,@ModelAttribute("tim") ThuocTinhInfo info) {
        List<ThuongHieu> list;
        String trimmedKey = (info.getKey() != null) ? info.getKey().trim().replaceAll("\\s+", " ") : null;
        boolean isKeyEmpty = (trimmedKey == null || trimmedKey.isEmpty());
        boolean isTrangthaiNull = (info.getTrangThai() == null);
        if (isKeyEmpty && isTrangthaiNull) {
            list = thuongHieuRepository.findAllByOrderByNgayTaoDesc();
        } else {
            list = thuongHieuRepository.findByTenAndTrangThai("%" + trimmedKey + "%", info.getTrangThai());
        }
        model.addAttribute("page", list);
        model.addAttribute("fillSearch", info.getKey());
        model.addAttribute("fillTrangThai", info.getTrangThai());
        return "admin/qlthuonghieu";
    }

    @PostMapping("/thuonghieu/updateTrangThai/{id}")
    public String updateTrangThaiThuongHieu(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        ThuongHieu existingThuongHieu = thuongHieuRepository.findById(id).orElse(null);
        if (existingThuongHieu != null) {
            existingThuongHieu.setTrangThai(!existingThuongHieu.getTrangThai());
            thuongHieuRepository.save(existingThuongHieu);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/listthuonghieu";
    }

    @PostMapping("/addSaveThuongHieu")
    @CacheEvict(value = "thuonghieuCache", allEntries = true)
    public String addSave(@ModelAttribute("thuonghieu") ThuongHieu thuongHieu, @ModelAttribute("th") ThuocTinhInfo info, Model model, HttpSession session) {

        if (thuongHieuRepository.existsByTen(thuongHieu.getTen())) {
            model.addAttribute("errThuongHieu", "Tên thương hiệu trùng!");
            return "admin/qlthuonghieu";
        }
        String trimmedTenThuongHieu = (thuongHieu.getTen() != null)
                ? thuongHieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        thuongHieu.setTen(trimmedTenThuongHieu);
        thuongHieu.setTrangThai(true);
        thuongHieu.setNgayTao(currentTime);
        thuongHieu.setNgayCapNhat(currentTime);
        thuongHieuImp.add(thuongHieu);
        return "redirect:/listthuonghieu";
    }


    @PostMapping("/addThuongHieuModal")
    public String addThuongHieuModal(@ModelAttribute("thuonghieu") ThuongHieu thuongHieu, @ModelAttribute("th") ThuocTinhInfo info,HttpSession session) {

        String trimmedTenThuongHieu = (thuongHieu.getTen() != null)
                ? thuongHieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        thuongHieu.setTen(trimmedTenThuongHieu);
        thuongHieu.setTrangThai(true);
        thuongHieu.setNgayTao(currentTime);
        thuongHieu.setNgayCapNhat(currentTime);
        thuongHieuImp.add(thuongHieu);
        return "redirect:/viewaddSPGET";
    }

    @PostMapping("/addThuongHieuSua")
    public String addThuongHieuSua(@ModelAttribute("thuonghieu") ThuongHieu thuongHieu, @ModelAttribute("th") ThuocTinhInfo info, @RequestParam("spctId") Integer spctId, HttpSession session) {

        String trimmedTenThuongHieu = (thuongHieu.getTen() != null)
                ? thuongHieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        thuongHieu.setTen(trimmedTenThuongHieu);
        thuongHieu.setTrangThai(true);
        thuongHieu.setNgayTao(currentTime);
        thuongHieu.setNgayCapNhat(currentTime);
        thuongHieuImp.add(thuongHieu);
        return "redirect:/updateCTSP/" + spctId;
    }

    @PostMapping("/addThuongHieuSuaAll")
    public String addThuongHieuSuaAll(@ModelAttribute("thuonghieu") ThuongHieu thuongHieu, @ModelAttribute("th") ThuocTinhInfo info, @RequestParam("spctId") Integer spctId, HttpSession session) {

        String trimmedTenThuongHieu = (thuongHieu.getTen() != null)
                ? thuongHieu.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        thuongHieu.setTen(trimmedTenThuongHieu);
        thuongHieu.setTrangThai(true);
        thuongHieu.setNgayTao(currentTime);
        thuongHieu.setNgayCapNhat(currentTime);
        thuongHieuImp.add(thuongHieu);
        return "redirect:/updateAllCTSP/" + spctId;
    }
}
