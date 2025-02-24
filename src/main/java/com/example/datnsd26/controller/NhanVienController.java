package com.example.datnsd26.controller;


import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.services.FileLoadService;
import com.example.datnsd26.services.NhanVienService;
import com.example.datnsd26.services.TaiKhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/nhan-vien")
@Controller
public class NhanVienController {
    @Autowired
    private NhanVienService nhanVienService;
    @Autowired
    private TaiKhoanService taiKhoanService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private FileLoadService fileLoadService;


    @GetMapping("/hien-thi")
    public String hienThiNhanVien(Model model) {
        List<NhanVien> listNV = nhanVienService.getAll();
        model.addAttribute("nhanVien", listNV);
        return "/admin/nhanvien/quanlynhanvien";
    }

    @GetMapping("/tim-kiem")
    public String timKiemNV(@RequestParam(value = "searchInput", required = false) String tenSdtMa,
                            @RequestParam(value = "searchOption", required = false) Boolean trangThai,
                            Model model) {
        List<NhanVien> listNV = nhanVienService.findByTenSdtMaTT(tenSdtMa, trangThai);
        model.addAttribute("nhanVien", listNV);
        model.addAttribute("searchInput", tenSdtMa);
        model.addAttribute("searchOption", trangThai);
        return "/admin/nhanvien/quanlynhanvien";
    }

    @GetMapping("/them")
    public String hienThiThemNhanVien(Model model) {
        model.addAttribute("nhanVien", nhanVienService.getAll());
        model.addAttribute("taiKhoan", taiKhoanService.getAll());
        model.addAttribute("nhanVienDto", new NhanVienTKDto());
        return "/admin/nhanvien/themnhanvien";
    }

    @PostMapping("/them")
    public String themNhanVien(@ModelAttribute("nhanVienDto") NhanVienTKDto nhanVienTKDto,
                               @RequestParam(name = "anh") MultipartFile anh) {
        String fileName = fileLoadService.uploadFile(anh);
        if (fileName != null) {
            nhanVienTKDto.setHinhAnh(fileName);
        }
//        String to = a.getEmail();
//        String subject = "Chúc mừng đã trở thành nhân viên của T&T shop";
//        String mailType = "";
//        String mailContent = "Tài khoản của bạn là: " + a.getEmail() +"\nMật khẩu của bạn là: "+ matKhau;
//        taiKhoan.sendEmail(to, subject, mailType, mailContent);
//        checkthem=1;
//        session.setAttribute("themthanhcong",checkthem);
        nhanVienService.save(nhanVienTKDto);
        return "redirect:/nhan-vien/hien-thi";
    }


    @GetMapping("/hien-thi/{id}")
    public String hienThiSuaNhanVien(@PathVariable("id") Integer id,
                                     Model model) {
        NhanVien nhanVien = nhanVienService.getById(id);
        NhanVienTKDto nhanVienTKDto = new NhanVienTKDto();
        nhanVienTKDto.setHinhAnh(nhanVien.getHinhAnh());
        nhanVienTKDto.setMaNhanVien(nhanVien.getMaNhanvien());
        nhanVienTKDto.setTenNhanVien(nhanVien.getTenNhanVien());
        nhanVienTKDto.setEmail(nhanVien.getTaiKhoan().getEmail());
        nhanVienTKDto.setId(nhanVien.getId());
        nhanVienTKDto.setDiaChiCuThe(nhanVien.getDiaChiCuThe());
        nhanVienTKDto.setGioiTinh(nhanVien.getGioiTinh());
        nhanVienTKDto.setPhuong(nhanVien.getPhuong());
        nhanVienTKDto.setQuan(nhanVien.getQuan());
        nhanVienTKDto.setTinh(nhanVien.getTinh());
        nhanVienTKDto.setCccd(nhanVien.getCccd());
        nhanVienTKDto.setHinhAnh(nhanVien.getHinhAnh());
        nhanVienTKDto.setNgaySinh(nhanVien.getNgaySinh());
        nhanVienTKDto.setNgayCapNhat(nhanVien.getNgayCapNhat());
        nhanVienTKDto.setNgayTao(nhanVien.getNgayTao());
        nhanVienTKDto.setSdt(nhanVien.getTaiKhoan().getSdt());
        nhanVienTKDto.setVaiTro(nhanVien.getTaiKhoan().getVaiTro());
        model.addAttribute("nhanVien", nhanVienTKDto);
        model.addAttribute("listNV", nhanVienService.getAll());
        model.addAttribute("listTK", taiKhoanService.getAll());
        return "/admin/nhanvien/suanhanvien";
    }

    @PostMapping("/sua/{id}")
    private String suaNhanVien(@ModelAttribute("nhanVien") NhanVienTKDto nhanVienTKDto,
                               @PathVariable Integer id,
                               @RequestParam(name = "anh") MultipartFile anh) {
        if (!anh.isEmpty()) {
            String fileName = fileLoadService.uploadFile(anh);
            nhanVienTKDto.setHinhAnh(fileName);
        }
        nhanVienService.update(nhanVienTKDto, id);
        return "redirect:/nhan-vien/hien-thi";
    }

//    @GetMapping("/xoa/{id}")
//    private String xoaNV(@PathVariable("id") Integer id){
//         nhanVienService.delete(id);
//        return "redirect:/nhan-vien/hien-thi";
//    }


}
