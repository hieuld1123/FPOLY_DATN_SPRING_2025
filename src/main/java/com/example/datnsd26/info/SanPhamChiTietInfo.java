package com.example.datnsd26.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamChiTietInfo {
    String key;
    Integer idThuongHieu;
    Integer idDeGiay;
    Integer idKichCo;
    Integer idMauSac;
    Integer idChatLieu;
    Boolean gioitinh;
    Boolean trangthai;
}
