use DEMO_SECURITY
GO

INSERT INTO san_pham (ten_san_pham, ma_san_pham, ngay_cap_nhat, ngay_tao, trang_thai)
VALUES (N'Vintas Denim', 'SP01', GETDATE(), GETDATE(), 1),
		(N'Giày Jeep Lifestyle Shoes', 'SP02', GETDATE(), GETDATE(), 1),
		(N'Adilette Shower', 'SP03', GETDATE(), GETDATE(), 1),
		(N'Air Jordan 1 Mid Hyper Royal', 'SP04', GETDATE(), GETDATE(), 1),
		(N'Reebok Zig Kinetica Concep', 'SP05', GETDATE(), GETDATE(), 1),
        (N'Nike Lebron Witness IV', 'SP06', GETDATE(), GETDATE(), 1),
        (N'Nike React Vision', 'SP07', GETDATE(), GETDATE(), 1),
        (N'Fila Ray Tracer', 'SP08', GETDATE(), GETDATE(), 1),
        (N'Supercourt', 'SP09', GETDATE(), GETDATE(), 1),
        (N'MLB Big Ball Chunky', 'SP10', GETDATE(), GETDATE(), 1),
        (N'Reebok Club C', 'SP11', GETDATE(), GETDATE(), 1),
        (N'Vans Style 36', 'SP12', GETDATE(), GETDATE(), 1),
        (N'Puma RS IRI 2020', 'SP13', GETDATE(), GETDATE(), 1)

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
    (N'Jeep'),
    (N'Reebok'),--4
    (N'Puma'), --5
    (N'Fila'), --6
    (N'Vans'), --7
    (N'MLB') --8

--
INSERT INTO mau_sac (ten, ten_mau_sac)
VALUES
    (N'#f4ece3', N'Be trắng'), --1
    (N'#f7f5fa', N'Trắng'), --2
    (N'#2e53c2', N'xanh dương đậm'), --3
    (N'#f76808', N'cam cháy'),
    (N'#f50505', N'đỏ tươi'), --5
    (N'#030303', N'đen'),
    (N'#77e92b', N'xanh lá non'),
    (N'#288a6a', N'xanh ngọc lục bảo'), --8
    (N'#0eccfb', N'xanh cyan'),
    (N'#654206', N'nâu đất'), --10
    (N'#d1236f', N'hồng đậm'),
    (N'#003b99', N'xanh dương'),
    (N'#443b6f', N'xanh tím'), --13
    (N'#e0c7bf', N'hồng be') --14



