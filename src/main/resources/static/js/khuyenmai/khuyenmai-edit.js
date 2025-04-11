document.addEventListener("DOMContentLoaded", async function () {
    const form = document.getElementById("khuyenMaiForm");
    const hinhThucGiam = document.getElementById("hinhThucGiam");
    const tenChienDich = document.getElementById("tenChienDich");
    const giaTriGiam = document.getElementById("giaTriGiam");
    const mucGiamInputs = document.querySelectorAll(".mucGiam");
    const giaGocElements = document.querySelectorAll(".giaGoc");
    const checkboxes = document.querySelectorAll(".chonSanPham");
    const selectAll = document.getElementById("selectAll");
    const searchForm = document.querySelector("form");
    const giaGocCells = document.querySelectorAll('.giaGoc');
    const mucGiamUnits = document.querySelectorAll('.mucGiamUnit');
    const giaSauGiamCells = document.querySelectorAll('.giaSauGiam');
    const khuyenMaiId = new URLSearchParams(window.location.search).get('id');
    const storageKey = "khuyenmai_selected_products_" + khuyenMaiId;
    let sanPhamDangKhuyenMai = []; // Mảng chứa ID sản phẩm đã có khuyến mãi (cần được khởi tạo hoặc lấy từ backend)
    let selectedProducts = [];
    const selected = JSON.parse(localStorage.getItem(storageKey)) || [];
    const savedProducts = JSON.parse(localStorage.getItem(storageKey) || '[]');
    const inputId = document.createElement('input');
    inputId.name = 'productId';


    // Xóa input ẩn cũ nếu có
    document.querySelectorAll('.hidden-selected-product').forEach(el => el.remove());

    // Thêm input ẩn mới vào form
    savedProducts.forEach(product => {
        const hiddenCheckbox = document.createElement('input');
        hiddenCheckbox.type = 'hidden';
        hiddenCheckbox.name = 'selectedProducts'; // backend sẽ nhận danh sách này
        hiddenCheckbox.value = JSON.stringify(product); // hoặc `${product.id}-${product.mucGiam}`
        hiddenCheckbox.classList.add('hidden-selected-product');
        form.appendChild(hiddenCheckbox);
    });


    // Thêm hàm xử lý phân trang mới
    function handlePaginationClick(e) {
        e.preventDefault();
        const href = this.getAttribute('href');

        // Lưu trạng thái trang hiện tại trước khi chuyển trang
        const currentState = JSON.parse(localStorage.getItem(storageKey) || '[]');

        checkboxes.forEach((checkbox, index) => {
            const productId = checkbox.getAttribute('data-product-id');
            if (checkbox.checked) {
                const existingIndex = currentState.findIndex(item => item.id === productId);
                const productData = {
                    id: productId,
                    mucGiam: mucGiamInputs[index].value
                };

                if (existingIndex >= 0) {
                    currentState[existingIndex] = productData;
                } else {
                    currentState.push(productData);
                }
            }
        });

        localStorage.setItem(storageKey, JSON.stringify(currentState));
        window.location.href = href;
    }

    // Thêm hàm lưu trạng thái trang hiện tại
    function saveCurrentPageState() {
        const savedState = JSON.parse(localStorage.getItem(storageKey) || '[]');

        checkboxes.forEach((checkbox, index) => {
            const productId = checkbox.getAttribute('data-product-id');
            const existingIndex = savedState.findIndex(item => item.id === productId);

            // Nếu checkbox được chọn, lưu hoặc cập nhật trạng thái
            if (checkbox.checked) {
                const productData = {
                    id: productId,
                    mucGiam: mucGiamInputs[index].value
                };

                if (existingIndex >= 0) {
                    savedState[existingIndex] = productData;
                } else {
                    savedState.push(productData);
                }
            }
        });

        localStorage.setItem(storageKey, JSON.stringify(savedState));
    }

    // Thêm hàm mới để xử lý click phân trang
    document.querySelectorAll('.pagination .page-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const href = this.getAttribute('href');

            // Lưu trạng thái hiện tại trước khi chuyển trang
            saveCurrentPageState();

            // Chuyển trang
            window.location.href = href;
        });
    });


    // Thêm hàm để khôi phục trạng thái ban đầu của khuyến mãi
    function restoreInitialState() {
        checkboxes.forEach((checkbox, index) => {
            const productId = checkbox.getAttribute('data-product-id');
            const mucGiamInput = mucGiamInputs[index];

            // Kiểm tra xem sản phẩm có trong map không
            if (mucGiamInput.dataset.originalValue) {
                checkbox.checked = true;
                mucGiamInput.disabled = false;
                mucGiamInput.value = mucGiamInput.dataset.originalValue;
                toggleProductSelection(productId, true);
            }
        });
        updateSelectAllState();
        updateGiaSauGiam();
    }

    // Cập nhật hàm restorePageState
    function restorePageState() {
        const savedState = JSON.parse(localStorage.getItem(storageKey) || '[]');

        checkboxes.forEach((checkbox, index) => {
            const productId = checkbox.getAttribute('data-product-id');
            const mucGiamInput = mucGiamInputs[index];
            const savedProduct = savedState.find(p => p.id === productId);

            if (savedProduct) {
                checkbox.checked = true;
                mucGiamInput.disabled = false;
                mucGiamInput.value = savedProduct.mucGiam;
            }
        });

        updateSelectAllState();
        updateGiaSauGiam();
    }


    // Cập nhật hàm toggleProductSelection
    function toggleProductSelection(productId, isSelected) {
        const savedState = JSON.parse(localStorage.getItem(storageKey) || '[]');
        const index = Array.from(checkboxes).findIndex(cb => cb.getAttribute('data-product-id') === productId);

        if (isSelected) {
            const existingIndex = savedState.findIndex(item => item.id === productId);
            if (existingIndex >= 0) {
                savedState[existingIndex].mucGiam = mucGiamInputs[index].value;
            } else {
                savedState.push({
                    id: productId,
                    mucGiam: mucGiamInputs[index].value
                });
            }
        } else {
            const existingIndex = savedState.findIndex(item => item.id === productId);
            if (existingIndex >= 0) {
                savedState.splice(existingIndex, 1);
            }
        }

        localStorage.setItem(storageKey, JSON.stringify(savedState));
    }


    function updateSelectAllState() {
        const total = Array.from(checkboxes).filter((checkbox) => {
            const productId = checkbox.getAttribute('data-product-id');
            return !sanPhamDangKhuyenMai.includes(productId);
        }).length;
        const checked = Array.from(checkboxes).filter((checkbox) => checkbox.checked).length;
        selectAll.checked = total > 0 && checked === total;
    }


    function showToastError(message) {
        let toastEl = document.getElementById('errorToast');
        let toastBody = toastEl.querySelector('.toast-body');
        toastBody.textContent = message;

        let toast = new bootstrap.Toast(toastEl, {delay: 3000}); // Hiển thị 3 giây
        toast.show();
    }

    // Xử lý từng checkbox
    checkboxes.forEach((checkbox, index) => {
        checkbox.addEventListener("change", function () {
            mucGiamInputs[index].disabled = !this.checked;
            updateSelectAllState();
        });
    });

    // // Cập nhật trạng thái "Chọn tất cả"
    // function updateSelectAllState() {
    //     const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;
    //     selectAll.checked = checkedCount === checkboxes.length;
    //     selectAll.indeterminate = checkedCount > 0 && checkedCount < checkboxes.length;
    // }

    // Cập nhật xử lý nút "Quay lại" và submit form
    const backBtn = document.querySelector('#btn-back');
    if (backBtn) {
        backBtn.addEventListener('click', function () {
            localStorage.removeItem(storageKey);
        });
    }

    form.addEventListener('submit', function () {
        localStorage.removeItem(storageKey);
    });


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
    // Cập nhật sự kiện change cho checkbox
    checkboxes.forEach((checkbox, index) => {
        checkbox.addEventListener('change', function () {
            const productId = this.getAttribute('data-product-id');

            if (this.checked && sanPhamDangKhuyenMai.map(id => id.toString()).includes(productId)) {
                showToastError('Sản phẩm này đã có khuyến mãi đang diễn ra!');
                this.checked = false;
                return;
            }

            mucGiamInputs[index].disabled = !this.checked;
            if (this.checked) {
                mucGiamInputs[index].value = giaTriGiam.value;
            } else {
                mucGiamInputs[index].value = '';
            }

            toggleProductSelection(productId, this.checked);
            updateSelectAllState();
            updateGiaSauGiam();
        });

        // Thêm sự kiện cho input mức giảm
        mucGiamInputs[index].addEventListener('change', function () {
            if (checkbox.checked) {
                const productId = checkbox.getAttribute('data-product-id');
                toggleProductSelection(productId, true);
            }
        });
    });


    // Cập nhật xử lý "Chọn tất cả"
    selectAll.addEventListener('change', function () {
        let hasSelected = false;
        const dsSanPhamKM = sanPhamDangKhuyenMai.map(id => id.toString());
        const savedState = JSON.parse(localStorage.getItem(storageKey) || '[]');

        checkboxes.forEach((checkbox, index) => {
            const productId = checkbox.getAttribute('data-product-id');
            const mucGiamInput = mucGiamInputs[index];

            if (!dsSanPhamKM.includes(productId)) {
                checkbox.checked = this.checked;
                if (this.checked) {
                    mucGiamInput.disabled = false;
                    mucGiamInput.value = giaTriGiam.value;
                    // Lưu trạng thái vào localStorage
                    toggleProductSelection(productId, true);
                } else {
                    mucGiamInput.disabled = true;
                    mucGiamInput.value = '';
                    // Xóa khỏi localStorage
                    toggleProductSelection(productId, false);
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

    document.querySelectorAll('.chonSanPham').forEach(cb => {
        cb.addEventListener("change", function () {
            const productId = this.getAttribute("data-product-id");
            const existing = form.querySelector(`input[name="sanPhamIds"][value="${productId}"]`);

            if (this.checked && !existing) {
                const hidden = document.createElement("input");
                hidden.type = "hidden";
                hidden.name = "sanPhamIds";
                hidden.value = productId;
                form.appendChild(hidden);
            }

            if (!this.checked && existing) {
                existing.remove();
            }
        });
    });

    // Khi load trang, đánh dấu lại những sản phẩm đã được hidden
    const hiddenInputs = form.querySelectorAll('input[name="sanPhamIds"]');
    hiddenInputs.forEach(input => {
        const checkbox = document.querySelector(`.chonSanPham[data-product-id="${input.value}"]`);
        if (checkbox) {
            checkbox.checked = true;
        }
    });


    // Xoá localStorage nếu bạn click "quay lại danh sách"

    // Kiểm tra form trước khi submit
    form.addEventListener("submit", function (event) {
        event.preventDefault();

        // Xoá input ẩn cũ
        document.querySelectorAll('.hidden-selected-product').forEach(el => el.remove());

        // Lấy sản phẩm đã lưu trong localStorage
        const savedProducts = JSON.parse(localStorage.getItem(storageKey) || '[]');

        savedProducts.forEach(product => {
            const inputCheckbox = document.createElement('input');
            inputCheckbox.type = 'hidden';
            inputCheckbox.name = 'sanPham_' + product.id;
            inputCheckbox.value = 'on';
            inputCheckbox.classList.add('hidden-selected-product');

            const inputMucGiam = document.createElement('input');
            inputMucGiam.type = 'hidden';
            inputMucGiam.name = 'mucGiam_' + product.id;
            inputMucGiam.value = product.mucGiam;
            inputMucGiam.classList.add('hidden-selected-product');

            form.appendChild(inputCheckbox);
            form.appendChild(inputMucGiam);
        });


        if (validateForm()) {
            Swal.fire({
                title: 'Bạn có muốn Sửa không?',
                icon: 'question',
                showCancelButton: true,
                confirmButtonText: 'Có',
                cancelButtonText: 'Không'
            }).then((result) => {
                if (result.isConfirmed) {
                    Swal.fire({
                        icon: 'success',
                        title: 'Sửa thành công!',
                        text: 'Đợt khuyến mãi đã được Sửa vào hệ thống.',
                        showConfirmButton: false,
                        timer: 1500
                    }).then(() => {

                        form.submit(); // <--- dùng biến form, không dùng this
                        localStorage.removeItem(storageKey); // Xóa localStorage sau khi submit
                    });
                }
            });
        }
    });

    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount).replace('₫', 'VNĐ');
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

    function tinhGiaSauGiam(giaGoc, mucGiam) {
        const isPercent = hinhThucGiam.value === 'Phần Trăm';
        if (isPercent) {
            return giaGoc * (1 - mucGiam / 100);
        }
        return Math.max(0, giaGoc - mucGiam);
    }

    // Hàm kiểm tra form
    function validateForm() {
        const startDate = new Date(document.getElementById("thoiGianBatDau").value);
        const endDate = new Date(document.getElementById("thoiGianKetThuc").value);

        const tenChienDich = document.querySelector("input[name='tenChienDich']").value.trim();
        if (tenChienDich === "") {
            showToastError("Vui lòng nhập Tên Chiến Dịch.");
            return false;
        }

        const giaTriGiam = document.getElementById("giaTriGiam").value;
        if (!giaTriGiam || giaTriGiam <= 0) {
            showToastError("Giá trị giảm chung phải lớn hơn 0.");
            return false;
        }

        if (endDate <= startDate) {
            showToastError("Thời gian kết thúc phải sau thời gian bắt đầu");
            return false;
        }

        let hasSelectedProduct = false;
        let isValid = true;

        checkboxes.forEach((checkbox, index) => {
            if (checkbox.checked) {
                hasSelectedProduct = true;
                const mucGiam = parseFloat(mucGiamInputs[index].value);
                const giaGoc = parseFloat(giaGocElements[index].dataset.gia);

                if (!mucGiam) {
                    showToastError("vui lòng nhập mức giảm");
                    isValid = false;
                    return;
                }

                if (mucGiam <= 0) {
                    showToastError("Mức giảm phải lớn hơn 0");
                    isValid = false;
                    return;
                }

                if (hinhThucGiam.value === "Phần Trăm" && mucGiam > 100) {
                    showToastError("Mức giảm không được vượt quá 100%");
                    isValid = false;
                    return;
                }

                if (hinhThucGiam.value === "Theo Giá Tiền" && mucGiam >= giaGoc) {
                    showToastError("Mức giảm không được vượt quá giá gốc");
                    isValid = false;
                    return;
                }
            }
        });

        if (!hasSelectedProduct) {
            showToastError("Vui lòng chọn ít nhất một sản phẩm");
            return false;
        }

        return isValid;
    }

    // Khởi tạo trạng thái ban đầu

    // checkboxes.forEach((checkbox, index) => {
    //     mucGiamInputs[index].disabled = !checkbox.checked;
    // });

    // Gọi API khi tải trang
    // Thay đổi phần khởi tạo cuối file
    await fetchSanPhamKhuyenMai();
    restorePageState();
    updateSelectAllState();
    updateGiaSauGiam();
});