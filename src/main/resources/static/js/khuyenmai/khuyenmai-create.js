document.addEventListener('DOMContentLoaded', async function () {
    const form = document.getElementById('khuyenMaiForm');
    const hinhThucGiam = document.getElementById('hinhThucGiam');
    const tenChienDich = document.getElementById("tenChienDichInput");
    const giaTriGiam = document.getElementById('giaTriGiam');
    const thoiGianBatDau = document.getElementById('thoiGianBatDau');
    const thoiGianKetThuc = document.getElementById('thoiGianKetThuc');
    const selectAll = document.getElementById('selectAll');
    const checkboxes = document.querySelectorAll('.chonSanPham');
    const mucGiamInputs = document.querySelectorAll('.mucGiam');
    const giaGocCells = document.querySelectorAll('.giaGoc');
    const giaSauGiamCells = document.querySelectorAll('.giaSauGiam');
    const giaTriGiamUnit = document.getElementById('giaTriGiamUnit');
    const mucGiamUnits = document.querySelectorAll('.mucGiamUnit');
    let currentPage = 0;

    function fetchSanPham(keyword, page = 0) {
        fetch(`/admin/khuyen-mai/product-search?keyword=${keyword}&page=${page}`)
            .then(response => response.json())
            .then(data => {
                let suggestions = document.getElementById("suggestions");
                let pagination = document.getElementById("pagination");
                suggestions.innerHTML = "";
                pagination.innerHTML = "";

                if (!data || !data.content || data.content.length === 0) {
                    suggestions.innerHTML = "<div class='list-group-item'>Không tìm thấy sản phẩm</div>";
                    return;
                }

                data.content.forEach(sp => {
                    let item = document.createElement("button");
                    item.classList.add("list-group-item", "list-group-item-action");
                    item.textContent = sp.tenSanPham;
                    item.onclick = function () {
                        document.getElementById("searchSanPham").value = sp.tenSanPham;
                        suggestions.innerHTML = "";
                    };
                    suggestions.appendChild(item);
                });

                if (data.totalPages > 1) {
                    for (let i = 0; i < data.totalPages; i++) {
                        let btn = document.createElement("button");
                        btn.classList.add("btn", "btn-sm", "btn-primary", "mx-1");
                        btn.textContent = i + 1;
                        btn.onclick = function () {
                            currentPage = i;
                            fetchSanPham(keyword, i);
                        };
                        pagination.appendChild(btn);
                    }
                }
            })
            .catch(error => {
                console.error("Lỗi khi gọi API: ", error);
                document.getElementById("suggestions").innerHTML = "<div class='list-group-item text-danger'>Lỗi hệ thống</div>";
            });
    }

    document.getElementById("searchSanPham").addEventListener("input", function () {
        let keyword = this.value.trim();
        if (keyword.length > 2) {
            currentPage = 0;
            fetchSanPham(keyword);
        } else {
            document.getElementById("suggestions").innerHTML = "";
            document.getElementById("pagination").innerHTML = "";
        }
    });
// Ẩn danh sách khi click ra ngoài
    document.addEventListener("click", function (event) {
        let suggestions = document.getElementById("suggestions");
        let searchBox = document.getElementById("searchSanPham");
        if (!searchBox.contains(event.target) && !suggestions.contains(event.target)) {
            suggestions.innerHTML = "";
            document.getElementById("pagination").innerHTML = "";
        }
    });

    function showToastError(message) {
        let toastEl = document.getElementById('errorToast');
        let toastBody = toastEl.querySelector('.toast-body');
        toastBody.textContent = message;

        let toast = new bootstrap.Toast(toastEl, {delay: 3000}); // Hiển thị 3 giây
        toast.show();
    }

    let sanPhamDangKhuyenMai = [];

    async function fetchSanPhamKhuyenMai() {
        try {
            const response = await fetch('/admin/khuyen-mai/san-pham-khuyen-mai');
            if (response.ok) {
                sanPhamDangKhuyenMai = await response.json();
                console.log('Danh sách sản phẩm đang khuyến mãi:', sanPhamDangKhuyenMai);
            }
        } catch (error) {
            console.error('Lỗi khi tải danh sách sản phẩm khuyến mãi:', error);
        }
    }

    // Sự kiện "change" cho từng checkbox sản phẩm
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function () {
            const productId = this.getAttribute('data-product-id'); // lấy dưới dạng chuỗi
            if (this.checked && sanPhamDangKhuyenMai.map(id => id.toString()).includes(productId)) {
                showToastError('Sản phẩm này đã có khuyến mãi đang diễn ra!');
                this.checked = false;
            }
        });
    });

    function formatCurrency(value) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(value);
    }

    function updateGiaTriGiamUnit() {
        const isPercent = hinhThucGiam.value === 'Phần Trăm';
        const unit = isPercent ? '%' : 'VNĐ';
        giaTriGiamUnit.textContent = unit;
        mucGiamUnits.forEach(el => el.textContent = unit);

        if (isPercent) {
            giaTriGiam.max = '100';
            giaTriGiam.step = '0.1';
            mucGiamInputs.forEach(input => {
                if (!input.disabled) {
                    input.max = '100';
                    input.step = '0.1';
                    if (parseFloat(input.value) > 100) {
                        input.value = '100';
                    }
                }
            });
        } else {
            giaTriGiam.removeAttribute('max');
            giaTriGiam.step = '1000';
            mucGiamInputs.forEach(input => {
                if (!input.disabled) {
                    input.removeAttribute('max');
                    input.step = '1000';
                }
            });
        }
        updateGiaSauGiam();
    }

    // Sự kiện "change" cho selectAll với kiểm tra sản phẩm chưa có KM
    selectAll.addEventListener('change', function () {
        let hasSelected = false;
        // Chuyển danh sách sản phẩm có KM thành chuỗi
        const dsSanPhamKM = sanPhamDangKhuyenMai.map(id => id.toString());

        checkboxes.forEach((checkbox, index) => {
            const productId = checkbox.getAttribute('data-product-id'); // lấy chuỗi
            if (!dsSanPhamKM.includes(productId)) {
                checkbox.checked = this.checked;
                const mucGiamInput = mucGiamInputs[index];
                if (this.checked) {
                    mucGiamInput.disabled = false;
                    mucGiamInput.value = giaTriGiam.value;
                } else {
                    mucGiamInput.disabled = true;
                    mucGiamInput.value = '';
                }
                hasSelected = true;
            } else {
                checkbox.checked = false;
            }
        });

        if (!hasSelected && this.checked) {
            showToastError("Tất cả sản phẩm đều đã có khuyến mãi, không thể chọn!");
            this.checked = false;
        }

        updateGiaSauGiam();
    });

    function tinhGiaSauGiam(giaGoc, mucGiam) {
        const isPercent = hinhThucGiam.value === 'Phần Trăm';
        if (isPercent) {
            return giaGoc * (1 - mucGiam / 100);
        }
        return Math.max(0, giaGoc - mucGiam);
    }

    function updateGiaSauGiam() {
        checkboxes.forEach((checkbox, index) => {
            const mucGiamInput = mucGiamInputs[index];
            const giaGoc = parseFloat(giaGocCells[index].dataset.gia);
            const giaSauGiamCell = giaSauGiamCells[index];

            if (checkbox.checked) {
                mucGiamInput.disabled = false;
                const mucGiam = parseFloat(mucGiamInput.value) || 0;
                const giaSauGiam = tinhGiaSauGiam(giaGoc, mucGiam);
                giaSauGiamCell.textContent = formatCurrency(giaSauGiam);
            } else {
                mucGiamInput.disabled = true;
                mucGiamInput.value = '';
                giaSauGiamCell.textContent = '';
            }
        });
    }

    hinhThucGiam.addEventListener('change', updateGiaTriGiamUnit);

    giaTriGiam.addEventListener('input', () => {
        const value = parseFloat(giaTriGiam.value) || 0;
        mucGiamInputs.forEach(input => {
            if (!input.disabled) {
                input.value = value;
            }
        });
        updateGiaSauGiam();
    });

    // Sự kiện cho từng checkbox sản phẩm (đã có ở trên)

    checkboxes.forEach((checkbox, index) => {
        checkbox.addEventListener('change', () => {
            const mucGiamInput = mucGiamInputs[index];
            if (checkbox.checked) {
                mucGiamInput.disabled = false;
                mucGiamInput.value = giaTriGiam.value;
            } else {
                mucGiamInput.disabled = true;
                mucGiamInput.value = '';
            }
            updateGiaSauGiam();

            const allChecked = Array.from(checkboxes).every(cb => cb.checked);
            const someChecked = Array.from(checkboxes).some(cb => cb.checked);
            selectAll.checked = allChecked;
            selectAll.indeterminate = someChecked && !allChecked;
        });
    });

    mucGiamInputs.forEach(input => {
        input.addEventListener('input', updateGiaSauGiam);
    });

    thoiGianBatDau.addEventListener('change', function () {
        thoiGianKetThuc.min = this.value;
        if (thoiGianKetThuc.value && thoiGianKetThuc.value < this.value) {
            thoiGianKetThuc.value = this.value;
        }
    });

    // Form Validation
    form.addEventListener('submit', function (event) {
        event.preventDefault();

        let isValid = true;
        let errorMessage = '';

        // Kiểm tra tên chiến dịch
        if (!tenChienDich.value.trim()) {
            errorMessage = 'Vui lòng nhập tên chiến dịch khuyến mãi';
            isValid = false;
        }

        // Kiểm tra hình thức giảm giá
        if (!hinhThucGiam.value) {
            errorMessage = 'Vui lòng chọn hình thức giảm giá';
            isValid = false;
        }

        // Kiểm tra giá trị giảm chung
        if (!giaTriGiam.value.trim()) {
            errorMessage = 'Vui lòng nhập giá trị giảm chung';
            isValid = false;
        } else if (parseFloat(giaTriGiam.value) < 0) {
            errorMessage = 'Giá trị giảm không được âm';
            isValid = false;
        }


        // Kiểm tra giá trị giảm chung
        const isPercent = hinhThucGiam.value === 'Phần Trăm';
        if (isPercent && parseFloat(giaTriGiam.value) > 100 || parseFloat(giaTriGiam.value) <= 0 ) {
            errorMessage = 'Giá trị giảm chung theo phần trăm phải từ 1% đến 100% ';
            isValid = false;
        }
        const isPercentt = hinhThucGiam.value === 'Theo Giá Tiền';
        if (isPercentt && parseFloat(giaTriGiam.value) <= 0 ) {
            errorMessage = 'Giá trị giảm chung  theo giá tiền phải lớn hơn 0 ';
            isValid = false;
        }


        // Kiểm tra ngày bắt đầu & ngày kết thúc không bỏ trống
        if (!thoiGianBatDau.value || !thoiGianKetThuc.value) {
            errorMessage = 'Vui lòng chọn ngày bắt đầu và ngày kết thúc';
            isValid = false;
        } else {
            let batDau = new Date(thoiGianBatDau.value);
            let ketThuc = new Date(thoiGianKetThuc.value);
            let ngayHienTai = new Date();

            // Đặt giây & mili-giây về 0 để không tính giây
            batDau.setSeconds(0, 0);
            ketThuc.setSeconds(0, 0);
            ngayHienTai.setSeconds(0, 0);

            // Kiểm tra ngày bắt đầu không được là quá khứ (không tính giây)
            if (batDau < ngayHienTai) {
                errorMessage = 'Ngày bắt đầu không được là quá khứ';
                isValid = false;
            }

            // Ngày kết thúc phải sau ngày bắt đầu
            if (ketThuc <= batDau) {
                errorMessage = 'Thời gian kết thúc phải sau thời gian bắt đầu';
                isValid = false;
            }
        }

        // Kiểm tra sản phẩm được chọn
        const hasSelectedProducts = Array.from(checkboxes).some(cb => cb.checked);
        if (!hasSelectedProducts) {
            errorMessage = 'Vui lòng chọn ít nhất một sản phẩm';
            isValid = false;
        }


        checkboxes.forEach((checkbox, index) => {
            if (checkbox.checked) {
                const mucGiamInput = mucGiamInputs[index];
                const giaGoc = parseFloat(giaGocCells[index].dataset.gia);
                const mucGiam = parseFloat(mucGiamInput.value);

                if (!mucGiam) {
                    errorMessage ="vui lòng nhập mức giảm";
                    isValid = false;
                }

                // if ( mucGiam <= 0) {
                //     errorMessage = ' mức giảm phải lớn hơn 0';
                //     isValid = false;
                // }

                if (!isPercent && mucGiam >= giaGoc) {
                    errorMessage = 'Mức giảm không được lớn hơn hoặc bằng giá gốc của sản phẩm';
                    isValid = false;
                }

                if (hinhThucGiam.value === "Phần Trăm" && mucGiam > 100 || mucGiam <= 0 ) {
                    errorMessage = 'Mức giảm theo phần trăm phải từ 1% đến 100% ';
                    isValid = false;
                }

                if (hinhThucGiam.value ==="Theo Giá Tiền" && mucGiam <= 0 ) {
                    errorMessage = 'Mức giảm theo giá tiền phải lớn hơn 0 ';
                    isValid = false;
                }
            }
        });

        if (!isValid) {
            showToastError(errorMessage);
            return;
        }

        Swal.fire({
            title: 'Bạn có muốn thêm không?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Có',
            cancelButtonText: 'Không'
        }).then((result) => {
            if (result.isConfirmed) {
                Swal.fire({
                    icon: 'success',
                    title: 'Thêm thành công!',
                    text: 'Đợt khuyến mãi đã được thêm vào hệ thống.',
                    showConfirmButton: false,
                    timer: 1500
                }).then(() => {
                    form.classList.add('was-validated');
                    form.submit();
                });
            }
        });
    });

    function validate(event) {
        // Nếu cần thêm xử lý validate riêng
    }

    // Set min datetime for inputs
    const now = new Date();
    const nowString = now.toISOString().slice(0, 16);
    thoiGianBatDau.min = nowString;
    thoiGianKetThuc.min = nowString;

    // Initialize
    updateGiaTriGiamUnit();

    // Gọi API khi tải trang
    await fetchSanPhamKhuyenMai();
});