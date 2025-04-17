package com.example.datnsd26.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamInfo {
    private String key;
    private String trangThai; // Giữ nguyên kiểu String trong form để dễ dàng bind dữ liệu
    private Integer soLuong;

    // Phương thức chuyển trangThai sang Boolean
    public Boolean getTrangThaiBoolean() {
        if (this.trangThai == null || this.trangThai.isEmpty()) {
            return null; // Trạng thái null khi không chọn
        }
        return this.trangThai.equals("true"); // Chuyển từ String sang Boolean
    }
}


