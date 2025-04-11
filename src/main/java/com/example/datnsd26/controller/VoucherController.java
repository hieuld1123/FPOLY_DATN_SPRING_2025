package com.example.datnsd26.controller;

import com.example.datnsd26.models.Voucher;
import com.example.datnsd26.services.VoucherSchedulerService;
import com.example.datnsd26.services.VoucherService;
import com.example.datnsd26.services.VoucherValidationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
@Slf4j
public class VoucherController {

    private final VoucherService voucherService;

    @Autowired
    private VoucherSchedulerService voucherSchedulerService;


    @GetMapping
    public String listVouchers(Model model, @RequestParam(defaultValue = "0") int page) {
        int pageSize = 5; // Số lượng voucher trên mỗi trang
        Page<Voucher> vouchers = voucherService.getAllVouchers(PageRequest.of(page, pageSize));
        model.addAttribute("vouchers", vouchers);
        return "/voucher/user/voucher-list";
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "/voucher/user/voucher-add";
    }

    @PostMapping("/create")
    public String createVoucher(@ModelAttribute Voucher voucher, Model model) {
        try {
            voucherService.createVoucher(voucher);
            return "redirect:/admin/vouchers";
        } catch (VoucherValidationException e) {
            model.addAttribute("voucher", voucher);
            model.addAttribute("errors", e.getErrors()); // Gửi lỗi xuống giao diện
            return "/voucher/user/voucher-add";
        }
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Voucher voucher = voucherService.getVoucherById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher không tồn tại"));
        model.addAttribute("voucher", voucher);
        return "/voucher/user/voucher-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateVoucher(@PathVariable Long id, @ModelAttribute Voucher voucher, Model model) {
        try {
            voucherService.updateVoucher(id, voucher);
            return "redirect:/admin/vouchers";
        } catch (VoucherValidationException e) {
            model.addAttribute("voucher", voucher);
            model.addAttribute("errors", e.getErrors()); // Gửi lỗi xuống giao diện
            return "/voucher/user/voucher-edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Long id, Model model) {
        try {
            voucherService.deleteVoucher(id);
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không thể xóa voucher: " + e.getMessage());
            return "redirect:/admin/vouchers";
        }
    }


    @GetMapping("/statuses")
    @ResponseBody
    public List<Map<String, Serializable>> getVoucherStatuses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> pageData = voucherService.getAllVouchers(pageable);
        return pageData.stream().map(v -> Map.of(
                "id", (Serializable) v.getId(),
                "trangThai", v.getTrangThai()
        )).toList();
    }

    @GetMapping("/search")
    public String searchVouchers(
            @RequestParam(required = false) String maVoucher,
            @RequestParam(required = false) String tenVoucher,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayBatDau,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayKetThuc,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        try {
            int pageSize = 5;
            Page<Voucher> results = voucherService.searchVouchers(
                    maVoucher,
                    tenVoucher,
                    trangThai,
                    ngayBatDau,
                    ngayKetThuc,
                    PageRequest.of(page, pageSize)
            );

            // Add search parameters to model
            model.addAttribute("maVoucher", maVoucher);
            model.addAttribute("tenVoucher", tenVoucher);
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("ngayBatDau", ngayBatDau);
            model.addAttribute("ngayKetThuc", ngayKetThuc);
            model.addAttribute("vouchers", results);

            return "/voucher/user/voucher-list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tìm kiếm: " + e.getMessage());
            return "redirect:/admin/vouchers";
        }
    }
}
