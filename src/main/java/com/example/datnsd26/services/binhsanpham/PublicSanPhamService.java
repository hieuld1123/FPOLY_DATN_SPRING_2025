package com.example.datnsd26.services.binhsanpham;

import com.example.datnsd26.controller.response.PublicSanPhamResponse;
import com.example.datnsd26.models.HinhAnh;
import com.example.datnsd26.models.MauSac;
import com.example.datnsd26.models.SanPham;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.SanPhamRepositoty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicSanPhamService {
    private final SanPhamRepositoty sanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public Page<PublicSanPhamResponse> getAllProducts(Pageable pageable) {
        // Lấy tất cả sản phẩm
        List<SanPham> sanPhamList = sanPhamRepository.findAll();

        // Chuyển đổi danh sách sản phẩm thành danh sách PublicSanPhamResponse
        List<PublicSanPhamResponse> responseList = sanPhamList.stream().map(sanPham -> {
            // Lấy danh sách tất cả biến thể của sản phẩm
            List<SanPhamChiTiet> danhSachBienThe = sanPhamChiTietRepository.findBySanPham(sanPham);

            if (danhSachBienThe.isEmpty()) {
                return null; // Nếu không có biến thể, bỏ qua sản phẩm này
            }

            // Lọc danh sách biến thể để lấy các biến thể có trạng thái là true
            List<SanPhamChiTiet> bienTheDuocChonList = danhSachBienThe.stream()
                    .filter(SanPhamChiTiet::getTrangThai) // Giữ lại các biến thể có trạng thái true
                    .collect(Collectors.toList()); // Thu thập vào danh sách

            if (bienTheDuocChonList.isEmpty()) {
                return null; // Nếu không có biến thể nào có trạng thái true, bỏ qua sản phẩm này
            }

            // Lấy tên thương hiệu từ biến thể
            String tenThuongHieu = bienTheDuocChonList.get(0).getThuongHieu() != null
                    ? bienTheDuocChonList.get(0).getThuongHieu().getTen()
                    : "Không rõ";

            // Lấy danh sách màu sắc của tất cả biến thể
            List<String> danhSachMauSac = bienTheDuocChonList.stream()
                    .map(bienThe -> bienThe.getMauSac() != null ? bienThe.getMauSac().getTen() : "#000000")
                    .distinct() // Lọc các màu sắc trùng lặp
                    .collect(Collectors.toList());
            return PublicSanPhamResponse.builder()
                    .id(sanPham.getId())
                    .tenSanPham(sanPham.getTenSanPham())
                    .hinhAnh(bienTheDuocChonList.get(0).getHinhAnh().stream().findFirst()
                            .map(HinhAnh::getTenAnh)
                            .orElse("default.jpg")) // Lấy ảnh đầu tiên
                    .giaBan(bienTheDuocChonList.get(0).getGiaBan()) // Lấy giá từ biến thể
                    .giaBanSauGiam(bienTheDuocChonList.get(0).getGiaBanSauGiam())
                    .idSanPhamChiTiet(bienTheDuocChonList.get(0).getId()) // Lưu ID biến thể sản phẩm
                    .thuongHieu(tenThuongHieu)
                    .mauSac(danhSachMauSac) // Lưu danh sách màu sắc của sản phẩm
                    .build();
        }).filter(Objects::nonNull).collect(Collectors.toList());

        // Tính toán start và end dựa trên pageable
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min(start + pageable.getPageSize(), responseList.size());

        // Đảm bảo start và end nằm trong phạm vi hợp lệ
        if (start >= responseList.size()) {
            start = responseList.size() - pageable.getPageSize(); // Điều chỉnh khi start vượt quá số lượng mục
            end = responseList.size();
        }

        List<PublicSanPhamResponse> pagedList = responseList.subList(start, end);

        // Tạo PageImpl cho phân trang
        Page<PublicSanPhamResponse> pagedResult = new PageImpl<>(pagedList, pageable, responseList.size());


        return pagedResult;
    }


    public Page<PublicSanPhamResponse> filterProducts(
            List<Long> thuongHieuIds,
            List<Long> chatLieuIds,
            List<Long> deGiayIds,
            List<Long> kichCoIds,
            List<Long> mauSacIds,
            String sortOrder,
            Pageable pageable
    ) {


        // Gọi repository để lấy danh sách SanPhamChiTiet sau khi lọc
        List<SanPhamChiTiet> result = sanPhamChiTietRepository.filterSanPham(
                thuongHieuIds == null || thuongHieuIds.isEmpty() ? null : thuongHieuIds,
                chatLieuIds == null || chatLieuIds.isEmpty() ? null : chatLieuIds,
                deGiayIds == null || deGiayIds.isEmpty() ? null : deGiayIds,
                kichCoIds == null || kichCoIds.isEmpty() ? null : kichCoIds,
                mauSacIds == null || mauSacIds.isEmpty() ? null : mauSacIds,
                true
        );
//        List<SanPhamChiTiet> result = resultPage.getContent();
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
            finalList.sort(Comparator.comparing(SanPhamChiTiet::getGiaBanSauGiam));
        } else if ("price_desc".equals(sortOrder)) {
            finalList.sort(Comparator.comparing(SanPhamChiTiet::getGiaBanSauGiam).reversed());
        } else if ("name_asc".equals(sortOrder)) {
            finalList.sort(Comparator.comparing(spct -> spct.getSanPham().getTenSanPham(), Comparator.nullsLast(String::compareToIgnoreCase)));
        } else if ("name_desc".equals(sortOrder)) {
            finalList.sort(Comparator.comparing((SanPhamChiTiet spct) -> spct.getSanPham().getTenSanPham(), Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
        }

        // Chuyển đổi thành PublicSanPhamResponse và thêm màu sắc
        List<PublicSanPhamResponse> responseList = finalList.stream().map(spct -> {
            // Lấy danh sách màu sắc của sản phẩm
            List<String> danhSachMauSac = spct.getSanPham().getSpct().stream()
                    .map(bienThe -> bienThe.getMauSac() != null ? bienThe.getMauSac().getTen() : "Không xác định")
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("Màu sắc: " + danhSachMauSac);
            // Xây dựng PublicSanPhamResponse với màu sắc
            return PublicSanPhamResponse.builder()
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
                    .mauSac(danhSachMauSac) // Thêm danh sách màu sắc
                    .build();
        }).collect(Collectors.toList());

        // Tính toán start và end dựa trên pageable
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min(start + pageable.getPageSize(), responseList.size());

// Nếu không có sản phẩm, trả về Page rỗng luôn
        if (responseList.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

// Nếu start > end (có thể xảy ra nếu page vượt size list) thì cũng trả list rỗng
        if (start >= responseList.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, responseList.size());
        }

// Nếu bình thường, subList như cũ
        List<PublicSanPhamResponse> pagedList = responseList.subList(start, end);

// Tạo PageImpl cho phân trang
        return new PageImpl<>(pagedList, pageable, responseList.size());

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

    public Page<PublicSanPhamResponse> searchProducts(String keyword, Pageable pageable) {

        // Tìm kiếm sản phẩm theo tên hoặc mã với phân trang
        Page<SanPham> results = sanPhamRepository.searchByTenOrMa(keyword.toLowerCase(), pageable);

        return results.map(sp -> {
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
                tenAnh = rawTenAnh;
            }

            // ✅ Lấy tên thương hiệu nếu có
            String tenThuongHieu = (spct != null && spct.getThuongHieu() != null)
                    ? spct.getThuongHieu().getTen()
                    : "Không rõ";

            // ✅ Lấy danh sách màu sắc của sản phẩm
            List<String> danhSachMauSac = spct.getSanPham().getSpct().stream()
                    .map(bienThe -> bienThe.getMauSac() != null ? bienThe.getMauSac().getTen() : "Không xác định")
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("Màu sắc: " + danhSachMauSac);

            return PublicSanPhamResponse.builder()
                    .id(sp.getId())
                    .tenSanPham(sp.getTenSanPham())
                    .giaBan(giaBan)
                    .giaBanSauGiam(giaBanSauGiam)
                    .idSanPhamChiTiet(idSpct)
                    .hinhAnh(tenAnh)
                    .thuongHieu(tenThuongHieu)
                    .mauSac(danhSachMauSac)
                    .build();
        });
    }


}
