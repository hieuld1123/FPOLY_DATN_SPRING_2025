document.addEventListener('DOMContentLoaded', async function () {
    // Khai báo các biến chính
    const form = document.getElementById('khuyenMaiForm');
    const hinhThucGiam = document.getElementById('hinhThucGiam');
    const tenChienDich = document.getElementById("tenChienDichInput");
    const giaTriGiam = document.getElementById('giaTriGiam');
    const thoiGianBatDau = document.getElementById('thoiGianBatDau');
    const thoiGianKetThuc = document.getElementById('thoiGianKetThuc');
    const selectAll = document.getElementById('selectAll');
    const giaTriGiamUnit = document.getElementById('giaTriGiamUnit');

    let sanPhamDangKhuyenMai = [];
    let currentPage = 0;
    let selectedProductsState = {}; // Lưu trạng thái chọn sản phẩm
    let allProductsData = {}; // Lưu trữ thông tin tất cả sản phẩm
    let dataTable; // Biến lưu trữ DataTable instance


    document.querySelectorAll('.product-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', updateSelectedProducts);
    });

    function updateSelectedProducts() {
        const selectedIds = [];
        document.querySelectorAll('.product-checkbox:checked').forEach(checkbox => {
            selectedIds.push(checkbox.dataset.productId);
        });
        document.getElementById('selectedProducts').value = selectedIds.join(',');
    }

    // 1. Hàm hiển thị thông báo lỗi
    function showToastError(message) {
        const toastEl = document.getElementById('errorToast');
        const toastBody = toastEl.querySelector('.toast-body');
        toastBody.textContent = message;
        new bootstrap.Toast(toastEl, {delay: 3000}).show();
    }

    // 2. Hàm fetch danh sách sản phẩm đang khuyến mãi
    async function fetchSanPhamKhuyenMai() {
        try {
            const response = await fetch('/admin/khuyen-mai/san-pham-khuyen-mai');
            if (response.ok) {
                sanPhamDangKhuyenMai = await response.json();
            }
        } catch (error) {
            console.error('Lỗi khi tải danh sách sản phẩm khuyến mãi:', error);
        }
    }

    // 3. Hàm cập nhật đơn vị giảm giá
    function updateGiaTriGiamUnit() {
        const isPercent = hinhThucGiam.value === 'Phần Trăm';
        const unit = isPercent ? '%' : 'VNĐ';
        giaTriGiamUnit.textContent = unit;

        document.querySelectorAll('.mucGiamUnit').forEach(el => el.textContent = unit);

        if (isPercent) {
            giaTriGiam.max = '100';
            giaTriGiam.step = '0.1';
            document.querySelectorAll('.mucGiam').forEach(input => {
                input.max = '100';
                input.step = '0.1';
            });
        } else {
            giaTriGiam.removeAttribute('max');
            giaTriGiam.step = '1000';
            document.querySelectorAll('.mucGiam').forEach(input => {
                input.removeAttribute('max');
                input.step = '1000';
            });
        }
        updateGiaSauGiam();
    }

    // 4. Hàm tính giá sau giảm
    function tinhGiaSauGiam(giaGoc, mucGiam) {
        const isPercent = hinhThucGiam.value === 'Phần Trăm';
        return isPercent ? giaGoc * (1 - mucGiam / 100) : Math.max(0, giaGoc - mucGiam);
    }

    // 5. Hàm cập nhật giá sau giảm cho từng hàng
    function updateGiaSauGiamForRow(row) {
        const giaGoc = parseFloat(row.querySelector('.giaGoc').dataset.gia) || 0;
        const mucGiamInput = row.querySelector('.mucGiam');
        const giaSauGiamCell = row.querySelector('.giaSauGiam');
        const checkbox = row.querySelector('.chonSanPham');

        if (checkbox.checked && mucGiamInput && !mucGiamInput.disabled) {
            const mucGiam = parseFloat(mucGiamInput.value) || 0;
            const giaSauGiam = tinhGiaSauGiam(giaGoc, mucGiam);
            giaSauGiamCell.textContent = new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(giaSauGiam);
        } else {
            giaSauGiamCell.textContent = '';
        }
    }

    // 6. Hàm cập nhật giá sau giảm cho tất cả sản phẩm
    function updateGiaSauGiam() {
        document.querySelectorAll('.chonSanPham').forEach(checkbox => {
            const row = checkbox.closest('tr');
            if (row) {
                updateGiaSauGiamForRow(row);
            }
        });
    }

    // 7. Hàm khởi tạo DataTable
    function initDataTable() {
        dataTable = $('#listSanPham').DataTable({
            "language": {
                "lengthMenu": "Hiển thị _MENU_ dòng mỗi trang",
                "zeroRecords": "Không tìm thấy kết quả",
                "info": "Hiển thị trang _PAGE_ của _PAGES_",
                "infoEmpty": "Không có dữ liệu",
                "infoFiltered": "(lọc từ _MAX_ dòng)",
                "paginate": {
                    "first": "Đầu",
                    "last": "Cuối",
                    "next": "Sau",
                    "previous": "Trước"
                }
            },
            "createdRow": function(row, data, dataIndex) {
                // Lưu thông tin sản phẩm vào allProductsData
                const productId = $(row).find('.chonSanPham').attr('data-product-id');
                const giaGoc = parseFloat($(row).find('.giaGoc').data('gia')) || 0;

                allProductsData[productId] = {
                    giaGoc: giaGoc,
                    row: row
                };
            },
            "drawCallback": function() {
                initCheckboxEvents();
                restoreSelectionState();
                updateSelectAllCheckbox();
            }
        });
        return dataTable;
    }

    // 8. Hàm cập nhật trạng thái checkbox "Chọn tất cả"
    function updateSelectAllCheckbox() {
        if (!selectAll) return;

        const visibleCheckboxes = $('.chonSanPham:visible');
        const checkedVisible = visibleCheckboxes.filter(':checked').length;
        const allVisibleChecked = checkedVisible === visibleCheckboxes.length && visibleCheckboxes.length > 0;

        selectAll.checked = allVisibleChecked;
        selectAll.indeterminate = checkedVisible > 0 && !allVisibleChecked;
    }

    // 9. Hàm khởi tạo sự kiện checkbox
    function initCheckboxEvents() {
        // Sử dụng event delegation để xử lý các checkbox có thể chưa tồn tại trong DOM
        $('#listSanPham').on('change', '.chonSanPham', function() {
            const row = this.closest('tr');
            if (!row) return;

            const productId = this.getAttribute('data-product-id');
            const mucGiamInput = row.querySelector('.mucGiam');

            mucGiamInput.disabled = !this.checked;

            if (this.checked) {
                selectedProductsState[productId] = {
                    checked: true,
                    mucGiam: mucGiamInput.value || giaTriGiam.value
                };
                if (giaTriGiam.value) {
                    mucGiamInput.value = giaTriGiam.value;
                }
            } else {
                delete selectedProductsState[productId];
            }

            updateGiaSauGiamForRow(row);
            updateSelectAllCheckbox();
        });

        $('#listSanPham').on('input', '.mucGiam', function() {
            const row = this.closest('tr');
            const productId = row.querySelector('.chonSanPham').getAttribute('data-product-id');
            if (selectedProductsState[productId]) {
                selectedProductsState[productId].mucGiam = this.value;
                updateHiddenInputs();
            }
            updateGiaSauGiamForRow(row);
        });
    }

    // 10. Hàm khôi phục trạng thái chọn
    function restoreSelectionState() {
        // Lặp qua tất cả sản phẩm đã chọn trong selectedProductsState
        for (const productId in selectedProductsState) {
            if (selectedProductsState[productId].checked) {
                // Tìm hàng tương ứng trong DataTable
                const row = allProductsData[productId]?.row;
                if (row) {
                    const checkbox = row.querySelector('.chonSanPham');
                    const mucGiamInput = row.querySelector('.mucGiam');

                    if (checkbox && mucGiamInput) {
                        checkbox.checked = true;
                        mucGiamInput.disabled = false;
                        mucGiamInput.value = selectedProductsState[productId].mucGiam;
                        updateGiaSauGiamForRow(row);
                    }
                }
            }
        }
        updateSelectAllCheckbox();
    }

    // 11. Hàm lấy danh sách sản phẩm đã chọn (HOÀN TOÀN MỚI)
    function getSelectedProducts() {
        const selectedProducts = [];

        // Lặp qua tất cả sản phẩm trong selectedProductsState
        for (const [productId, productData] of Object.entries(selectedProductsState)) {
            if (productData.checked) {
                // Tìm thông tin sản phẩm từ allProductsData
                const productInfo = allProductsData[productId];

                if (productInfo) {
                    selectedProducts.push({
                        id: productId,
                        mucGiam: parseFloat(productData.mucGiam) || 0,
                        giaGoc: productInfo.giaGoc || 0
                    });
                }
            }
        }

        return selectedProducts;
    }

    // 12. Hàm validate form
    function validateForm() {
        let isValid = true;
        let errorMessage = '';

        if (!tenChienDich.value.trim()) {
            errorMessage = 'Vui lòng nhập tên chiến dịch khuyến mãi';
            isValid = false;
        }

        if (!hinhThucGiam.value) {
            errorMessage = 'Vui lòng chọn hình thức giảm giá';
            isValid = false;
        }

        const isPercent = hinhThucGiam.value === 'Phần Trăm';
        const giaGiam = parseFloat(giaTriGiam.value) || 0;

        if (!giaTriGiam.value.trim()) {
            errorMessage = 'Vui lòng nhập giá trị giảm chung';
            isValid = false;
        } else if (giaGiam <= 0) {
            errorMessage = 'Giá trị giảm phải lớn hơn 0';
            isValid = false;
        } else if (isPercent && giaGiam > 100) {
            errorMessage = 'Giá trị giảm theo phần trăm không được vượt quá 100%';
            isValid = false;
        }

        if (!thoiGianBatDau.value || !thoiGianKetThuc.value) {
            errorMessage = 'Vui lòng chọn ngày bắt đầu và ngày kết thúc';
            isValid = false;
        } else {
            const batDau = new Date(thoiGianBatDau.value);
            const ketThuc = new Date(thoiGianKetThuc.value);
            const now = new Date();

            batDau.setSeconds(0, 0);
            ketThuc.setSeconds(0, 0);
            now.setSeconds(0, 0);

            if (batDau < now) {
                errorMessage = 'Ngày bắt đầu không được là quá khứ';
                isValid = false;
            } else if (ketThuc <= batDau) {
                errorMessage = 'Thời gian kết thúc phải sau thời gian bắt đầu';
                isValid = false;
            }
        }

        const selectedProducts = getSelectedProducts();
        if (selectedProducts.length === 0) {
            errorMessage = 'Vui lòng chọn ít nhất một sản phẩm';
            isValid = false;
        } else {
            for (const product of selectedProducts) {
                if (product.mucGiam <= 0) {
                    errorMessage = `Mức giảm phải lớn hơn 0 cho sản phẩm ${product.id}`;
                    isValid = false;
                    break;
                }

                if (!isPercent && product.mucGiam >= product.giaGoc) {
                    errorMessage = `Mức giảm không được lớn hơn hoặc bằng giá gốc (${product.giaGoc} VNĐ) của sản phẩm ${product.id}`;
                    isValid = false;
                    break;
                }

                if (isPercent && product.mucGiam > 100) {
                    errorMessage = `Mức giảm theo phần trăm không được vượt quá 100% cho sản phẩm ${product.id}`;
                    isValid = false;
                    break;
                }
            }
        }

        if (!isValid) {
            showToastError(errorMessage);
        }

        return isValid;
    }

    // 13. Hàm xử lý submit form
    async function handleSubmit(event) {
        event.preventDefault();

        if (!validateForm()) {
            return;
        }

        const confirmed = await Swal.fire({
            title: 'Bạn có muốn thêm không?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Có',
            cancelButtonText: 'Không'
        });

        if (!confirmed.isConfirmed) {
            return;
        }

        const selectedProducts = getSelectedProducts();
        const productsInput = document.createElement('input');
        productsInput.type = 'hidden';
        productsInput.name = 'selectedProducts';
        productsInput.value = JSON.stringify(selectedProducts);
        form.appendChild(productsInput);

        form.submit();
    }

    // 14. Khởi tạo sự kiện chính
    function initEventListeners() {
        hinhThucGiam.addEventListener('change', updateGiaTriGiamUnit);

        giaTriGiam.addEventListener('input', () => {
            const value = parseFloat(giaTriGiam.value) || 0;
            document.querySelectorAll('.mucGiam').forEach(input => {
                if (!input.disabled) {
                    input.value = value;
                    const productId = input.closest('tr').querySelector('.chonSanPham').getAttribute('data-product-id');
                    if (selectedProductsState[productId]) {
                        selectedProductsState[productId].mucGiam = value;
                    }
                }
            });
            updateGiaSauGiam();
            updateHiddenInputs();
        });

        if (selectAll) {
            selectAll.addEventListener('change', function() {
                const isChecked = this.checked;
                document.querySelectorAll('.chonSanPham').forEach(checkbox => {
                    const productId = checkbox.getAttribute('data-product-id');
                    if (!sanPhamDangKhuyenMai.includes(productId.toString())) {
                        checkbox.checked = isChecked;
                        const row = checkbox.closest('tr');
                        if (row) {
                            const mucGiamInput = row.querySelector('.mucGiam');
                            if (mucGiamInput) {
                                mucGiamInput.disabled = !isChecked;
                                mucGiamInput.value = isChecked ? giaTriGiam.value : '';

                                if (isChecked) {
                                    selectedProductsState[productId] = {
                                        checked: true,
                                        mucGiam: giaTriGiam.value
                                    };
                                } else {
                                    delete selectedProductsState[productId];
                                }
                            }
                        }
                    }
                });
                updateGiaSauGiam();
                updateSelectAllCheckbox();
                updateHiddenInputs();
            });
        }

        thoiGianBatDau.addEventListener('change', function() {
            thoiGianKetThuc.min = this.value;
            if (thoiGianKetThuc.value && thoiGianKetThuc.value < this.value) {
                thoiGianKetThuc.value = this.value;
            }
        });

        form.addEventListener("submit", handleSubmit);
    }

    function updateHiddenInputs() {
        const form = document.getElementById('khuyenMaiForm');

        // Xóa tất cả hidden inputs cũ
        document.querySelectorAll('input[name="selectedProducts"]').forEach(el => el.remove());

        // Tạo hidden input mới cho mỗi sản phẩm đã chọn
        for (const [productId, productData] of Object.entries(selectedProductsState)) {
            if (productData.checked) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'selectedProducts';
                input.value = JSON.stringify({
                    id: productId,
                    mucGiam: productData.mucGiam
                });
                form.appendChild(input);
            }
        }
    }

    // 15. Khởi tạo chính
    async function initialize() {
        const urlParams = new URLSearchParams(window.location.search);
        currentPage = parseInt(urlParams.get('page')) || 0;

        await fetchSanPhamKhuyenMai();
        const table = initDataTable();
        initEventListeners();

        const now = new Date();
        const nowString = now.toISOString().slice(0, 16);
        thoiGianBatDau.min = nowString;
        thoiGianKetThuc.min = nowString;
    }

    // Khởi chạy
    initialize();
});