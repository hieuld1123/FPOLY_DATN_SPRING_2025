package com.example.datnsd26.controller;

import com.example.datnsd26.controller.response.PublicSanPhamResponse;
import com.example.datnsd26.models.*;
import com.example.datnsd26.repository.*;
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
    private final ThuongHieuRepository thuongHieuRepository;
    private final MauSacRepository mauSacRepository;
    private final DeGiayRepository deGiayRepository;
    private final ChatLieuRepository chatLieuRepository;

    @GetMapping("/shop/homepage")
    public String homepage(Model model) {
        List<PublicSanPhamResponse> products = publicSanPhamService.getAllProducts();
        model.addAttribute("products", products);
        return "/shop/homepage";
    }

    @GetMapping("/shop/ve-chung-toi")
    public String veChungToi() {
        return "/shop/ve-chung-toi";
    }

    @GetMapping("/shop/lien-he")
    public String lienHe() {
        return "/shop/lien-he";
    }

    @GetMapping("/shop/product/all-product")
    public String allProduct(
            @RequestParam(required = false) List<Long> filterBrand,
            @RequestParam(required = false) List<Long> filterMaterial,
            @RequestParam(required = false) List<Long> filterSole,
            @RequestParam(required = false) List<Long> filterSize,
            @RequestParam(required = false) String filterColor, // màu là chuỗi CSV "1,2,3"
            @RequestParam(required = false) String sortOrder,
            Model model) {

        List<Long> colorIds = new ArrayList<>();
        if (filterColor != null && !filterColor.isEmpty()) {
            colorIds = Arrays.stream(filterColor.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        List<PublicSanPhamResponse> products = publicSanPhamService.filterProducts(
                filterBrand, filterMaterial, filterSole, filterSize, colorIds, sortOrder
        );

        List<ThuongHieu> listThuongHieu = thuongHieuRepository.getAll();
        List<MauSac> listMauSac = mauSacRepository.getAll();
        List<KichCo> listKichCo = kichCoRepository.getAll();
        List<DeGiay> listDeGiay = deGiayRepository.getAll();
        List<ChatLieu> listChatLieu = chatLieuRepository.getAll();

        model.addAttribute("products", products);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("mauSac", listMauSac);
        model.addAttribute("thuongHieu", listThuongHieu);
        model.addAttribute("kichCo", listKichCo);
        model.addAttribute("deGiay", listDeGiay);
        model.addAttribute("chatLieu", listChatLieu);

        return "/shop/all-product";
    }


    @GetMapping("/shop/product/details/{idSanPhamChiTiet}")
    public String productDetails(@PathVariable Integer idSanPhamChiTiet, Model model) {
        // Lấy thông tin của biến thể được chọn
        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Lấy danh sách tất cả biến thể của cùng một sản phẩm ĐANG KINH DOANH
        List<SanPhamChiTiet> danhSachBienThe = sanPhamChiTietRepository.findBySanPhamAndTrangThaiTrue(spct.getSanPham());

        // Lấy danh sách màu sắc của tất cả biến thể ĐANG KINH DOANH, đã sắp xếp
        Set<MauSac> danhSachMauSac = danhSachBienThe.stream()
                .map(SanPhamChiTiet::getMauSac)
                .distinct()
                .sorted(Comparator.comparing(MauSac::getId))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(MauSac::getId))));

        // Lấy danh sách kích cỡ
        List<KichCo> danhSachKichCo = kichCoRepository.findAll();

        // Xác định kích cỡ có sẵn cho MÀU SẮC đã chọn
        Set<Integer> kichCoTonTai = danhSachBienThe.stream()
                .filter(bienThe -> bienThe.getMauSac().getId().equals(spct.getMauSac().getId()))
                .map(bienThe -> bienThe.getKichCo().getId())
                .collect(Collectors.toSet());

        // Lấy danh sách hình ảnh
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

        // Lấy danh sách tất cả biến thể của sản phẩm ĐANG KINH DOANH
        List<SanPhamChiTiet> danhSachBienThe = sanPhamChiTietRepository.findBySanPhamIdAndTrangThaiTrue(idSanPham);

        // Lấy danh sách màu sắc ĐANG KINH DOANH
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
                .orElse(danhSachBienThe.isEmpty() ? null : danhSachBienThe.get(0)); // Nếu không có, trả về null hoặc sản phẩm đầu tiên

        // Nếu không tìm thấy sản phẩm phù hợp, có thể xử lý theo cách khác (ví dụ: hiển thị thông báo lỗi)
        if (spct == null) {
            // Trường hợp không tìm thấy sản phẩm phù hợp với màu sắc và kích cỡ đã chọn
            // Bạn có thể muốn hiển thị một trang lỗi hoặc thông báo cho người dùng
            model.addAttribute("errorMessage", "Không tìm thấy sản phẩm phù hợp với lựa chọn của bạn.");
            return "/shop/product-details"; // Hoặc trang lỗi
        }

        // Xác định kích cỡ có sẵn cho màu sắc đã chọn
        Set<Integer> kichCoTonTai = danhSachBienThe.stream()
                .filter(bienThe -> bienThe.getMauSac().getId().equals(spct.getMauSac().getId()))
                .map(bienThe -> bienThe.getKichCo().getId())
                .collect(Collectors.toSet());

        model.addAttribute("spct", spct);
        model.addAttribute("danhSachMauSac", danhSachMauSac);
        model.addAttribute("danhSachKichCo", danhSachKichCo);
        model.addAttribute("kichCoTonTai", kichCoTonTai);

        return "/shop/product-details";
    }

}
