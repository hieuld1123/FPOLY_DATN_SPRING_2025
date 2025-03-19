package com.example.datnsd26.controller.api;

import com.example.datnsd26.controller.request.InvoiceParamRequest;
import com.example.datnsd26.controller.response.ApiResponse;
import com.example.datnsd26.services.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/hoa-don")
@RequiredArgsConstructor
public class HoaDonApiController {

    private final HoaDonService hoaDonService;

    @GetMapping
    public ApiResponse getHoaDon(InvoiceParamRequest request) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách hóa đơn")
                .data(hoaDonService.getInvoices(request))
                .build();
    }

    @GetMapping("/{code}")
    public ApiResponse getInvoice(@PathVariable String code) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Hóa đơn")
                .data(hoaDonService.getInvoice(code))
                .build();
    }
}
