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

@RequestMapping("/admin")
@Controller
public class DeGiayController {
    @Autowired
    private DeGiayRepository deGiayRepository;

    @Autowired
    private DeGiayImp deGiayImp;

    @GetMapping("/de-giay")
    public String listDeGiay(Model model, @ModelAttribute("degiay") DeGiay deGiay, @ModelAttribute("tim") ThuocTinhInfo info) {
        List<DeGiay> list;
        String trimmedKey = (info.getKey() != null) ? info.getKey().trim().replaceAll("\\s+", " ") : null;
        boolean isKeyEmpty = (trimmedKey == null || trimmedKey.isEmpty());
        boolean isTrangThaiNull = (info.getTrangThai() == null);

        if (isKeyEmpty && isTrangThaiNull) {
            list = deGiayRepository.findAllByOrderByNgayTaoDesc();
        } else {
            list = deGiayRepository.findByTenAndTrangThai("%" + trimmedKey + "%", info.getTrangThai());
        }

        List<DeGiay> listAll = deGiayRepository.findAll();
        model.addAttribute("listAll", listAll);
        model.addAttribute("list", list);
        model.addAttribute("fillSearch", trimmedKey);
        model.addAttribute("fillTrangThai", info.getTrangThai());

        return "admin/qldegiay";
    }

    @PostMapping("/degiay/updateTrangThai/{id}")
    public String updateTrangThaiDeGiay(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        DeGiay existingDeGiay = deGiayRepository.findById(id).orElse(null);
        if (existingDeGiay != null) {
            existingDeGiay.setTrangThai(!existingDeGiay.getTrangThai());
            deGiayRepository.save(existingDeGiay);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/admin/de-giay";
    }

    @PostMapping("/addSave")
    public String addSave(@ModelAttribute("degiay") DeGiay deGiay) {
        String trimmedTenDeGiay = (deGiay.getTen() != null) ? deGiay.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();

        deGiay.setTen(trimmedTenDeGiay);
        deGiay.setTrangThai(true);
        deGiay.setNgayTao(currentTime);
        deGiay.setNgayCapNhat(currentTime);
        deGiayImp.add(deGiay);

        return "redirect:/admin/de-giay";
    }

    @PostMapping("/addDeGiayModal")
    public String addDeGiayModal(@ModelAttribute("degiay") DeGiay deGiay) {
        return processDeGiay(deGiay, "redirect:/viewaddSPGET");
    }

    @PostMapping("/addDeGiaySua")
    public String addDeGiaySua(@ModelAttribute("degiay") DeGiay deGiay, @RequestParam("spctId") Integer spctId) {
        return processDeGiay(deGiay, "redirect:/updateCTSP/" + spctId);
    }

    @PostMapping("/addDeGiaySuaAll")
    public String addDeGiaySuaAll(@ModelAttribute("degiay") DeGiay deGiay, @RequestParam("spctId") Integer spctId) {
        return processDeGiay(deGiay, "redirect:/updateAllCTSP/" + spctId);
    }

    private String processDeGiay(DeGiay deGiay, String redirectUrl) {
        String trimmedTenDeGiay = (deGiay.getTen() != null) ? deGiay.getTen().trim().replaceAll("\\s+", " ") : null;
        LocalDateTime currentTime = LocalDateTime.now();

        deGiay.setTen(trimmedTenDeGiay);
        deGiay.setTrangThai(true);
        deGiay.setNgayTao(currentTime);
        deGiay.setNgayCapNhat(currentTime);
        deGiayImp.add(deGiay);

        return redirectUrl;
    }

    @ModelAttribute("dsdg")
    public List<DeGiay> getDanhSachDeGiay() {
        return deGiayImp.findAll();
    }
}