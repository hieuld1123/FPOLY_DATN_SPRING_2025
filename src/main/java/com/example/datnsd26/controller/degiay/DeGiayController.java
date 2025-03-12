package com.example.datnsd26.controller.degiay;

import com.example.datnsd26.info.ThuocTinhInfo;
import com.example.datnsd26.models.DeGiay;
import com.example.datnsd26.repository.DeGiayRepository;
import com.example.datnsd26.services.impl.DeGiayImp;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class DeGiayController {
    @Autowired
    DeGiayRepository deGiayRepository;
    @Autowired
    DeGiayImp deGiayImp;


    @GetMapping("/listdegiay")
    public String listdegiay(Model model, @ModelAttribute("degiay") DeGiay deGiay, @ModelAttribute("tim") ThuocTinhInfo info) {
        List<DeGiay> list;
        String trimmedKey = (info.getKey() != null) ? info.getKey().trim().replaceAll("\\s+", " ") : null;
        boolean isKeyEmpty = (trimmedKey == null || trimmedKey.isEmpty());
        boolean isTrangthaiNull = (info.getTrangthai() == null);
        if (isKeyEmpty && isTrangthaiNull) {
            list = deGiayRepository.findAllByOrderByNgaytaoDesc();
        } else {
            list = deGiayRepository.findByTenAndTrangthai("%" + trimmedKey + "%", info.getTrangthai());
        }
        List<DeGiay> listAll = deGiayRepository.findAll();
        model.addAttribute("listAll", listAll);
        //dùng listAll để validate
        model.addAttribute("list", list);
        model.addAttribute("fillSearch", trimmedKey);
        model.addAttribute("fillTrangThai", info.getTrangthai());
        return "admin/qldegiay";
    }

    @PostMapping("/degiay/updateTrangThai/{id}")
    public String updateTrangThaiDeGiay(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        DeGiay existingDeGiay = deGiayRepository.findById(id).orElse(null);
        if (existingDeGiay != null) {
            existingDeGiay.setTrangthai(!existingDeGiay.getTrangthai());
            deGiayRepository.save(existingDeGiay);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/listdegiay";
    }


    @PostMapping("/addSave")
    public String addSave(@ModelAttribute("degiay") DeGiay deGiay, HttpSession session) {

        String trimmedTenDeGiay = (deGiay.getTen() != null)
                ? deGiay.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        deGiay.setTen(trimmedTenDeGiay);
        deGiay.setTrangthai(true);
        deGiay.setNgaytao(currentTime);
        deGiay.setNgaycapnhat(currentTime);
        deGiayImp.add(deGiay);
        return "redirect:/listdegiay";
    }


    @PostMapping("/addDeGiayModal")
    public String addDeGiayModal(@ModelAttribute("degiay") DeGiay deGiay, HttpSession session) {

        String trimmedTenDeGiay = (deGiay.getTen() != null)
                ? deGiay.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        deGiay.setTen(trimmedTenDeGiay);
        deGiay.setTrangthai(true);
        deGiay.setNgaytao(currentTime);
        deGiay.setNgaycapnhat(currentTime);
        deGiayImp.add(deGiay);
        return "redirect:/viewaddSPGET";
    }

    @PostMapping("/addDeGiaySua")
    public String addDeGiaySua(@ModelAttribute("degiay") DeGiay deGiay, @RequestParam("spctId") Integer spctId, HttpSession session) {

        String trimmedTenDeGiay = (deGiay.getTen() != null)
                ? deGiay.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        deGiay.setTen(trimmedTenDeGiay);
        deGiay.setTrangthai(true);
        deGiay.setNgaytao(currentTime);
        deGiay.setNgaycapnhat(currentTime);
        deGiayImp.add(deGiay);
        return "redirect:/updateCTSP/" + spctId;
    }

    @PostMapping("/addDeGiaySuaAll")
    public String addDeGiaySuaAll(@ModelAttribute("degiay") DeGiay deGiay, @RequestParam("spctId") Integer spctId, HttpSession session) {

        String trimmedTenDeGiay = (deGiay.getTen() != null)
                ? deGiay.getTen().trim().replaceAll("\\s+", " ")
                : null;
        LocalDateTime currentTime = LocalDateTime.now();
        deGiay.setTen(trimmedTenDeGiay);
        deGiay.setTrangthai(true);
        deGiay.setNgaytao(currentTime);
        deGiay.setNgaycapnhat(currentTime);
        deGiayImp.add(deGiay);
        return "redirect:/updateAllCTSP/" + spctId;
    }

    @ModelAttribute("dsdg")
    public List<DeGiay> getDS() {
        return deGiayImp.findAll();
    }
}
