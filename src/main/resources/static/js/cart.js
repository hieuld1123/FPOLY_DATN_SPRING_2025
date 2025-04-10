document.addEventListener("DOMContentLoaded", function () {
    // SweetAlert confirm xóa (form submit)
    // document.querySelectorAll(".delete-btn").forEach(button => {
    //     button.addEventListener("click", function () {
    //         const form = this.closest(".remove-form");
    //
    //         Swal.fire({
    //             title: "Bạn có chắc chắn?",
    //             text: "Sản phẩm sẽ bị xóa khỏi giỏ hàng!",
    //             icon: "warning",
    //             showCancelButton: true,
    //             confirmButtonColor: "#d33",
    //             cancelButtonColor: "#3085d6",
    //             confirmButtonText: "Xóa",
    //             cancelButtonText: "Hủy"
    //         }).then((result) => {
    //             if (result.isConfirmed) {
    //                 form.submit();
    //             }
    //         });
    //     });
    // });

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

                    // ✅ Cập nhật số lượng hiển thị
                    document.querySelector(`#quantity-${id}`).innerText = data.newQuantity;

                    // ✅ Cập nhật tổng tiền của từng sản phẩm
                    if (data.totalPriceFormatted) {
                        document.querySelector(`#total-price-${id}`).innerText = data.totalPriceFormatted;
                    }

                    // ✅ Cập nhật tổng tiền toàn bộ giỏ hàng
                    if (data.cartTotalFormatted) {
                        document.querySelector('.cart-total-price').innerText = data.cartTotalFormatted;
                        document.querySelector('.cart-total-price-end').innerText = data.cartTotalFormatted;
                    }

                    // ✅ Bật/tắt nút "-" tùy vào số lượng
                    const decreaseBtn = document.querySelector(`#decrease-btn-${id}`);
                    if (decreaseBtn) {
                        if (parseInt(data.newQuantity) === 1) {
                            decreaseBtn.classList.add("disabled");
                        } else {
                            decreaseBtn.classList.remove("disabled");
                        }
                    }
                } else {
                    alert(data.message || 'Đã có lỗi xảy ra');
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
                    document.querySelector('.cart-total-price').innerText = data.cartTotalFormatted;
                    document.querySelector('.cart-total-price-end').innerText = data.cartTotalFormatted;
                }
            });
    }

    // Event tăng số lượng
    document.querySelectorAll('.btn-increase').forEach(btn => {
        btn.addEventListener('click', () => updateQuantity(btn.dataset.id, 'increase'));
    });

    // Event giảm số lượng (có kiểm tra "disabled")
    document.querySelectorAll('.btn-decrease').forEach(btn => {
        btn.addEventListener('click', () => {
            if (!btn.classList.contains("disabled")) {
                updateQuantity(btn.dataset.id, 'decrease');
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
    });

    // Khi user tick/bỏ tick từng item, cập nhật lại trạng thái "select all"
    itemCheckboxes.forEach(cb => {
        cb.addEventListener('change', function () {
            const allChecked = [...itemCheckboxes].every(checkbox => checkbox.checked);
            selectAllCheckbox.checked = allChecked;
        });
    });
}
