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
    Boolean gioiTinh;
    String trangThai;

    // Phương thức chuyển trangThai sang Boolean
    public Boolean getTrangThaiBoolean() {
        if (this.trangThai == null || this.trangThai.isEmpty()) {
            return null; // Trạng thái null khi không chọn
        }
        return this.trangThai.equals("true"); // Chuyển từ String sang Boolean
    }
}
