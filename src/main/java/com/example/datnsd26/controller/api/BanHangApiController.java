package com.example.datnsd26.controller.api;

import com.example.datnsd26.controller.response.ApiResponse;
import com.example.datnsd26.services.BanHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/ban-hang")
@RequiredArgsConstructor
public class BanHangApiController {
    private final BanHangService banHangService;

    @PostMapping("/hoa-don")
    public ApiResponse createHoaDon() {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(banHangService.createHoaDon())
                .build();
    }

    @GetMapping("/hoa-don")
    public ApiResponse getHoaDon() {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách hóa đơn")
                .data(banHangService.getHoaDon())
                .build();
    }

    @GetMapping("/hoa-don/{id}")
    public ApiResponse getHoaDonById(@PathVariable("id") Integer id) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Hóa đơn " + id)
                .data(banHangService.getHoaDonChiTiet(id))
                .build();
    }

    @GetMapping("/san-pham")
    public ApiResponse getSanPhamByName(@RequestParam(defaultValue = "") String keyword) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Products")
                .data(banHangService.getSanPhamByName(keyword))
                .build();
    }

    @PostMapping("/add-to-cart/{productId}/{invoiceId}")
    public ApiResponse addProduct(@PathVariable("productId") Integer productId, @PathVariable Integer invoiceId) {
        return ApiResponse.builder()
                .status(HttpStatus.ACCEPTED.value())
                .message("Add product")
                .data(banHangService.addToCart(productId, invoiceId))
                .build();
    }

    @PutMapping("/update-quantity/{invoiceId}/{quantity}")
    public ApiResponse updateQuantity(@PathVariable("invoiceId") Integer invoiceId, @PathVariable Integer quantity) {
        banHangService.updateSoLuong(invoiceId, quantity);
        return ApiResponse.builder()
                .status(HttpStatus.ACCEPTED.value())
                .message("Cap nhat so luong")
                .build();
    }

    @DeleteMapping("/delete-item/{itemId}")
    public ApiResponse deleteItem(@PathVariable Integer itemId) {
        banHangService.deleteItem(itemId);
        return ApiResponse.builder()
                .status(HttpStatus.ACCEPTED.value())
                .message("Xóa thành công")
                .build();
    }
}
