package com.example.datnsd26.controller;

import com.example.datnsd26.models.KhuyenMai;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.KhuyenMaiRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.services.KhuyenMaiService;
import com.example.datnsd26.services.SanPhamChitiet1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller xử lý các chức năng liên quan đến Khuyến Mãi
 * Bao gồm: xem danh sách, thêm mới, chỉnh sửa và xóa khuyến mãi
 */
@Controller
@RequestMapping("/admin/khuyen-mai")
public class KhuyenMaiController {
    @Autowired
    private KhuyenMaiService khuyenMaiService;
    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;
    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;
    @Autowired
    private SanPhamChitiet1 sanPhamChitiet1;

    /**
     * Hiển thị danh sách khuyến mãi
     */
    @GetMapping
    public String viewKhuyenMai(Model model, @RequestParam(defaultValue = "0") int page) {
        try {
            int pageSize = 5;
            Page<KhuyenMai> khuyenMaiPage = khuyenMaiService.findAll(PageRequest.of(page, pageSize));
            model.addAttribute("danhSachKhuyenMai", khuyenMaiPage);
            model.addAttribute("title", "Danh sách khuyến mãi");
            return "khuyenmai/user/khuyenmai-list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách khuyến mãi");
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    /**
     * Hiển thị form tạo mới khuyến mãi
     */
    @GetMapping("/create")
    public String viewCreate(Model model) {
        model.addAttribute("khuyenMai", new KhuyenMai());
        model.addAttribute("sanPhams", sanPhamChiTietRepository.findAll());
        return "khuyenmai/user/khuyenmai-create";
    }

    /**
     * Xử lý tạo mới khuyến mãi
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute KhuyenMai khuyenMai,
                         BindingResult bindingResult,
                         @RequestParam Map<String, String> allParams,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        try {
            // Kiểm tra dữ liệu nhập vào
            if (bindingResult.hasErrors()) {
                model.addAttribute("sanPhams", sanPhamChiTietRepository.findAll());
                return "khuyenmai/user/khuyenmai-create";
            }

            // Kiểm tra thời gian khuyến mãi
            if (khuyenMai.getThoiGianKetThuc().isBefore(khuyenMai.getThoiGianBatDau())) {
                model.addAttribute("error", "Thời gian kết thúc phải sau thời gian bắt đầu");
                model.addAttribute("sanPhams", sanPhamChiTietRepository.findAll());
                return "khuyenmai/user/khuyenmai-create";
            }

            Map<Integer, Float> sanPhamGiamGia = new HashMap<>();
            boolean hasValidProduct = false;

            // Xử lý thông tin sản phẩm được chọn
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("sanPham_") && entry.getValue().equals("on")) {
                    String sanPhamId = entry.getKey().substring(8);
                    String mucGiamKey = "mucGiam_" + sanPhamId;
                    String mucGiamValue = allParams.get(mucGiamKey);

                    if (mucGiamValue != null && !mucGiamValue.isEmpty()) {
                        try {
                            Long id = Long.parseLong(sanPhamId);
                            Float mucGiam = Float.parseFloat(mucGiamValue);

                            // Kiểm tra mức giảm giá hợp lệ
                            if (mucGiam <= 0) {
                                throw new IllegalArgumentException("Mức giảm phải lớn hơn 0 cho sản phẩm " + sanPhamId);
                            }

                            if ("Phần Trăm".equals(khuyenMai.getHinhThucGiam()) && mucGiam > 100) {
                                throw new IllegalArgumentException("Mức giảm theo phần trăm không được vượt quá 100%");
                            }

                            sanPhamGiamGia.put(Math.toIntExact(id), mucGiam);
                            hasValidProduct = true;
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Giá trị giảm không hợp lệ cho sản phẩm " + sanPhamId);
                        }
                    }
                }
            }

            // Kiểm tra có ít nhất một sản phẩm được chọn
            if (!hasValidProduct) {
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm và nhập mức giảm");
            }

            khuyenMaiService.save(khuyenMai, sanPhamGiamGia);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo khuyến mãi thành công");
            return "redirect:/admin/khuyen-mai";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("sanPhams", sanPhamChiTietRepository.findAll());
            return "khuyenmai/user/khuyenmai-create";
        }
    }

    /**
     * Hiển thị form chỉnh sửa khuyến mãi
     */
    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable Long id, Model model) {
        try {
            KhuyenMai khuyenMai = khuyenMaiService.findById(id);
            List<SanPhamChiTiet> allSanPhams = sanPhamChiTietRepository.findAll();

            // Lấy map chứa thông tin sản phẩm và mức giảm giá
            Map<Integer, Float> sanPhamGiamGiaMap = khuyenMaiService.getGiaTriGiamMap(id);

            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("sanPhams", allSanPhams);
            model.addAttribute("sanPhamGiamGiaMap", sanPhamGiamGiaMap);

            return "khuyenmai/user/khuyenmai-edit";
        } catch (Exception e) {
            return "redirect:/admin/khuyen-mai?error=" + e.getMessage();
        }
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute KhuyenMai khuyenMaiMoi,
                         @RequestParam Map<String, String> allParams,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        try {
            Map<Integer, Float> sanPhamGiamGia = new HashMap<>();
            boolean hasValidProduct = false;

            // Xử lý thông tin sản phẩm được chọn
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("sanPham_") && entry.getValue().equals("on")) {
                    String sanPhamId = entry.getKey().substring(8);
                    String mucGiamKey = "mucGiam_" + sanPhamId;
                    String mucGiamValue = allParams.get(mucGiamKey);

                    if (mucGiamValue != null && !mucGiamValue.isEmpty()) {
                        try {
                            Integer idSp = Integer.parseInt(sanPhamId);
                            Float mucGiam = Float.parseFloat(mucGiamValue);

                            // Kiểm tra mức giảm giá hợp lệ
                            if (mucGiam <= 0) {
                                throw new IllegalArgumentException("Mức giảm phải lớn hơn 0 cho sản phẩm " + sanPhamId);
                            }

                            if ("Phần Trăm".equals(khuyenMaiMoi.getHinhThucGiam()) && mucGiam > 100) {
                                throw new IllegalArgumentException("Mức giảm theo phần trăm không được vượt quá 100%");
                            }

                            sanPhamGiamGia.put(idSp, mucGiam);
                            hasValidProduct = true;
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Giá trị giảm không hợp lệ cho sản phẩm " + sanPhamId);
                        }
                    }
                }
            }

