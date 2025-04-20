package com.example.datnsd26.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoucherResponse {
    private Long id;
    private String maVoucher;
    private String tenVoucher;
    private String hinhThucGiam;
    private Integer soLuong;
    private Float giaTriGiam;
    private Float giaTriGiamToiThieu;
    private Float giaTriGiamToiDa;
}
