package com.example.datnsd26.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HoaDonResponse {
    private Integer id;
    private String maHoaDon;
    private String tranThai;
}
