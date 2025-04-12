package com.example.datnsd26.services.binhsanpham;

import com.example.datnsd26.controller.response.PublicSanPhamResponse;
import com.example.datnsd26.models.HinhAnh;
import com.example.datnsd26.models.MauSac;
import com.example.datnsd26.models.SanPham;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.SanPhamRepositoty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicSanPhamService {
    private final SanPhamRepositoty sanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public List<PublicSanPhamResponse> getAllProducts() {
        List<SanPham> sanPhamList = sanPhamRepository.findAll();
        return sanPhamList.stream().map(sanPham -> {
            // Lấy danh sách tất cả biến thể của sản phẩm
            List<SanPhamChiTiet> danhSachBienThe = sanPhamChiTietRepository.findBySanPham(sanPham);

            if (danhSachBienThe.isEmpty()) {
                return null; // Nếu không có biến thể, bỏ qua sản phẩm này
            }

            // Lọc danh sách biến thể để lấy biến thể có trạng thái là true
            SanPhamChiTiet bienTheDuocChon = danhSachBienThe.stream()
                    .filter(SanPhamChiTiet::getTrangThai) // Giữ lại các biến thể có trạng thái true
                    .findFirst() // Lấy biến thể đầu tiên thỏa mãn
                    .orElse(null); // Trả về null nếu không có biến thể nào thỏa mãn

            if (bienTheDuocChon == null) {
                return null; // Nếu không có biến thể nào có trạng thái true, bỏ qua sản phẩm này
            }
// Lấy tên thương hiệu từ biến thể
            String tenThuongHieu = bienTheDuocChon.getThuongHieu() != null
                    ? bienTheDuocChon.getThuongHieu().getTen()
                    : "Không rõ";
            return PublicSanPhamResponse.builder()
                    .id(sanPham.getId())
                    .tenSanPham(sanPham.getTenSanPham())
                    .hinhAnh(bienTheDuocChon.getHinhAnh().stream().findFirst()
                            .map(HinhAnh::getTenAnh)
                            .orElse("default.jpg")) // Lấy ảnh đầu tiên
                    .giaBan(bienTheDuocChon.getGiaBan()) // Lấy giá từ biến thể
                    .giaBanSauGiam(bienTheDuocChon.getGiaBanSauGiam())
                    .idSanPhamChiTiet(bienTheDuocChon.getId()) // ✅ Lưu ID biến thể sản phẩm
                    .thuongHieu(tenThuongHieu)
                    .build();
        }).filter(Objects::nonNull).toList();
    }


    public List<PublicSanPhamResponse> filterProducts(
            List<Long> thuongHieuIds,
            List<Long> chatLieuIds,
            List<Long> deGiayIds,
            List<Long> kichCoIds,
            List<Long> mauSacIds,
            String sortOrder
    ) {
        // Gọi repository
        List<SanPhamChiTiet> result = sanPhamChiTietRepository.filterSanPham(
                thuongHieuIds == null || thuongHieuIds.isEmpty() ? null : thuongHieuIds,
                chatLieuIds == null || chatLieuIds.isEmpty() ? null : chatLieuIds,
                deGiayIds == null || deGiayIds.isEmpty() ? null : deGiayIds,
                kichCoIds == null || kichCoIds.isEmpty() ? null : kichCoIds,
                mauSacIds == null || mauSacIds.isEmpty() ? null : mauSacIds,
                true
        );

        // Group theo sản phẩm (mỗi sản phẩm chỉ hiển thị 1 dòng - giá thấp nhất)
        Map<Long, SanPhamChiTiet> uniqueBySanPham = new LinkedHashMap<>();
        for (SanPhamChiTiet spct : result) {
            Integer idSanPham = spct.getSanPham().getId();
            if (!uniqueBySanPham.containsKey(idSanPham) ||
                    spct.getGiaBan().compareTo(uniqueBySanPham.get(idSanPham).getGiaBan()) < 0) {
                uniqueBySanPham.put(Long.valueOf(idSanPham), spct);
            }
        }

        List<SanPhamChiTiet> finalList = new ArrayList<>(uniqueBySanPham.values());

        // Sắp xếp
        if ("price_asc".equals(sortOrder)) {
            finalList.sort(Comparator.comparing(SanPhamChiTiet::getGiaBan));
        } else if ("price_desc".equals(sortOrder)) {
            finalList.sort(Comparator.comparing(SanPhamChiTiet::getGiaBan).reversed());
        } else if ("name_asc".equals(sortOrder)) {
            finalList.sort(Comparator.comparing(spct -> spct.getSanPham().getTenSanPham(), Comparator.nullsLast(String::compareToIgnoreCase)));
        } else if ("name_desc".equals(sortOrder)) {
            finalList.sort(Comparator.comparing((SanPhamChiTiet spct) -> spct.getSanPham().getTenSanPham(), Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
        }

        // Map sang DTO
        return finalList.stream().map(spct -> PublicSanPhamResponse.builder()
                .id(spct.getSanPham().getId())
                .tenSanPham(spct.getSanPham().getTenSanPham())
                .giaBan(spct.getGiaBan())
                .giaBanSauGiam(spct.getGiaBanSauGiam())
                .idSanPhamChiTiet(spct.getId())
                .hinhAnh(spct.getSanPham().getSpct().stream()
                        .flatMap(ct -> ct.getHinhAnh().stream())
                        .findFirst()
                        .map(HinhAnh::getTenAnh)
                        .orElse(null))
                .thuongHieu(spct.getThuongHieu().getTen())
                .build()
        ).collect(Collectors.toList());
    }


    // Phương thức lấy tất cả sản phẩm sắp xếp theo tên A-Z
    public List<PublicSanPhamResponse> getAllProductsSortedByNameAsc() {
        return sanPhamChiTietRepository.findAllSortedByNameAsc();
    }

    // Phương thức lấy tất cả sản phẩm sắp xếp theo tên Z-A
    public List<PublicSanPhamResponse> getAllProductsSortedByNameDesc() {
        return sanPhamChiTietRepository.findAllSortedByNameDesc();
    }

    // Phương thức lấy tất cả sản phẩm sắp xếp theo giá từ thấp đến cao
    public List<PublicSanPhamResponse> getAllProductsSortedByPriceAsc() {
        return sanPhamChiTietRepository.findAllSortedByPriceAsc();
    }

    // Phương thức lấy tất cả sản phẩm sắp xếp theo giá từ cao đến thấp
    public List<PublicSanPhamResponse> getAllProductsSortedByPriceDesc() {
        return sanPhamChiTietRepository.findAllSortedByPriceDesc();
    }

    public List<PublicSanPhamResponse> searchProducts(String keyword) {
        List<SanPham> results = Optional.ofNullable(
                sanPhamRepository.searchByTenOrMa(keyword.toLowerCase())
        ).orElse(Collections.emptyList());

        return results.stream()
                .map(sp -> {
                    SanPhamChiTiet spct = null;

                    if (sp.getSpct() != null && !sp.getSpct().isEmpty()) {
                        spct = sp.getSpct().get(0);

                        System.out.println(">>> SAN PHAM: " + sp.getTenSanPham());
                        System.out.println(">>> GIA BAN: " + spct.getGiaBan() + ", GIA SAU GIAM: " + spct.getGiaBanSauGiam());
                    } else {
                        System.out.println(">>> SAN PHAM KHONG CO CHI TIET: " + sp.getTenSanPham());
                    }
                    Float giaBan = spct != null ? spct.getGiaBan() : null;
                    Float giaBanSauGiam = spct != null ? spct.getGiaBanSauGiam() : null;
                    Integer idSpct = spct != null ? spct.getId() : null;

                    String tenAnh = null;
                    if (spct != null && spct.getHinhAnh() != null && !spct.getHinhAnh().isEmpty()) {
                        String rawTenAnh = spct.getHinhAnh().get(0).getTenAnh();
                        tenAnh = rawTenAnh.startsWith("/upload/") ? rawTenAnh : "/upload/" + rawTenAnh;
                    }

                    // ✅ Lấy tên thương hiệu nếu có
                    String tenThuongHieu = (spct != null && spct.getThuongHieu() != null)
                            ? spct.getThuongHieu().getTen()
                            : "Không rõ";
                    return PublicSanPhamResponse.builder()
                            .id(sp.getId())
                            .tenSanPham(sp.getTenSanPham())
                            .giaBan(giaBan)
                            .giaBanSauGiam(giaBanSauGiam)
                            .idSanPhamChiTiet(idSpct)
                            .hinhAnh(tenAnh)
                            .thuongHieu(tenThuongHieu)
                            .build();

                })
                .collect(Collectors.toList());

    }


}
