package com.example.datnsd26.controller;

import com.example.datnsd26.controller.response.PublicSanPhamResponse;
import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.KichCoRepository;
import com.example.datnsd26.repository.MauSacRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.SanPhamRepositoty;
import com.example.datnsd26.services.binhsanpham.PublicSanPhamService;
import com.example.datnsd26.services.cart.GioHangService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final GioHangService gioHangService;
    private final PublicSanPhamService publicSanPhamService;
    private final KichCoRepository kichCoRepository;


    @GetMapping("/homepage")
    public String homepage() {
        return "/shop/homepage";
    }

    @GetMapping("/shop/product/all-product")
    public String allProduct(Model model) {
        List<PublicSanPhamResponse> products = publicSanPhamService.getAllProducts();
        model.addAttribute("products", products);
        return "/shop/all-product";
    }

    @GetMapping("/shop/product/details/{idSanPhamChiTiet}")
    public String productDetails(@PathVariable Integer idSanPhamChiTiet, Model model) {
        // Lấy thông tin của biến thể được chọn
        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Lấy danh sách tất cả biến thể của cùng một sản phẩm
        List<SanPhamChiTiet> danhSachBienThe = sanPhamChiTietRepository.findBySanPham(spct.getSanPham());

        // Lấy danh sách màu sắc của tất cả biến thể
        Set<MauSac> danhSachMauSac = danhSachBienThe.stream()
                .map(SanPhamChiTiet::getMauSac)
                .collect(Collectors.toSet());

        // Lấy danh sách kích cỡ
        List<KichCo> danhSachKichCo = kichCoRepository.findAll(); // Lấy tất cả kích cỡ từ DB

        // Xác định những kích cỡ nào bị disable
        Set<Integer> kichCoTonTai = danhSachBienThe.stream()
                .filter(sp -> sp.getMauSac().getId().equals(spct.getMauSac().getId()))
                .map(sp -> sp.getKichCo().getId())
                .collect(Collectors.toSet());

        List<HinhAnh> danhSachHinhAnh = spct.getHinhAnh();

        model.addAttribute("spct", spct);
        model.addAttribute("danhSachMauSac", danhSachMauSac);
        model.addAttribute("danhSachKichCo", danhSachKichCo);
        model.addAttribute("kichCoTonTai", kichCoTonTai);
        model.addAttribute("danhSachHinhAnh", danhSachHinhAnh);

        return "/shop/product-details";
    }

    @GetMapping("/shop/product/details")
    public String productDetails(
            @RequestParam Integer idSanPham,
            @RequestParam(required = false) Integer mauSac,
            @RequestParam(required = false) Integer kichCo,
            Model model) {

        // Lấy danh sách tất cả biến thể của sản phẩm
        List<SanPhamChiTiet> danhSachBienThe = sanPhamChiTietRepository.findBySanPhamId(idSanPham);

        // Lấy danh sách màu sắc
        List<MauSac> danhSachMauSac = danhSachBienThe.stream()
                .map(SanPhamChiTiet::getMauSac)
                .distinct()
                .sorted(Comparator.comparing(MauSac::getId))
                .toList();

        // Lấy danh sách kích cỡ
        List<KichCo> danhSachKichCo = kichCoRepository.findAll();

        // Xác định biến thể phù hợp
        SanPhamChiTiet spct = danhSachBienThe.stream()
                .filter(sp -> (mauSac == null || sp.getMauSac().getId().equals(mauSac)) &&
                        (kichCo == null || sp.getKichCo().getId().equals(kichCo)))
                .findFirst()
                .orElse(danhSachBienThe.get(0)); // Nếu không có, lấy biến thể đầu tiên

        // Xác định kích cỡ có sẵn cho màu sắc đã chọn
        Set<Integer> kichCoTonTai = danhSachBienThe.stream()
                .filter(sp -> sp.getMauSac().getId().equals(spct.getMauSac().getId()))
                .map(sp -> sp.getKichCo().getId())
                .collect(Collectors.toSet());

        model.addAttribute("spct", spct);
        model.addAttribute("danhSachMauSac", danhSachMauSac);
        model.addAttribute("danhSachKichCo", danhSachKichCo);
        model.addAttribute("kichCoTonTai", kichCoTonTai);

        return "/shop/product-details";
    }

//    @PostMapping("/add-to-cart")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> addToCart(@RequestParam("productId") Integer productId,
//                                                         @RequestParam("quantity") Integer quantity,
//                                                         HttpSession session) {
//        Map<String, Object> response = new HashMap<>();
//        Optional<SanPhamChiTiet> optionalProduct = sanPhamChiTietRepository.findById(productId);
//
//        if (optionalProduct.isEmpty()) {
//            response.put("success", false);
//            response.put("message", "Sản phẩm không tồn tại.");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        SanPhamChiTiet product = optionalProduct.get();
//
//        if (quantity == null || quantity < 1) {
//            response.put("success", false);
//            response.put("message", "Số lượng không hợp lệ.");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        if (quantity > product.getSoLuong()) {
//            response.put("success", false);
//            response.put("message", "Chỉ còn " + product.getSoLuong() + " sản phẩm.");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        gioHangService.addToCart(session, productId, quantity);
//
//        response.put("success", true);
//        response.put("message", "Thêm vào giỏ hàng thành công!");
//        return ResponseEntity.ok(response);
//    }


}