--
INSERT INTO chat_lieu (ten)
VALUES
    (N'Canvas'),
    (N'Lụa'),
    (N'Vải'),
    (N'Da tổng hợp'),
    (N'Da bò'),
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

    ('SP01-CT2-35', NULL, N'Biến thể của Vintas Denim', 1, 100, 1000000, 1000000, GETDATE(), GETDATE(), 1, 1, 1, 2, 1, 1, 1),
    ('SP01-CT2-36', NULL, N'Biến thể của Vintas Denim', 1, 100, 1000000, 1000000, GETDATE(), GETDATE(), 1, 1, 2, 2, 1, 1, 1),
    ('SP01-CT2-37', NULL, N'Biến thể của Vintas Denim', 1, 100, 1000000, 1000000, GETDATE(), GETDATE(), 1, 1, 3, 2, 1, 1, 1),

    ('SP01-CT3-35', NULL, N'Biến thể của Vintas Denim', 1, 100, 1000000, 1000000, GETDATE(), GETDATE(), 1, 1, 1, 3, 1, 1, 1),
    ('SP01-CT3-36', NULL, N'Biến thể của Vintas Denim', 1, 100, 1000000, 1000000, GETDATE(), GETDATE(), 1, 1, 2, 3, 1, 1, 1),

    --spct nhom2
    ('SP02-CT1-40', NULL, N'Chưa có mô tả', 1, 100, 1000000, 1000000, GETDATE(), GETDATE(), 1, 2, 6, 1, 3, 3, 3),

    --spct nhom3
    ('SP03-CT1-40', NULL, N'Chưa có mô tả', 1, 100, 1000000, 1000000, GETDATE(), GETDATE(), 1, 3, 6, 2, 2, 2, 2),
    ('SP03-CT1-41', NULL, N'Chưa có mô tả', 1, 100, 1000000, 1000000, GETDATE(), GETDATE(), 1, 3, 7, 2, 2, 2, 2),

    --spct nhom4                                                                              _SP   _M       _TH
    ('SP04-CT1-40', NULL, N'Chưa có mô tả', 1, 100, 2000000, 2000000, GETDATE(), GETDATE(), 1, 4, 6, 3, 3, 3, 1),
    ('SP04-CT2-40', NULL, N'Chưa có mô tả', 1, 100, 2000000, 2000000, GETDATE(), GETDATE(), 1, 4, 6, 10, 3, 3, 1),

    --spct nhom5                                                                              _SP   _M       _TH
    ('SP05-CT1-40', NULL, N'Chưa có mô tả', 1, 100, 2000000, 2000000, GETDATE(), GETDATE(), 1, 5, 6, 5, 4, 4, 4),
    ('SP05-CT2-40', NULL, N'Chưa có mô tả', 1, 100, 2000000, 2000000, GETDATE(), GETDATE(), 1, 5, 6, 8, 4, 4, 4),

    --spct nhom6                                                                              _SP   _M       _TH
    ('SP06-CT1-40', NULL, N'Chưa có mô tả', 1, 100, 2000000, 2000000, GETDATE(), GETDATE(), 1, 6, 6, 13, 5, 5, 1),

    --spct nhom7                                                                              _SP   _M       _TH
    ('SP07-CT1-40', NULL, N'Chưa có mô tả', 1, 100, 1499000, 1499000, GETDATE(), GETDATE(), 1, 7, 6, 6, 6, 6, 1),

    --spct nhom8                                                                              _SP   _M       _TH
    ('SP08-CT1-41', NULL, N'Chưa có mô tả', 1, 100, 1499000, 1499000, GETDATE(), GETDATE(), 1, 8, 7, 1, 7, 7, 6),

    --spct nhom9                                                                              _SP   _M        _TH
    ('SP09-CT1-41', NULL, N'Chưa có mô tả', 1, 100, 1499000, 1499000, GETDATE(), GETDATE(), 1, 9, 7, 14, 7, 7, 2),

    --spct nhom10                                                                              _SP   _M        _TH
    ('SP10-CT1-41', NULL, N'Chưa có mô tả', 1, 100, 1699000, 1699000, GETDATE(), GETDATE(), 1, 10, 7, 1, 7, 7, 8),

    --spct nhom11                                                                              _SP   _M        _TH
    ('SP11-CT1-41', NULL, N'Chưa có mô tả', 1, 100, 1699000, 1699000, GETDATE(), GETDATE(), 1, 11, 7, 3, 2, 2, 4),

    --spct nhom12                                                                              _SP   _M        _TH
    ('SP12-CT1-41', NULL, N'Chưa có mô tả', 1, 100, 1699000, 1699000, GETDATE(), GETDATE(), 1, 12, 7, 5, 2, 2, 7),

    --spct nhom13                                                                            _SP   _M        _TH
    ('SP13-CT1-41', NULL, N'Chưa có mô tả', 1, 100, 250000, 250000, GETDATE(), GETDATE(), 1, 13, 7, 6, 2, 2, 5)

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

--SP02
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/Jeep.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 2 AND id_mau_sac = 1;

--SP03
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/AdiletteShower.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 3 AND id_mau_sac = 2;

--SP04
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp04_1.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 4 AND id_mau_sac = 10;
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp04_2.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 4 AND id_mau_sac = 3;

--SP05
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp05_1.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 5 AND id_mau_sac = 5;
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp05_2.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 5 AND id_mau_sac = 8;

--SP06
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp06_13.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 6 AND id_mau_sac = 13;

--SP07
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp07_6.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 7 AND id_mau_sac = 6;

--SP08
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp08_1.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 8 AND id_mau_sac = 1;

--SP09
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp09_14.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 9 AND id_mau_sac = 14;

--SP10
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp10_1.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 10 AND id_mau_sac = 1;

--SP11
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp11_3.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 11 AND id_mau_sac = 3;

--SP12
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp12_5.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 12 AND id_mau_sac = 5;

--SP13
INSERT INTO hinh_anh (ten_anh, ngay_tao, ngay_cap_nhat, trang_thai, id_san_pham_chi_tiet)
SELECT '/upload/sp13_6.jpg',
       GETDATE(), GETDATE(), 1, id
