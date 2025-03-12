//package com.example.datnsd26.controller;
//
//import com.example.datnsd26.repository.HoaDonChiTietRepository;
//import com.example.datnsd26.repository.HoaDonRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Controller
//public class HoaDonController {
//
//    @Autowired
//    private HoaDonRepository hoaDonRepository;
//
//    @Autowired
//    private HoaDonChiTietRepository hoaDonChiTietRepository;
//
//    @GetMapping("/hoa-don/tat-ca-hoa-don")
//    public String hoaDonTatCaHoaDon(Model model) {
//        model.addAttribute("hoaDonList", hoaDonRepository.findAll(Sort.by(Sort.Direction.DESC, "ngayTao")));
//        return "/admin/hoa-don/hoa-don-all";
//    }
//
//    @GetMapping("/all-user")
//    public String allUser(Model model) {
//        return "/admin/user/user-all";
//    }
//
//    @GetMapping("/view-user")
//    public String viewUser(Model model) {
//        return "/admin/user/view-update-user";
//    }
//
//}
