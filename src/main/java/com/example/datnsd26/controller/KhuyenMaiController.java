package com.example.datnsd26.controller;

import com.example.datnsd26.models.KhuyenMai;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.KhuyenMaiChiTietRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.services.KhuyenMaiSchedulerService;
import com.example.datnsd26.services.KhuyenMaiService;
import com.example.datnsd26.services.SanPhamChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private KhuyenMaiChiTietRepository khuyenMaiChiTietRepository;
    @Autowired
    private SanPhamChiTietService sanPhamChiTietService;

    @Autowired
    private KhuyenMaiSchedulerService khuyenMaiSchedulerService;
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
    public String viewCreate(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(required = false) Boolean selectAll) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        Page<SanPhamChiTiet> sanPhamPage = sanPhamChiTietRepository.findAll(pageable);

        // Thêm tổng số sản phẩm vào model
        long totalProducts = sanPhamChiTietRepository.count();

        model.addAttribute("khuyenMai", new KhuyenMai());
        model.addAttribute("sanPhamPage", sanPhamPage);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("selectAll", selectAll);

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
                model.addAttribute("sanPhams", sanPhamChiTietService.findAll());
                return "khuyenmai/user/khuyenmai-create";
            }

            // Kiểm tra thời gian khuyến mãi
            if (khuyenMai.getThoiGianKetThuc().isBefore(khuyenMai.getThoiGianBatDau())) {
                model.addAttribute("error", "Thời gian kết thúc phải sau thời gian bắt đầu");
                model.addAttribute("sanPhams", sanPhamChiTietService.findAll());
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
            model.addAttribute("sanPhams", sanPhamChiTietService.findAll());
            return "khuyenmai/user/khuyenmai-create";
        }
    }

    /**
     * Hiển thị form chỉnh sửa khuyến mãi
     */
    @GetMapping("/edit/{id}")
    public String viewEdit(@PathVariable Long id,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size,
                           Model model) {
        try {
            KhuyenMai khuyenMai = khuyenMaiService.findById(id);

            // Tạo đối tượng Pageable để phân trang
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
            Page<SanPhamChiTiet> sanPhamPage = khuyenMaiService.finAllPage(pageable);
            List<SanPhamChiTiet> sanPhams = sanPhamPage.getContent();

            // Lấy danh sách sản phẩm đã có khuyến mãi
            Map<Integer, Float> sanPhamGiamGiaMap = khuyenMaiService.getGiaTriGiamMap(id);

            model.addAttribute("khuyenMai", khuyenMai);
            model.addAttribute("sanPhams", sanPhams);
            model.addAttribute("sanPhamGiamGiaMap", sanPhamGiamGiaMap);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", sanPhamPage.getTotalPages());
            model.addAttribute("size", size);

            return "khuyenmai/user/khuyenmai-edit";
        } catch (Exception e) {
            return "redirect:/admin/khuyen-mai?error=" + e.getMessage();
        }
    }



    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute KhuyenMai khuyenMaiMoi,
                         @RequestParam Map<String, String> allParams,
//                         @RequestParam("sanPhamIds") List<Long> sanPhamIds,
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
            model.addAttribute("sanPhams", sanPhamChiTietService.findAll());
            model.addAttribute("sanPhamGiamGiaMap", sanPhamGiamGiaMap);
            return "khuyenmai/user/khuyenmai-edit";
        }
    }
    /**
     * Xử lý xóa khuyến mãi
     */
    @PostMapping("/delete/{id}")
    public String deleteKhuyenMai(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        khuyenMaiService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa khuyến mãi thành công!");
        return "redirect:/admin/khuyen-mai";
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

    @GetMapping("/san-pham-khuyen-mai")
    public ResponseEntity<List<Long>> getSanPhamDangKhuyenMai() {
        List<Long> danhSachSanPham = khuyenMaiChiTietRepository.findSanPhamDangKhuyenMai();
        return ResponseEntity.ok(danhSachSanPham);
    }

    @GetMapping("/search-products")
    public String searchProducts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        if (searchTerm == null) {
            searchTerm = "";
        }
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<SanPhamChiTiet> productPage = sanPhamChiTietRepository.findAvailableProductsWithSearch(searchTerm, now, pageable);
        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", searchTerm);
        return "/admin/khuyenmai/user/product-search";
    }

    @GetMapping("/search")
    public String searchKhuyenMai(
            @RequestParam(required = false) String tenChienDich,
            @RequestParam(required = false) Integer trangThai,  // Đảm bảo là Integer
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<KhuyenMai> khuyenMaiPage = khuyenMaiService.searchKhuyenMai(
                    tenChienDich, trangThai, startDate, endDate, pageable);

            model.addAttribute("danhSachKhuyenMai", khuyenMaiPage);
            model.addAttribute("tenChienDich", tenChienDich);
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);

            return "khuyenmai/user/khuyenmai-list";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tìm kiếm khuyến mãi");
            return "error";
        }
    }

    @GetMapping("/statuses")
    @ResponseBody
    public List<Map<String, Serializable>> getVoucherStatuses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<KhuyenMai> pageData = khuyenMaiService.findAll(pageable);
        return pageData.stream().map(v -> Map.of(
                "id", (Serializable) v.getId(),
                "trangThai", v.getTrangThai()
        )).toList();
    }


}