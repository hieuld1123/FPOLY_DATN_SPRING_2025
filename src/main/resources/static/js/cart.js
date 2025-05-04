document.addEventListener("DOMContentLoaded", function () {
    updateSelectedTotal();

    // Hàm cập nhật trạng thái disabled của nút giảm số lượng
    function updateDecreaseButtonState(id, quantity) {
        const decreaseBtn = document.querySelector(`#decrease-btn-${id}`);
        if (decreaseBtn) {
            decreaseBtn.classList.toggle("disabled", parseInt(quantity) === 1);
        }
    }

    // Gọi API tăng/giảm số lượng
    function updateQuantity(id, action) {
        fetch('/api/cart/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: new URLSearchParams({
                chiTietId: id,
                action: action
            })
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    console.log(data);

                    const newQuantity = data.newQuantity;

                    // ✅ Cập nhật số lượng hiển thị
                    document.querySelector(`#quantity-${id}`).innerText = newQuantity;

                    // ✅ Cập nhật tổng tiền của từng sản phẩm (hiển thị)
                    if (data.totalPriceFormatted) {
                        document.querySelector(`#total-price-${id}`).innerText = data.totalPriceFormatted;
                    }

                    // ✅ Cập nhật thuộc tính data-price của checkbox
                    const checkbox = document.querySelector(`.item-checkbox[value="${id}"]`);
                    if (checkbox) {
                        const priceText = data.totalPriceFormatted.replace(' VND', '').replace(/\./g, '');
                        checkbox.dataset.price = parseFloat(priceText);
                    }

                    // ✅ Cập nhật trạng thái disabled của nút giảm
                    updateDecreaseButtonState(id, newQuantity);

                    // ✅ Cập nhật lại tổng tiền của các sản phẩm đã chọn
                    updateSelectedTotal();
                } else {
                    Swal.fire({ // Sử dụng SweetAlert2 để hiển thị thông báo đẹp hơn
                        icon: 'error',
                        title: 'Oops...',
                        text: data.message || 'Đã có lỗi xảy ra!',
                    });
                }
            });
    }

    // Gọi API xóa sản phẩm (AJAX)
    function removeItem(id) {
        fetch('/api/cart/remove', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: new URLSearchParams({ chiTietId: id })
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    document.querySelector(`#cart-item-${id}`).remove();
                    updateSelectedTotal(); // ✅ Cập nhật lại tổng tiền sau khi xóa
                }
            });
    }

    // Event tăng số lượng
    document.querySelectorAll('.btn-increase').forEach(btn => {
        btn.addEventListener('click', () => updateQuantity(btn.dataset.id, 'increase'));
    });

    // Event giảm số lượng (có kiểm tra "disabled")
    document.querySelectorAll('.btn-decrease').forEach(btn => {
        btn.addEventListener('click', function() { // Sử dụng function thường để 'this' trỏ đúng
            const itemId = this.dataset.id;
            const quantityElement = document.querySelector(`#quantity-${itemId}`);
            const currentQuantity = parseInt(quantityElement.innerText);

            if (currentQuantity > 1) {
                updateQuantity(itemId, 'decrease');
            }
        });
    });

    // Event xóa bằng nút
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            if (confirm('Bạn chắc chắn muốn xóa sản phẩm này?')) {
                removeItem(btn.dataset.id);
            }
        });
    });
});

// Checkbox "Chọn tất cả"
const selectAllCheckbox = document.querySelector('#select-all');
const itemCheckboxes = document.querySelectorAll('.item-checkbox');

if (selectAllCheckbox) {
    selectAllCheckbox.addEventListener('change', function () {
        itemCheckboxes.forEach(cb => cb.checked = this.checked);
        updateSelectedTotal(); // ✅ Cập nhật lại tổng tiền khi chọn/bỏ chọn tất cả
    });

    // Khi user tick/bỏ tick từng item, cập nhật lại trạng thái "select all"
    itemCheckboxes.forEach(cb => {
        cb.addEventListener('change', function () {
            const allChecked = [...itemCheckboxes].every(checkbox => checkbox.checked);
            selectAllCheckbox.checked = allChecked;
            updateSelectedTotal(); // ✅ Cập nhật lại tổng tiền khi chọn/bỏ chọn từng item
        });
    });
}

function formatCurrency(value) {
    return value.toLocaleString('vi-VN') + ' VND';
}

function updateSelectedTotal() {
    const selectedCheckboxes = document.querySelectorAll('.item-checkbox:checked');
    let total = 0;
    selectedCheckboxes.forEach(cb => {
        const priceValue = cb.dataset.price || '0';
        const price = parseFloat(priceValue);
        total += price;
    });

    document.querySelector('.cart-total-price').innerText = formatCurrency(total);
    document.querySelector('.cart-total-price-end').innerText = formatCurrency(total);
}