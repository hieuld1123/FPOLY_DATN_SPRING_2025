package com.example.datnsd26.controller.api;

import com.example.datnsd26.controller.response.ApiResponse;
import com.example.datnsd26.controller.response.PublicSanPhamResponse;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.SanPhamRepositoty;
import com.example.datnsd26.services.binhsanpham.PublicSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class PublicProductApiController {
    private final PublicSanPhamService publicSanPhamService;

    @GetMapping("/all")
    public ApiResponse getAllProducts(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PublicSanPhamResponse> products = publicSanPhamService.getAllProducts(pageable);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách sản phẩm")
                .data(products)
                .build();
    }

}
