package com.example.datnsd26.controller.api;

import com.example.datnsd26.controller.response.ApiResponse;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.SanPhamRepositoty;
import com.example.datnsd26.services.binhsanpham.PublicSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class PublicProductApiController {
    private final PublicSanPhamService publicSanPhamService;

    @GetMapping("/all")
    public ApiResponse getAllProducts() {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách sản phẩm")
                .data(publicSanPhamService.getAllProducts())
                .build();
    }

}
