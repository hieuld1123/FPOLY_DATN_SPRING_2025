package com.example.datnsd26.controller;

import com.example.datnsd26.Dto.DiaChiDTO;
import com.example.datnsd26.Dto.KhachHangDto;
import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.HoaDonRepository;
import com.example.datnsd26.services.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class TaiKhoanController {
    @Autowired
    TaiKhoanService taiKhoanService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    HoaDonRepository hoaDonRepository;

    @Autowired
    NhanVienService nhanVienService;

    @Autowired
    KhachHangService khachHangService;
    @Autowired
    DiaChiService diaChiService;
    @Autowired
    private FileLoadService fileLoadService;

    @GetMapping("/doi-mat-khau")
    public String hienThiFormDoiMatKhau(@RequestParam("email") String email, Model model) {
        if (email == null || email.isEmpty()) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();  // Thường username là email
            }
        }
        model.addAttribute("email", email);
        return "/tai-khoan/doi-mat-khau";
    }

    @PostMapping("/doi-mat-khau")
    public String doiMatKhau(@RequestParam(value = "email", required = false) String email,
                             @RequestParam String matKhauCu,
                             @RequestParam String matKhauMoi,
                             RedirectAttributes redirectAttributes) {

        TaiKhoan taiKhoan = taiKhoanService.findByEmail(email);

        // Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(matKhauCu, taiKhoan.getMatKhau())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cũ không chính xác!");
            return "redirect:/doi-mat-khau?email=" + email;
        }

        // Kiểm tra mật khẩu mới có trùng mật khẩu cũ không
        if (passwordEncoder.matches(matKhauMoi, taiKhoan.getMatKhau())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không được trùng với mật khẩu cũ!");
            return "redirect:/doi-mat-khau?email=" + email;
        }

        // Cập nhật mật khẩu mới
        taiKhoan.setMatKhau(passwordEncoder.encode(matKhauMoi));
        taiKhoanService.save(taiKhoan);
        redirectAttributes.addFlashAttribute("successMessage", "Vui lòng đăng nhập");

        return "redirect:/login";
    }

    @GetMapping("user/dang-ky")
    public String dangKy(Model model) {
        KhachHangDto khachHangDto = new KhachHangDto();
        khachHangDto.setListDiaChi(new ArrayList<>());
        khachHangDto.getListDiaChi().add(new DiaChiDTO());
        model.addAttribute("taiKhoan", taiKhoanService.getAll());
        model.addAttribute("khachHangDto", khachHangDto);
        return "/tai-khoan/dang-ky";
    }

    @PostMapping("user/dang-ky")
    public String dangKy2(@ModelAttribute("khachHangDto") KhachHangDto khachHangDto) throws MessagingException {
        taiKhoanService.dangKy(khachHangDto);
        return "login";
    }
    @GetMapping("/quen-mat-khau")
    public String hienThiQuenMatKhau() {
        return "tai-khoan/quen-mat-khau";
    }

    @PostMapping("/quen-mat-khau")
    public String xuLyQuenMatKhau(@RequestParam("email") String email, Model model) throws MessagingException {
        boolean exists = taiKhoanService.kiemTraEmailTonTai(email);
        if (!exists) {
            model.addAttribute("error", "Email không tồn tại !");
            return "tai-khoan/quen-mat-khau";
        }
        taiKhoanService.guiEmailDatLaiMatKhau(email);
        model.addAttribute("success", "Hãy kiểm tra email để đặt lại mật khẩu!");
        return "tai-khoan/quen-mat-khau";
    }

    @GetMapping("/dat-lai-mat-khau")
    public String hienThiFormDatLaiMatKhau(@RequestParam("token") String token, Model model) {
        Optional<TaiKhoan> optionalTaiKhoan = taiKhoanService.timTaiKhoanBangToken(token);
        if (optionalTaiKhoan.isPresent()) {
            model.addAttribute("token", token);
            return "tai-khoan/dat-lai-mat-khau";
        } else {
            model.addAttribute("error", "Liên kết không hợp lệ hoặc đã hết hạn!");
            return "tai-khoan/dat-lai-mat-khau";
        }
    }
    @PostMapping("/dat-lai-mat-khau")
    public String xuLyDatLaiMatKhau(@RequestParam("token") String token,
                                    @RequestParam("matKhauMoi") String matKhauMoi,
                                    Model model) {
        boolean success = taiKhoanService.datLaiMatKhau(token, matKhauMoi);
        if (success) {
            return "login";
        } else {
            model.addAttribute("error", "Liên kết không hợp lệ hoặc đã hết hạn!");
            return "tai-khoan/dat-lai-mat-khau";
        }
    }

    @GetMapping("/403")
    public String accessDenied(@RequestParam(value = "unauthorized", required = false) String unauthorized,
                               Model model) {
        if ("true".equals(unauthorized)) {
            model.addAttribute("unauthorized", true);
        }
        return "tai-khoan/403";
    }

    @GetMapping("/khach-hang/lich-su-mua-hang")
    public String lichSuMuaHang(Model model, Principal principal) {
        String email = principal.getName();
        List<HoaDon> hoaDons = hoaDonRepository.findByKhachHang_TaiKhoan_EmailOrderByNgayTaoDesc(email);
        model.addAttribute("hoaDon", hoaDons);
        return "tai-khoan/lich-su-mua-hang";
    }
    @GetMapping("/thong-tin-ca-nhan")
    public String thongTinCaNhanKH(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        KhachHang khachHang = khachHangService.findByEmail(email);
        if (khachHang != null) {
            model.addAttribute("currentKhachHang", khachHang);
            // Lấy danh sách địa chỉ của khách hàng
            List<DiaChi> danhSachDiaChi = diaChiService.findByKhachHang(khachHang); // hoặc khachHang.getDiaChis()
            List<DiaChiDTO> listDiaChiDTO = danhSachDiaChi.stream()
                    .filter(DiaChi::getTrangThai)
                    .map(diaChi -> {
                        DiaChiDTO dto = new DiaChiDTO();
                        dto.setId(diaChi.getId());
                        dto.setTinh(diaChi.getTinh());
                        dto.setHuyen(diaChi.getHuyen());
                        dto.setXa(diaChi.getXa());
                        dto.setDiaChiCuThe(diaChi.getDiaChiCuThe());
                        dto.setTrangThai(diaChi.getTrangThai());
                        return dto;
                    }).collect(Collectors.toList());

            model.addAttribute("listDiaChi", listDiaChiDTO);
            return "tai-khoan/thong-tin-khach-hang"; //
        }

        return "redirect:/";
    }
    @GetMapping("/thong-tin-ca-nhan/sua/{id}")
    public String hienThiFormSuaThongTin(@PathVariable("id") Integer id, Model model) {
        KhachHang khachHang = khachHangService.findById(id);
        if (khachHang == null) {
            return "redirect:/thong-tin-ca-nhan";
        }

        KhachHangDto khachHangDto = new KhachHangDto();
        khachHangDto.setId(khachHang.getId());
        khachHangDto.setHinhAnh(khachHang.getHinhAnh());
        khachHangDto.setMaKhachHang(khachHang.getMaKhachHang());
        khachHangDto.setTenKhachHang(khachHang.getTenKhachHang());
        khachHangDto.setEmail(khachHang.getTaiKhoan().getEmail());
        khachHangDto.setSdt(khachHang.getTaiKhoan().getSdt());
        khachHangDto.setTrangThai(khachHang.getTrangThai());
        khachHangDto.setVaiTro(khachHang.getTaiKhoan().getVaiTro());
        khachHangDto.setGioiTinh(khachHang.getGioiTinh());
        khachHangDto.setNgaySinh(khachHang.getNgaySinh());
        khachHangDto.setNgayTao(khachHang.getNgayTao());
        khachHangDto.setNgayCapNhat(khachHang.getNgayCapNhat());

        // Load danh sách địa chỉ
        List<DiaChi> danhSachDiaChi = diaChiService.findByKhachHangId(id);
        if (danhSachDiaChi == null) {
            danhSachDiaChi = new ArrayList<>();
        }

        List<DiaChiDTO> listDiaChiDTO = danhSachDiaChi.stream()
                .map(diaChi -> {
                    DiaChiDTO dto = new DiaChiDTO();
                    dto.setId(diaChi.getId());
                    dto.setTinh(diaChi.getTinh());
                    dto.setHuyen(diaChi.getHuyen());
                    dto.setXa(diaChi.getXa());
                    dto.setDiaChiCuThe(diaChi.getDiaChiCuThe());
                    dto.setTrangThai(diaChi.getTrangThai());
                    return dto;
                }).collect(Collectors.toList());

        khachHangDto.setListDiaChi(listDiaChiDTO);

        model.addAttribute("khachHang", khachHangDto);
        return "tai-khoan/sua-thong-tin-khach-hang"; // View của user, không phải admin
    }
    @PostMapping("/thong-tin-ca-nhan/sua/{id}")
    public String suaKhachHang(@ModelAttribute("khachHang") KhachHangDto khachHangDto,
                               @RequestParam(name = "anh") MultipartFile anh,
                               @RequestParam("oldImage") String oldImage,
                               @PathVariable Integer id) {
        if (!anh.isEmpty()) {
            String fileName = fileLoadService.uploadFile(anh);
            khachHangDto.setHinhAnh(fileName);
        } else {
            khachHangDto.setHinhAnh(oldImage);
        }
        khachHangService.update(khachHangDto, id);
        return "redirect:/thong-tin-ca-nhan";
    }


    @GetMapping("/doi-mat-khau-kh")
    public String hienThiFormDoiMatKhauKhachHang(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String email = principal.getName();
        model.addAttribute("email", email);
        return "/tai-khoan/doi-mat-khau";
    }
    @GetMapping("/admin/thong-tin-nv")
    public String thongTinNhanVien(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        NhanVien nhanVien = nhanVienService.findByEmail(email);
        if (nhanVien != null) {
            model.addAttribute("currentNhanVien", nhanVien);
            return "tai-khoan/thong-tin-nhan-vien";
        }

        return "redirect:/"; // tạm điều hướng nếu không phải nhân viên
    }


}

