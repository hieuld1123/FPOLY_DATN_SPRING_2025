package com.example.datnsd26.services.impl;

import com.example.datnsd26.controller.response.HoaDonChiTietResponse;
import com.example.datnsd26.controller.response.HoaDonResponse;
import com.example.datnsd26.controller.response.SanPhamResponse;
import com.example.datnsd26.exception.EntityNotFound;
import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.models.HoaDonChiTiet;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.HoaDonChiTietRepository;
import com.example.datnsd26.repository.HoaDonRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.services.BanHangService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.example.datnsd26.utilities.CommonUtils.generateInvoiceCode;

@Service
@Slf4j(topic = "BAN-HANG-SERVICE")
@RequiredArgsConstructor
public class BanHangServiceImpl implements BanHangService {
    private final HoaDonRepository hoaDonRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    @Override
    public List<HoaDonResponse> getHoaDon() {
        return this.hoaDonRepository.findAll().stream().map(s -> HoaDonResponse.builder()
                .id(s.getId())
                .maHoaDon(s.getMaHoaDon())
                .tranThai(s.getTrangThai())
                .build()).toList();
    }

    @Override
    public HoaDonChiTietResponse getHoaDonChiTiet(int id) {
        var hoaDon = this.findHoaDonById(id);
        List<SanPhamResponse> listSanPham = hoaDon.getDanhSachSanPham().stream().map(sp ->
                SanPhamResponse.builder()
                        .id(sp.getId())
                        .maSanPham(sp.getSanPhamChiTiet().getSanPham().getMaSanPham())
                        .tenSanPham(String.format("%s [%s - %s]", sp.getSanPhamChiTiet().getSanPham().getTenSanPham(), sp.getSanPhamChiTiet().getMauSac().getTen(), sp.getSanPhamChiTiet().getKichThuoc().getSizeVn()))
                        .gia(sp.getSanPhamChiTiet().getGiaBan())
                        .soLuong(sp.getSoLuong())
                        .soLuongTonKho(sp.getSanPhamChiTiet().getSoLuong())
                        .hinhAnh("https://th.bing.com/th/id/OIP.8tQmmY_ccVpcxBxu0Z0mzwHaE8?rs=1&pid=ImgDetMain") // TODO
                        .build()
                ).toList();
        return HoaDonChiTietResponse.builder()
                .tongTien(hoaDon.getTongTien())
                .listSanPham(listSanPham)
                .build();
    }

    @Override
    public Integer createHoaDon() {
        HoaDon hoaDon = HoaDon.builder()
                .maHoaDon(generateInvoiceCode())
                .trangThai("Đang chờ")
                .tongTien(0f)
                .ngayTao(new Date())
                .ngayCapNhat(new Date())
                .build();
        return this.hoaDonRepository.save(hoaDon).getId();
    }

    @Override
    public List<SanPhamResponse> getSanPhamByName(String keyword) {
        log.info("get product keyword: {}", keyword);
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return this.sanPhamChiTietRepository.findByName(keyword).stream().map(sp -> SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getSanPham().getMaSanPham())
                .tenSanPham(String.format("%s [%s - %s]", sp.getSanPham().getTenSanPham(), sp.getMauSac().getTen(), sp.getKichThuoc().getSizeVn()))
                .gia(sp.getGiaBan())
                .soLuong(sp.getSoLuong())
                .hinhAnh("https://th.bing.com/th/id/OIP.8tQmmY_ccVpcxBxu0Z0mzwHaE8?rs=1&pid=ImgDetMain") // TODO
                .build()).toList();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public int addToCart(int productId, int invoiceId) {
        log.info("addToCart productId: {}, invoiceId: {}", productId, invoiceId);
        Optional<HoaDonChiTiet> invoice = this.hoaDonChiTietRepository.findHoaDonChiTietByIdAndProductId(invoiceId, productId);
        SanPhamChiTiet product = this.findSanPhamChiTietById(productId);
        HoaDon hoaDon = this.findHoaDonById(invoiceId);
        product.setSoLuong(product.getSoLuong() - 1);
        this.sanPhamChiTietRepository.save(product);
        if (invoice.isPresent()) {
            log.info("update invoice id: {}", invoiceId);
            HoaDonChiTiet hoaDonChiTiet = invoice.get();
            hoaDonChiTiet.setSoLuong(hoaDonChiTiet.getSoLuong() + 1);
            hoaDon.setTongTien(hoaDon.getTongTien() + product.getGiaBan());
            return this.hoaDonChiTietRepository.save(hoaDonChiTiet).getId();
        }
        log.info("addToCart invoice id: {}", invoiceId);
        HoaDonChiTiet hoaDonChiTiet = HoaDonChiTiet.builder()
                .sanPhamChiTiet(product)
                .hoaDon(hoaDon)
                .soLuong(1)
                .build();
        hoaDon.setTongTien(hoaDon.getTongTien() + product.getGiaBan()); // NOTE: Price
        return this.hoaDonChiTietRepository.save(hoaDonChiTiet).getId();
    }

    @Override
    public void updateSoLuong(int invoiceId,int quantity) {
        Optional<HoaDonChiTiet> invoice = this.hoaDonChiTietRepository.findById(invoiceId);
        if (invoice.isPresent()) {
            log.info("update invoice id: {}", invoiceId);
            HoaDonChiTiet hoaDonChiTiet = invoice.get();
            hoaDonChiTiet.setSoLuong(quantity);
            HoaDonChiTiet hoaDonChiTietSave = hoaDonChiTietRepository.save(hoaDonChiTiet);

            HoaDon hoaDon = hoaDonChiTietSave.getHoaDon();
            hoaDon.setTongTien((float) hoaDon.getDanhSachSanPham()
                    .stream()
                    .mapToDouble(sp -> sp.getSoLuong() * sp.getSanPhamChiTiet().getGiaBan())
                    .sum());
            hoaDonRepository.save(hoaDon);
        }
    }

    @Override
    public void deleteItem(int itemId) {
        HoaDonChiTiet hoaDonChiTiet = findHoaDonChiTietById(itemId);
        SanPhamChiTiet product = hoaDonChiTiet.getSanPhamChiTiet();
        product.setSoLuong(product.getSoLuong() + hoaDonChiTiet.getSoLuong());
        this.sanPhamChiTietRepository.save(product);
        this.hoaDonChiTietRepository.delete(hoaDonChiTiet);
        HoaDon hoaDon = findHoaDonById(hoaDonChiTiet.getHoaDon().getId());
        hoaDon.setTongTien((float) hoaDon.getDanhSachSanPham()
                .stream()
                .mapToDouble(sp -> sp.getSoLuong() * sp.getSanPhamChiTiet().getGiaBan())
                .sum());
        hoaDonRepository.save(hoaDon);
    }

    private HoaDonChiTiet findHoaDonChiTietById(int id) {
        return this.hoaDonChiTietRepository.findById(id).orElseThrow(() -> new EntityNotFound("Không tìm thấy hóa đơn!"));
    }

    private HoaDon findHoaDonById(Integer id) {
        return this.hoaDonRepository.findById(id).orElseThrow(() -> new EntityNotFound("Không tìm thấy hóa đơn!"));
    }

    private SanPhamChiTiet findSanPhamChiTietById(Integer id) {
        return this.sanPhamChiTietRepository.findById(id).orElseThrow(() -> new EntityNotFound("Không tìm sản phẩm chi tiet!"));
    }
}