FROM san_pham_chi_tiet
WHERE id_san_pham = 13 AND id_mau_sac = 6;



select * from tai_khoan
select * from khach_hang


INSERT INTO tai_khoan (email, sdt, matKhau, vaiTro, trangThai, reset_token) VALUES
('admin@gmail.com', '0988675432', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'ADMIN', 1, NULL),
('employee1@gmail.com', '0988097123', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'EMPLOYEE', 1, NULL),
('employee2@gmail.com', '0988005112', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'EMPLOYEE', 1, NULL),
('employee3@gmail.com', '0999678823', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'EMPLOYEE', 1, NULL),
('employee4@gmail.com', '0999111456', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'EMPLOYEE', 1, NULL),
('hoangquocbinh0411@gmail.com', '0912345678', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'CUSTOMER', 1, NULL),
('hieuldph30616@fpt.edu.vn', '0987654321', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'CUSTOMER', 1, NULL),
('khanhlinh241220000@gmail.com', '0988965124', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'CUSTOMER', 1, NULL),
('quannvph41619@fpt.edu.vn', '0988111506', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'CUSTOMER', 1, NULL),
('dangvank@gmail.com', '0988609453', '$2a$10$c/0f56WsPLRCLd7L0REp5uzShMtGqLFC/qpc/iMISdEypxiy7fntW', 'CUSTOMER', 1, NULL);

INSERT INTO nhan_vien (id_tai_khoan, ma_nhan_vien, ho_ten, dia_chi_cu_the, tinh_thanh_pho, quan_huyen, xa_phuong, gioi_tinh, hinh_anh, ngay_sinh, trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(1, 'NVADMIN01', N'Nguyễn Văn An', N'123 Đường ABC, Phường XYZ', N'Hà Nội', N'Ba Đình', N'Phúc Xá', 1, 'default.jpg', '1990-01-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'NV00002', N'Hoàng Thị Hà Lan', N'123 Đường Nguyễn Lan', N'Hà Nội', N'Hà Đông', N'Văn Quán', 2, 'default.jpg', '1990-01-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'NV00003', N'Nguyễn Thị Hồng', N'89 Nguyễn Trãi', N'Hà Nội', N'Thanh Xuân', N'Thanh Xuân Trung', 0, 'default.jpg', '1995-04-15', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'NV00004', N'Phạm Minh Tuấn', N'12 Lý Thường Kiệt', N'Đà Nẵng', N'Hải Châu', N'Nam Dương', 1, 'default.jpg', '1992-09-23', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'NV00005', N'Lê Quang Huy', N'33 Trần Phú', N'Hồ Chí Minh', N'Quận 5', N'Phường 2', 1, 'default.jpg', '1990-12-05', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO khach_hang(id_tai_khoan, ho_ten, ma_khach_hang, gioi_tinh, hinh_anh,ngay_sinh,trang_thai, ngay_tao, ngay_cap_nhat) VALUES
(6, N'Nguyễn Văn Anh', 'KH001', 1, 'default.jpg', '1995-05-20',1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, N'Lê Thị Bình', 'KH002', 0, 'default.jpg', '1992-10-15',1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, N'Phạm Văn Hoàng', 'KH003', 1, 'default.jpg', '1990-03-12',1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, N'Trần Thị Cúc', 'KH004', 0, 'default.jpg', '1998-07-07',1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, N'Đặng Văn Khánh', 'KH005', 1, 'default.jpg', '1993-12-01',1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


INSERT INTO dia_chi (dia_chi_cu_the, tinh_thanh_pho, quan_huyen, xa_phuong, trang_thai, id_khach_hang) VALUES
(N'123 Đường ABC', N'Hà Nội', N'Ba Đình', N'Phúc Xá', 1, 1),
(N'456 Đường DEF', N'Hồ Chí Minh', N'Quận 1', N'Bến Nghé', 1, 2),
(N'789 Đường GHI', N'Đà Nẵng', N'Hải Châu', N'Thạch Thang', 1, 3),
(N'101 Đường JKL', N'Cần Thơ', N'Ninh Kiều', N'An Khánh', 1, 4),
(N'202 Đường MNO', N'Hải Phòng', N'Lê Chân', N'An Dương', 1, 5);
