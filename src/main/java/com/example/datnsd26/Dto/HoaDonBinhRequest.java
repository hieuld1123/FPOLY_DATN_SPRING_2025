package com.example.datnsd26.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HoaDonBinhRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String tenNguoiNhan;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[2-9]|84[2-9])[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String sdtNguoiNhan;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Vui lòng chọn tỉnh/thành phố")
    private String tinh;

    @NotBlank(message = "Vui lòng chọn quận/huyện")
    private String quan;

    @NotBlank(message = "Vui lòng chọn xã/phường")
    private String xa;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    @Size(min = 3, max = 100, message = "Địa chỉ cụ thể phải từ 3 đến 100 ký tự")
    private String diaChiNguoiNhan;

    private String phuongThucThanhToan;

    @Size(max = 100, message = "Ghi chú không được vượt quá 100 ký tự")
    private String ghiChu;

    private Long idVoucher; // sẽ là null nếu không áp dụng

    private Float giamGia = 0.0f; // mặc định là 0 nếu không có

}
