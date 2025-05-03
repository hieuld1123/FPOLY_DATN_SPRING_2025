USE DEMO_SECURITY;

-- Khai báo biến một lần
DECLARE @i INT;
DECLARE @ngay DATE;
DECLARE @soLuong INT;
DECLARE @tongTien FLOAT;
DECLARE @hoaDonId INT;

-- 12 hóa đơn năm 2024
SET @i = 1;
WHILE @i <= 12
BEGIN
    SET @ngay = DATEFROMPARTS(2024, @i, 15);
    SET @soLuong = @i;
    SET @tongTien = @soLuong * 500000;

INSERT INTO hoa_don (
    ma_hoa_don, ngay_tao, ngay_cap_nhat, hinh_thuc_mua_hang, ten_nguoi_nhan,
    sdt_nguoi_nhan, email, tinh, quan, xa, dia_chi_nguoi_nhan,
    phi_van_chuyen, tong_tien, phuong_thuc_thanh_toan, ghi_chu,
    trang_thai, thanh_toan, id_khach_hang, id_nhan_vien, id_voucher,
    giam_gia, thanh_tien
) VALUES (
             CONCAT('HD2024', FORMAT(@i, '00')), @ngay, @ngay, N'Offline', NULL,
             NULL, NULL, NULL, NULL, NULL, NULL,
             0, @tongTien, NULL, NULL,
             N'Hoàn thành', 1, NULL, 1, NULL,
             0, @tongTien
         );

SET @hoaDonId = SCOPE_IDENTITY();

INSERT INTO hoa_don_chi_tiet (
    id_hoa_don, id_san_pham_chi_tiet, so_luong, gia_tien_sau_giam
) VALUES (
             @hoaDonId, 1, @soLuong, @tongTien
         );

-- Thêm lịch sử hóa đơn
INSERT INTO lich_su_hoa_don (id_hoa_don, trang_thai, thoi_gian)
VALUES
    (@hoaDonId, N'Đặt hàng', @ngay),
    (@hoaDonId, N'Hoàn thành', @ngay);

SET @i = @i + 1;
END;

-- 10 hóa đơn tháng 4/2025
SET @i = 1;
WHILE @i <= 10
BEGIN
    SET @ngay = DATEFROMPARTS(2025, 4, @i);
    SET @soLuong = @i + 1;
    SET @tongTien = @soLuong * 500000;

INSERT INTO hoa_don (
    ma_hoa_don, ngay_tao, ngay_cap_nhat, hinh_thuc_mua_hang, ten_nguoi_nhan,
    sdt_nguoi_nhan, email, tinh, quan, xa, dia_chi_nguoi_nhan,
    phi_van_chuyen, tong_tien, phuong_thuc_thanh_toan, ghi_chu,
    trang_thai, thanh_toan, id_khach_hang, id_nhan_vien, id_voucher,
    giam_gia, thanh_tien
) VALUES (
             CONCAT('HD0425', FORMAT(@i, '00')), @ngay, @ngay, N'Offline', NULL,
             NULL, NULL, NULL, NULL, NULL, NULL,
             0, @tongTien, NULL, NULL,
             N'Hoàn thành', 1, NULL, 1, NULL,
             0, @tongTien
         );

SET @hoaDonId = SCOPE_IDENTITY();

INSERT INTO hoa_don_chi_tiet (
    id_hoa_don, id_san_pham_chi_tiet, so_luong, gia_tien_sau_giam
) VALUES (
             @hoaDonId, 1, @soLuong, @tongTien
         );

-- Thêm lịch sử hóa đơn
INSERT INTO lich_su_hoa_don (id_hoa_don, trang_thai, thoi_gian)
VALUES
    (@hoaDonId, N'Đặt hàng', @ngay),
    (@hoaDonId, N'Hoàn thành', @ngay);

SET @i = @i + 1;
END;