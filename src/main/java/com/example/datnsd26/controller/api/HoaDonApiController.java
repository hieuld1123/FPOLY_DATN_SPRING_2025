package com.example.datnsd26.controller.api;

import com.example.datnsd26.controller.request.InvoiceParamRequest;
import com.example.datnsd26.controller.request.InvoiceRecipientInfoRequest;
import com.example.datnsd26.controller.response.ApiResponse;
import com.example.datnsd26.services.HoaDonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
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

    @PatchMapping("/confirm/{code}")
    public ApiResponse confirmInvoice(@PathVariable String code) {
        hoaDonService.confirmInvoice(code);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Xác nhận đơn hàng")
                .build();
    }

    @PatchMapping("/delivery/{code}")
    public ApiResponse delivery(@PathVariable String code) {
        hoaDonService.confirmDelivery(code);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Xác nhận vận chuyển")
                .build();
    }

    @PatchMapping("/cancel/{code}")
    public ApiResponse cancel(@PathVariable String code) {
        hoaDonService.cancel(code);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Hủy hóa đơn thành công")
                .build();
    }

    @GetMapping("/edit/{code}")
    public ApiResponse edit(@PathVariable String code) {
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Xác nhận chỉnh sửa hóa đơn")
                .data(this.hoaDonService.editInvoice(code))
                .build();
    }

    @PatchMapping("/payment/{code}")
    public ApiResponse payment(@PathVariable String code) {
        hoaDonService.payment(code);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Thanh toán")
                .build();
    }

    @PatchMapping("/completed/{code}")
    public ApiResponse completed(@PathVariable String code) {
        hoaDonService.completed(code);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Thanh toán")
                .build();
    }

    @PutMapping("/update-recipient/{orderId}")
    public ApiResponse updateRecipient(@RequestBody InvoiceRecipientInfoRequest request, @PathVariable String orderId) {
        log.info("orderId: {}, new name: {}", orderId, request.getName());
        this.hoaDonService.updateRecipient(orderId, request);
        return ApiResponse.builder()
                .status(HttpStatus.ACCEPTED.value())
                .message("Cập nhật thông tin người nhận thành công")
                .build();
    }
}
