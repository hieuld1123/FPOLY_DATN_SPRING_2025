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

    // Lấy ID khuyến mãi từ URL (nếu có)
    // Sửa phần khởi tạo storageKey
    const khuyenMaiId = window.location.pathname.split('/').pop();
    const storageKey = khuyenMaiId && khuyenMaiId !== 'new'
        ? `promotion_${khuyenMaiId}_selected`
        : 'promotion_new_selected';

    // Danh sách sản phẩm đã chọn
    let selectedProducts = new Map();
    let sanPhamDangKhuyenMai = [];
    let formSubmitted = false;


    // 1. Hàm quản lý trạng thái
    function saveCurrentState() {
        // Lưu cả trang hiện tại
        const state = {
            currentPage: currentPage,
            selectedProducts: Array.from(selectedProducts.entries())
        };
        sessionStorage.setItem(storageKey, JSON.stringify(state));
    }

    function loadProductPage(page) {
        fetch(`/admin/khuyen-mai/product-page?page=${page}`)
            .then(response => response.text())
            .then(html => {
                document.getElementById('product-table-body').innerHTML = html;
                bindCheckboxEvents();
                restoreState();
            });
    }


    function restoreState() {
        const savedData = sessionStorage.getItem(storageKey);
        if (savedData) {
            try {
                const state = JSON.parse(savedData);
                currentPage = state.currentPage || 0;
                selectedProducts = new Map(state.selectedProducts);

                // Đánh dấu checkbox và cập nhật giá trị
                document.querySelectorAll('.chonSanPham').forEach(checkbox => {
                    const productId = checkbox.getAttribute('data-product-id');
                    if (selectedProducts.has(productId)) {
                        checkbox.checked = true;
                        const mucGiamInput = checkbox.closest('tr').querySelector('.mucGiam');
                        if (mucGiamInput) {
                            mucGiamInput.disabled = false;
                            mucGiamInput.value = selectedProducts.get(productId);
                        }
                    }
                });

                updateGiaSauGiam();
                updateSelectAllState();
            } catch (e) {
                console.error('Lỗi khôi phục trạng thái:', e);
            }
        }
    }

    function updateUIAfterPageLoad() {
        updateGiaTriGiamUnit();
        updateGiaSauGiam();
        updateSelectAllState();
        bindCheckboxEvents();
        bindDiscountInputEvents();

        // Cập nhật URL mà không tải lại trang
        window.history.pushState({}, '', `?page=${currentPage}`);
    }

    // Xử lý sự kiện popstate khi người dùng dùng nút back/forward
    window.addEventListener('popstate', function() {
        const urlParams = new URLSearchParams(window.location.search);
        currentPage = parseInt(urlParams.get('page')) || 0;
        loadProductPage(currentPage);
    });

    // Kiểm tra nếu là trang mới và có dữ liệu cũ, hỏi người dùng có muốn tiếp tục không
    window.addEventListener('load', function() {
        if (khuyenMaiId === 'new' && sessionStorage.getItem('promotion_new_selected')) {
            Swal.fire({
                title: 'Tiếp tục phiên làm việc trước?',
                text: 'Chúng tôi tìm thấy dữ liệu khuyến mãi chưa hoàn thành. Bạn có muốn tiếp tục?',
                icon: 'question',
                showCancelButton: true,
                confirmButtonText: 'Tiếp tục',
                cancelButtonText: 'Bắt đầu mới'
            }).then((result) => {
                if (!result.isConfirmed) {
                    clearSavedState();
                }
            });
        }
    });

    function clearSavedState() {
        sessionStorage.removeItem(storageKey);
    }

    function bindCheckboxEvents() {
        document.querySelectorAll('.chonSanPham').forEach(checkbox => {
            // Xóa tất cả sự kiện change hiện có
            checkbox.onchange = null;

            // Thêm sự kiện mới
            checkbox.addEventListener('change', function() {
                const productId = this.getAttribute('data-product-id');
                const row = this.closest('tr');
                const mucGiamInput = row.querySelector('.mucGiam');

                // Kiểm tra sản phẩm đang khuyến mãi
                if (this.checked && sanPhamDangKhuyenMai.includes(productId.toString())) {
                    showToastError('Sản phẩm này đã có khuyến mãi!');
                    this.checked = false;
                    return;
                }

                if (this.checked) {
                    mucGiamInput.disabled = false;
                    mucGiamInput.value = giaTriGiam.value || 0;
                    selectedProducts.set(productId, mucGiamInput.value);
                } else {
                    mucGiamInput.disabled = true;
                    mucGiamInput.value = '';
                    selectedProducts.delete(productId);
                }

                saveCurrentState();
                updateGiaSauGiam();
                updateSelectAllState();
            });
        });
    }

    function bindDiscountInputEvents() {
        document.querySelectorAll('.mucGiam').forEach(input => {
            input.addEventListener('input', function() {
                const productId = this.getAttribute('data-product-id');
                if (selectedProducts.has(productId)) {
                    selectedProducts.set(productId, this.value);
                    saveCurrentState();
                    updateGiaSauGiam();
                }
            });
        });
    }

    // Sửa lại sự kiện phân trang
    function bindPaginationEvents() {
        document.querySelectorAll('.pagination a').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const page = this.getAttribute('data-page');
                loadProductPage(page);
            });
        });
    }

    // 3. Hàm hỗ trợ
    function updateSelectAllState() {
        const allCheckboxes = document.querySelectorAll('.chonSanPham');
        const checkedCount = Array.from(allCheckboxes).filter(cb => cb.checked).length;

        if (selectAll) {
            selectAll.checked = checkedCount === allCheckboxes.length;
            selectAll.indeterminate = checkedCount > 0 && checkedCount < allCheckboxes.length;
        }
    }


    function fetchSanPham(keyword) {
        fetch(`/admin/khuyen-mai/product-search?keyword=${keyword}&page=${currentPage}`)
            .then(response => response.text())
            .then(html => {
                document.getElementById('product-table-body').innerHTML = html;
                updateUIAfterPageLoad();
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
    if (selectAll) {
        selectAll.addEventListener('change', function() {
            const isChecked = this.checked;
            document.querySelectorAll('.chonSanPham').forEach(checkbox => {
                const productId = checkbox.getAttribute('data-product-id');
                if (!sanPhamDangKhuyenMai.includes(productId)) {
                    checkbox.checked = isChecked;
                    const row = checkbox.closest('tr');
                    if (row) {
                        const mucGiamInput = row.querySelector('.mucGiam');
                        if (mucGiamInput) {
                            mucGiamInput.disabled = !isChecked;
                            mucGiamInput.value = isChecked ? giaTriGiam.value : '';

                            if (isChecked) {
                                selectedProducts.set(productId, mucGiamInput.value);
                            } else {
                                selectedProducts.delete(productId);
                            }
                        }
                    }
                }
            });

            saveCurrentState();
            updateGiaSauGiam();
        });
    }

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
    form.addEventListener("submit", function (event) {
        event.preventDefault();

        let isValid = true;
        let errorMessage = '';

        selectedProducts.forEach((mucGiam, productId) => {
            const hiddenInput = document.createElement('input');
            hiddenInput.type = 'hidden';
            hiddenInput.name = 'selectedProducts';
            hiddenInput.value = JSON.stringify({id: productId, mucGiam: mucGiam});
            form.appendChild(hiddenInput);
        });

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


            // Xử lý lưu (đồng ý thêm khuyến mãi)
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
                        formSubmitted = true;
                        clearSavedState(); // Quan trọng: Xóa dữ liệu đã lưu
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
    // 6. Khởi tạo
    async function initialize() {
        // Lấy trang hiện tại từ URL
        const urlParams = new URLSearchParams(window.location.search);
        currentPage = parseInt(urlParams.get('page')) || 0;

        await fetchSanPhamKhuyenMai();
        loadProductPage(currentPage);

        // Các sự kiện khác
        hinhThucGiam.addEventListener('change', updateGiaTriGiamUnit);
        giaTriGiam.addEventListener('input', updateDiscountValues);
        selectAll.addEventListener('change', handleSelectAll);
        form.addEventListener('submit', handleFormSubmit);
    }

    // Chạy khởi tạo
    initialize();



});