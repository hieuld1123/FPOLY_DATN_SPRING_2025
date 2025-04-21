use DEMO_SECURITY
GO

INSERT INTO san_pham (ten_san_pham, ma_san_pham, ngay_cap_nhat, ngay_tao, trang_thai)
VALUES ('Vintas Denim', 'SP01', GETDATE(), GETDATE(), 1);

--
INSERT INTO kich_co (ten)
VALUES
    (N'35'),
    (N'36'),
    (N'37'),
    (N'38'),
    (N'39'),
    (N'40'),
    (N'41'),
    (N'42'),
    (N'43'),
    (N'44')


--
    INSERT INTO thuong_hieu(ten)
VALUES
    (N'Nike'),
    (N'Adidas'),
    (N'Asics'),
    (N'Clarks'),
    (N'Gucci'),
    (N'Puma'),
    (N'Birkenstock'),
    (N'Salvatore Ferragamo'),
    (N'Timberland'),
    (N'New Balance')


--
INSERT INTO mau_sac (ten, ten_mau_sac)
VALUES
    (N'#2e53c2', N'xanh dương đậm'),
    (N'#f76808', N'cam cháy'),
    (N'#f50505', N'đỏ tươi'),
    (N'#030303', N'đen'),
    (N'#77e92b', N'xanh lá non'),
    (N'#288a6a', N'xanh ngọc lục bảo'),
    (N'#0eccfb', N'xanh cyan'),
    (N'#654206', N'nâu đất'),
    (N'#d1236f', N'hồng đậm'),
    (N'#003b99', N'xanh dương')

--
INSERT INTO chat_lieu (ten)
VALUES
    (N'Nylon'),
    (N'Nhựa'),
    (N'Lụa'),
    (N'Vải'),
    (N'Da tổng hợp'),
    (N'Da bò'),
    (N'Canvas'),
    (N'Suede'),
    (N'Vải dù'),
    (N'Nỉ')


--
INSERT INTO de_giay (ten)
VALUES
    (N'Super Soft Ground'),
    (N'Soft Ground'),
    (N'Artificial Ground'),
    (N'Turf'),
    (N'Hard Ground'),
    (N'Indoor Court'),
    (N'Multi Ground'),
    (N'Soft Ground Pro'),
    (N'Firm Ground'),
    (N'Hard Ground')


INSERT INTO san_pham_chi_tiet
(ma_san_pham_chi_tiet, qr_code, mo_ta, gioi_tinh, so_luong, gia_ban, gia_ban_sau_giam, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham, id_kich_co, id_mau_sac, id_chat_lieu, id_de_giay, id_thuong_hieu)
VALUES
    ('SP01-CT1-35', NULL, N'Biến thể của Vintas Denim', 1, 100, 500000, 500000, GETDATE(), GETDATE(), 1, 1, 1, 1, 1, 1, 1),
    ('SP01-CT1-36', NULL, N'Biến thể của Vintas Denim', 1, 100, 500000, 500000, GETDATE(), GETDATE(), 1, 1, 2, 1, 1, 1, 1),
    ('SP01-CT1-37', NULL, N'Biến thể của Vintas Denim', 1, 100, 500000, 500000, GETDATE(), GETDATE(), 1, 1, 3, 1, 1, 1, 1),
    ('SP01-CT1-38', NULL, N'Biến thể của Vintas Denim', 1, 100, 500000, 500000, GETDATE(), GETDATE(), 1, 1, 4, 1, 1, 1, 1),

    ('SP01-CT2-35', NULL, N'Biến thể của Vintas Denim', 1, 100, 100000, 100000, GETDATE(), GETDATE(), 1, 1, 1, 2, 1, 1, 1),
    ('SP01-CT2-36', NULL, N'Biến thể của Vintas Denim', 1, 100, 100000, 100000, GETDATE(), GETDATE(), 1, 1, 2, 2, 1, 1, 1),
    ('SP01-CT2-37', NULL, N'Biến thể của Vintas Denim', 1, 100, 100000, 100000, GETDATE(), GETDATE(), 1, 1, 3, 2, 1, 1, 1),

    ('SP01-CT3-35', NULL, N'Biến thể của Vintas Denim', 1, 100, 100000, 100000, GETDATE(), GETDATE(), 1, 1, 1, 3, 1, 1, 1),
    ('SP01-CT3-36', NULL, N'Biến thể của Vintas Denim', 1, 100, 100000, 100000, GETDATE(), GETDATE(), 1, 1, 2, 3, 1, 1, 1);

-- Ảnh cho nhóm SP01-CT1 (màu sắc 1, kích cỡ 1, 2, 3, 4)
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/pro_AV00142_2-500x500.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 1 AND id_mau_sac = 1;

INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/pro_AV00172_2-500x500.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 1 AND id_mau_sac = 2;

INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/pro_AV00211_2-1-500x500.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 1 AND id_mau_sac = 3;

select * from mau_sac

select * from hinh_anh

INSERT INTO tai_khoan (email, sdt, matKhau, vaiTro, trangThai, reset_token)
VALUES ('admin@gmail.com', '0123456789', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'ADMIN', 1, NULL);
INSERT INTO tai_khoan (email, sdt, matKhau, vaiTro, trangThai, reset_token)
VALUES ('employee@gmail.com', '0123456789', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'EMPLOYEE', 1, NULL);

select * from tai_khoan
select * from nhan_vien

INSERT INTO nhan_vien (id_tai_khoan, ma_nhan_vien, ho_ten, dia_chi_cu_the, tinh_thanh_pho, quan_huyen, xa_phuong, gioi_tinh, hinh_anh, ngay_sinh, trang_thai, ngay_tao, ngay_cap_nhat)
VALUES (1, 'NVADMIN01', N'Nguyễn Văn An', N'123 Đường ABC, Phường XYZ', N'Hà Nội', N'Ba Đình', N'Phúc Xá', 1, null, '1990-01-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO nhan_vien (id_tai_khoan, ma_nhan_vien, ho_ten, dia_chi_cu_the, tinh_thanh_pho, quan_huyen, xa_phuong, gioi_tinh, hinh_anh, ngay_sinh, trang_thai, ngay_tao, ngay_cap_nhat)
VALUES (2, 'NV00002', N'Hoàng Thị Hà Lan', N'123 Đường Nguyễn Lan', N'Hà Nội', N'Hà Đông', N'Văn Quán', 2, null, '1990-01-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
