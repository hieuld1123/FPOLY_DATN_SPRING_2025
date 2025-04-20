$(document).ready(function () {
    function fetchOrderData(orderId) {
        $.ajax({
            url: `http://localhost:8080/api/v1/hoa-don/${orderId}`,
            method: 'GET',
            dataType: 'json',
            success: function (response) {
                if (response.status === 200) {
                    const data = response.data;
                    renderOrderDetails(data);
                } else {
                    alert('Không thể tải dữ liệu hóa đơn: ' + response.message);
                }
            },
            error: function (xhr, status, error) {
                console.error('Lỗi khi gọi API:', error);
                alert('Đã xảy ra lỗi khi tải dữ liệu hóa đơn.');
            }
        });
    }

    function renderOrderDetails(data) {
        const confirmInvoice = document.getElementById("confirm-invoice");
        confirmInvoice.disabled = !data.confirm;
        if (!data.confirm) {
            confirmInvoice.style.display = "none"
        }

        const confirmDelivery = document.getElementById("confirm-delivery");
        confirmDelivery.disabled = !data.delivery;
        if (!data.delivery) {
            confirmDelivery.style.display = "none"
        }

        const btnCancelInvoice = document.getElementById("btn-cancel-invoice");
        btnCancelInvoice.disabled = !data.allowCancel;

        // const btnModifyInvoice = document.getElementById("btn-modify-invoice");
        // btnModifyInvoice.disabled = !data.allowCancel;

        const btnCompleted = document.getElementById("btn-completed");
        btnCompleted.disabled = !data.completed;
        if (!data.completed) {
            btnCompleted.style.display = "none"
        }

        $('#order-id').text(data.order_id);

        // Xử lý thông tin khách hàng
        const customer = data.customer || {};
        const customerName = customer.name || "Khách lẻ";
        const customerPhone = customer.phone || "Chưa có số điện thoại";
        const customerAddress = customer.delivery_address && customer.delivery_address !== "null, null, null, null"
            ? customer.delivery_address
            : "Chưa có địa chỉ";

        $('#customer-header').html(`
        <span class="text-primary fs-3 fw-bold">${customerName}</span>
        <span class="ms-2 me-2">-</span>
        <span class="fs-3 fw-bold">${customerPhone}</span>
    `);
        $('#customer-information').html(`
        <h4 class="text-uppercase">Địa chỉ giao hàng</h4>
        <div class="mt-2">
            <span>${customerName}</span>
            <span class="ms-2 me-2">-</span>
            <span>${customerPhone}</span>
        </div>
        <div class="mt-1">
            <span>${customerAddress}</span>
        </div>
    `);

        let timelineHtml = '';
        data.status_timeline.forEach(item => {
            timelineHtml += `
                    <div class="timeline-item ${item.completed ? 'completed' : ''}">
                        <div class="timeline-icon">${item.completed ? '✓' : ''}</div>
                        <div class="timeline-label">${item.status}</div>
                        <div class="timeline-time">${item.time ? new Date(item.time).toLocaleString() : 'Chưa có'}</div>
                    </div>
                `;
        });
        $('#timeline').html(timelineHtml);

        $('#payment-info').html(`
                <div class="payment-item">
                    <span class="label">Khách phải trả:</span>
                    <span class="value">${data.payment.total_amount.toLocaleString()}</span>
                </div>
                <div class="payment-item">
                    <span class="label">Đã thanh toán:</span>
                    <span class="value">${data.payment.paid_amount.toLocaleString()}</span>
                </div>
                <div class="payment-item">
                    <span class="label">Còn phải trả:</span>
                    <span class="value">${data.payment.remaining_amount.toLocaleString()}</span>
                </div>
            `);

        if (data.payment.paid_amount >= data.payment.total_amount) {
            $('#paymentButton').hide();
        }

        $('#order-info').html(`
                <div class="row mb-2">
                    <div class="col-4 text-muted">Bán bởi</div>
                    <div class="col-8">${data.seller}</div>
                </div>
                <div class="row mb-2">
                    <div class="col-4 text-muted">Ngày bán</div>
                    <div class="col-8">${new Date(data.order_date).toLocaleString()}</div>
                </div>
            `);

        $('#order-note').html(`<span>${data.note}</span>`);

        let productHtml = '';
        data.products.forEach((product, index) => {
            productHtml += `
                    <tr>
                        <td>${index + 1}</td>
                        <td><img src="${product.image}" alt="Product Image" width="50"></td>
                        <td>${product.name}<br><small>${product.code}</small></td>
                        <td>${product.quantity}</td>
                        <td class="text-start">${product.unit_price.toLocaleString()}VND</td>
                        <td class="text-start">${product.total_price.toLocaleString()}VND</td>
                    </tr>
                `;
        });
        $('#product-list').html(productHtml);

        $('#summary').html(`
                <div class="row mb-2">
                    <div class="col-8">Tổng tiền (${data.products.length} sản phẩm)</div>
                    <div class="col-4 text-end">${data.summary.subtotal.toLocaleString()}</div>
                </div>
                <div class="row mb-2">
                    <div class="col-8">Phí giao hàng</div>
                    <div class="col-4 text-end">${data.summary.shipping_fee.toLocaleString()}</div>
                </div>
                <div class="row mb-2">
                    <div class="col-8">Mã giảm giá</div>
                    <div class="col-4 text-end">${data.summary.discount > 0 ? "-" : ""} ${data.summary.discount.toLocaleString()}</div>
                </div>
                <div class="row mb-2">
                    <div class="col-8 fw-semibold">Thành tiền</div>
                    <div class="col-4 text-end fw-semibold">${data.summary.total.toLocaleString()}</div>
                </div>
            `);

        $('#paymentModal').on('show.bs.modal', function () {
            $('#paymentAmount').val(data.payment.total_amount.toLocaleString());
            $('#paymentDate').val(new Date().toLocaleString());
            $('#paymentMethod').val('Chuyển khoản');
            $('#paymentReference').val('');
        });

        // Khi nhấn "Thanh toán đơn hàng" trong modal
        $('#confirmPayment').on('click', function () {
            $('#paymentModal').modal('hide');
        });

        // "In đơn hàng"
        $('#printButton').on('click', function () {

            const originalContent = document.body.innerHTML;

            let printContent = `
        <style>
            body { font-family: Arial, sans-serif; margin: 20px; }
            h1 { text-align: center; }
            .section { margin-bottom: 20px; }
            .section h3 { border-bottom: 1px solid #000; padding-bottom: 5px; }
            table { width: 100%; border-collapse: collapse; margin-top: 10px; }
            table, th, td { border: 1px solid #000; padding: 8px; text-align: left; }
            th { background-color: #f2f2f2; }
            .text-right { text-align: right; }
            .summary { margin-top: 20px; }
            .summary div { display: flex; justify-content: space-between; }
            .thank-you { text-align: center; margin-top: 20px; }
            @media print {
                body { margin: 0; }
                img { display: none; } /* Ẩn hình ảnh khi in */
            }
        </style>
        <h1>HÓA ĐƠN BÁN HÀNG</h1>
        <div class="section">
            <h3>Shop: NineShoes</h3>
            <p><strong>Hotline:</strong> 0123-456-789</p>
        </div>
        <div class="section">
            <h3>Mã hóa đơn: ${data.order_id}</h3>
            <p><strong>Ngày bán:</strong> ${new Date(data.order_date).toLocaleString()}</p>
            <p><strong>Người bán:</strong> ${data.seller}</p>
        </div>
        <div class="section">
            <h3>Thông tin khách hàng</h3>
            <p><strong>Tên:</strong> ${data.customer.name}</p>
            <p><strong>Số điện thoại:</strong> ${data.customer.phone || "Chưa có số điện thoại"}</p>
            <p><strong>Địa chỉ giao hàng:</strong>
                    ${data.customer.delivery_address && data.customer.delivery_address !== "null, null, null, null"
                ? data.customer.delivery_address
                : "Chưa có địa chỉ"}
            </p>
        </div>
        <div class="section">
            <h3>Thông tin sản phẩm</h3>
            <table>
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>Tên sản phẩm</th>
                        <th>Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Thành tiền</th>
                    </tr>
                </thead>
                <tbody>
    `;

            data.products.forEach((product, index) => {
                printContent += `
            <tr>
                <td>${index + 1}</td>
                <td>${product.name} (${product.code})</td>
                <td>${product.quantity}</td>
                <td class="text-right">${product.unit_price.toLocaleString()} VND</td>
                <td class="text-right">${product.total_price.toLocaleString()} VND</td>
            </tr>
        `;
            });

            printContent += `
                </tbody>
            </table>
        </div>
        <div class="section summary">
            <h3>Tổng kết</h3>
            <div><span>Tổng tiền (${data.products.length} sản phẩm):</span><span>${data.summary.subtotal.toLocaleString()} VND</span></div>
            <div><span>Phí giao hàng:</span><span>${data.summary.shipping_fee.toLocaleString()} VND</span></div>
            <div><span>Mã giảm giá:</span><span>${data.summary.discount > 0 ? "-" : ""} ${data.summary.discount.toLocaleString()} VND</span></div>
            <div><strong>Thành tiền:</strong><strong>${data.summary.total.toLocaleString()} VND</strong></div>
        </div>
        <div class="section thank-you">
            <p>Cảm ơn quý khách đã mua hàng tại NineShoes!</p>
        </div>
    `;

            document.body.innerHTML = printContent;

            window.print();

            window.onafterprint = function () {
                document.body.innerHTML = originalContent;
                location.reload();
            };
        });
    }

    $('#confirm-invoice').on('click', function () {
        const orderId = window.location.pathname.split('/').pop();
        if (confirm('Bạn có chắc chắn muốn xác nhận hóa đơn này?')) {
            $.ajax({
                url: `http://localhost:8080/api/v1/hoa-don/confirm/${orderId}`,
                method: 'PATCH',
                contentType: 'application/json',
                data: JSON.stringify({}),
                success: function (response) {
                    if (response.status === 200) {
                        alert('Xác nhận hóa đơn thành công!');
                        window.location.reload();
                    } else {
                        alert('Xác nhận hóa đơn thất bại: ' + response.message);
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Lỗi khi xác nhận hóa đơn:', error);
                    alert('Đã xảy ra lỗi khi xác nhận hóa đơn.');
                }
            });
        }
    });

    $('#confirm-delivery').on('click', function () {
        const orderId = window.location.pathname.split('/').pop();
        if (confirm('Bạn có chắc chắn muốn vận chuyển hóa đơn này?')) {
            $.ajax({
                url: `http://localhost:8080/api/v1/hoa-don/delivery/${orderId}`,
                method: 'PATCH',
                contentType: 'application/json',
                data: JSON.stringify({}),
                success: function (response) {
                    if (response.status === 200) {
                        alert('Vận chuyển hóa đơn thành công!');
                        window.location.reload();
                    } else {
                        alert('Vận chuyển hóa đơn thất bại: ' + response.message);
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Lỗi khi vận chuyển hóa đơn:', error);
                    alert('Đã xảy ra lỗi khi vận chuyển hóa đơn.');
                }
            });
        }
    });

    $('#confirmPayment').on('click', function () {
        const orderId = window.location.pathname.split('/').pop();
        if (confirm('Bạn có chắc chắn thanh toán hóa đơn này?')) {
            $.ajax({
                url: `http://localhost:8080/api/v1/hoa-don/payment/${orderId}`,
                method: 'PATCH',
                contentType: 'application/json',
                data: JSON.stringify({}),
                success: function (response) {
                    if (response.status === 200) {
                        alert('Thanh toán hóa đơn thành công!');
                        window.location.reload();
                    } else {
                        alert('Thanh toán hóa đơn thất bại: ' + response.message);
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Lỗi khi thanh toán hóa đơn:', error);
                    alert('Đã xảy ra lỗi khi thanh toán hóa đơn.');
                }
            });
        }
    });

    $('#btn-cancel-invoice').on('click', function () {
        const orderId = window.location.pathname.split('/').pop();
        if (confirm('Bạn có chắc chắn hủy hóa đơn này?')) {
            $.ajax({
                url: `http://localhost:8080/api/v1/hoa-don/cancel/${orderId}`,
                method: 'PATCH',
                contentType: 'application/json',
                data: JSON.stringify({}),
                success: function (response) {
                    if (response.status === 200) {
                        alert('Hủy hóa đơn thành công!');
                        window.location.reload();
                    } else {
                        alert('Hủy hóa đơn thất bại: ' + response.message);
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Lỗi khi hủy hóa đơn:', error);
                    alert('Đã xảy ra lỗi khi hủy hóa đơn.');
                }
            });
        }
    });

    $('#btn-modify-invoice').on('click', function () {
        const orderId = window.location.pathname.split('/').pop();
        if (confirm('Xác nhận chỉnh sửa?')) {
            location.href = `/quan-ly/hoa-don/edit/${orderId}`;
        }
    });

    $('#btn-completed').on('click', function () {
        const orderId = window.location.pathname.split('/').pop();
        if (confirm('Xác nhận hoàn thành?')) {
            $.ajax({
                url: `http://localhost:8080/api/v1/hoa-don/completed/${orderId}`,
                method: 'PATCH',
                contentType: 'application/json',
                data: JSON.stringify({}),
                success: function (response) {
                    if (response.status === 200) {
                        alert('Đơn hàng hoàn tất!');
                        window.location.reload();
                    } else {
                        alert('Thất bại: ' + response.message);
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Lỗi khi xác nhận hóa đơn:', error);
                    alert('Đã xảy ra lỗi khi xác nhận hóa đơn.');
                }
            });
        }
    });

    const urlPath = window.location.pathname;
    const orderId = urlPath.split('/').pop();
    fetchOrderData(orderId);
});