package com.example.datnsd26.controller;

import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.services.cart.GioHangService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final GioHangService gioHangService;


    @GetMapping("/homepage")
    public String homepage() {
        return "/shop/homepage";
    }

    @GetMapping("/shop/product/all-product")
    public String allProduct(Model model) {
        // Lấy danh sách sản phẩm theo nhóm biến thể (1 nhóm/màu sắc)
        List<SanPhamChiTiet> productGroups = sanPhamChiTietRepository.findDistinctBySanPham_Id(1);
        model.addAttribute("productGroups", productGroups);
        return "/shop/all-product";
    }

    @GetMapping("/shop/product/{id}")
    public String getProductDetail(@PathVariable("id") Integer id, Model model) {
        Optional<SanPhamChiTiet> productOpt = sanPhamChiTietRepository.findById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/shop"; // Nếu không tìm thấy, quay về trang shop
        }

        SanPhamChiTiet product = productOpt.get();

        // Lấy danh sách size của sản phẩm này (cùng màu, cùng loại sản phẩm)
        List<SanPhamChiTiet> sizes = sanPhamChiTietRepository.findBySanPhamAndMauSac(
                product.getSanPham(), product.getMauSac());

        model.addAttribute("product", product);
        model.addAttribute("sizes", sizes);
        return "/shop/product-detail";
    }

    @PostMapping("/shop/add-to-user-cart")
    public String addToCart(@RequestParam("sizeId") Integer sizeId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (sizeId == null) {  // Check lỗi nếu sizeId null
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn size sản phẩm.");
            return "redirect:/shop/product/all-product";
        }

        Optional<SanPhamChiTiet> optionalProduct = sanPhamChiTietRepository.findById(sizeId);

        if (optionalProduct.isPresent()) {
            SanPhamChiTiet product = optionalProduct.get();

            if (quantity > product.getSoLuong()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Hiện sản phẩm " + product.getSanPham().getTenSanPham() + " chỉ còn " + product.getSoLuong() +
                                " sản phẩm. Nếu bạn đặt hàng với số lượng lớn, hãy liên hệ với chúng tôi để được hỗ trợ.");
                return "redirect:/shop/product/" + sizeId;
            }

            gioHangService.addToUserCart(session, sizeId, quantity);
        }

        return "redirect:/shop/cart";
    }

}
