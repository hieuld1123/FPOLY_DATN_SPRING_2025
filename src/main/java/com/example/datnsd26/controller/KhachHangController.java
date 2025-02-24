package com.example.datnsd26.controller;

import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.services.DiaChiService;
import com.example.datnsd26.services.KhachHangService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class KhachHangController {
    @Autowired
    private KhachHangService khachHangService;

    @Autowired
    private DiaChiService diaChiService;

    @GetMapping("/khach-hang/hien-thi")
    public String hienThiKhachHang(Model model) {
        model.addAttribute("listKH", khachHangService.getAll());
        return "/admin/khachhang/quanlykhachhang";
    }

    @GetMapping("/khach-hang/them")
    public String hienThithemKhachHang(Model model) {
//        model.addAttribute("taikhoan",taiKhoanService.getAll());
        model.addAttribute("diachi",diaChiService.getAll());
        model.addAttribute("khachhang", new KhachHang());
        return "/admin/khachhang/themkhachhang";
    }


}