            // Cập nhật khuyến mãi
            khuyenMaiService.update(id, khuyenMaiMoi, hasValidProduct ? sanPhamGiamGia : null);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khuyến mãi thành công");
            return "redirect:/admin/khuyen-mai";

        } catch (Exception e) {
            KhuyenMai khuyenMai = khuyenMaiService.findById(id);
            Map<Integer, Float> sanPhamGiamGiaMap = khuyenMaiService.getGiaTriGiamMap(id);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("sanPhams", sanPhamChiTietRepository.findAll());
            model.addAttribute("sanPhamGiamGiaMap", sanPhamGiamGiaMap);
            return "khuyenmai/user/khuyenmai-edit";
        }
    }
    /**
     * Xử lý xóa khuyến mãi
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        try {
            KhuyenMai khuyenMai = khuyenMaiRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy khuyến mãi"));



            khuyenMaiService.delete(id);
            return "redirect:/admin/khuyen-mai?success=deleted";
        } catch (IllegalStateException e) {
            return "redirect:/admin/khuyen-mai?error=" + e.getMessage();
        }
    }

    /**
     * Chuyển đổi dữ liệu từ form sang đối tượng Map chứa thông tin giảm giá của sản phẩm
     */
    private Map<Long, Float> convertToSanPhamGiamGia(Map<String, String> raw, KhuyenMai khuyenMai) {
        Map<Long, Float> sanPhamGiamGia = new HashMap<>();
        Float giaTriGiam = (khuyenMai.getGiaTriGiam() != null) ? khuyenMai.getGiaTriGiam().floatValue() : 0.0f;

        for (Map.Entry<String, String> entry : raw.entrySet()) {
            if (entry.getKey().startsWith("sanPham_") && !entry.getValue().trim().isEmpty()) {
                try {
                    Long sanPhamId = Long.parseLong(entry.getKey().substring(8));
                    Float giamGia = Float.parseFloat(entry.getValue().trim());

                    if (giamGia <= 0) {
                        throw new IllegalArgumentException("Mức giảm phải lớn hơn 0.");
                    }

                    if ("Phần Trăm".equals(khuyenMai.getHinhThucGiam()) && giamGia > 100) {
                        throw new IllegalArgumentException("Mức giảm theo phần trăm không được vượt quá 100%");
                    }

                    sanPhamGiamGia.put(sanPhamId, giamGia);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Mức giảm không hợp lệ cho sản phẩm.");
                }
            }
        }

        if (sanPhamGiamGia.isEmpty() && giaTriGiam <= 0) {
            throw new IllegalArgumentException("Vui lòng nhập ít nhất một mức giảm giá hoặc giá trị giảm.");
        }

        return sanPhamGiamGia;
    }

    @GetMapping("/restore/{id}")
    public String restoreKhuyenMai(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            khuyenMaiService.restoreKhuyenMai(id);
            redirectAttributes.addFlashAttribute("successMessage", "Khôi phục khuyến mãi thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }

    @GetMapping("/stop/{id}")
    public String stopKhuyenMai(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            khuyenMaiService.stopKhuyenMai(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ngừng khuyến mãi thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }


}
