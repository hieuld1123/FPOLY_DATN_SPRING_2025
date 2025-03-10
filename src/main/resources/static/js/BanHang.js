let formData = {
    invoiceId: null,
    message: null
}

const handlePayment = () => {
    alert(`Thanh toán hoa hóa đơn ${formData}`);
};

const createInvoice = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/v1/ban-hang/hoa-don', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({})
        });
        const result = await response.json();
        if (result.status === 200) {
            document.getElementById("search-product").disabled = true;
            document.getElementById("btnPayment").disabled = true;
            document.getElementById("btnCancel").disabled = true;
            await loadInvoices();
            const productTableBody = document.querySelector('.product-table tbody');
            productTableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center">Chưa có sản phẩm nào được thêm</td>
                </tr>
            `;
        }
    } catch (error) {
        console.error('Error creating invoice:', error);
    }
};

async function loadInvoices() {
    try {
        const response = await fetch('http://localhost:8080/api/v1/ban-hang/hoa-don', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const invoices = await response.json();

        const invoiceContainer = document.getElementById('invoices');

        if (!invoices.data || invoices.data.length === 0) {
            invoiceContainer.innerHTML = '<p>Không tìm thấy hóa đơn nào.</p>';
            return;
        }

        let htmlContent = '';
        invoices.data.forEach((invoice) => {
            htmlContent += `
                <label class="form-check">
                    <input class="form-check-input" type="radio" name="radios" value="${invoice.id}" />
                    <span class="form-check-label">${invoice.maHoaDon} - ${invoice.tranThai}</span>
                </label>
            `;
        });

        invoiceContainer.innerHTML = htmlContent;

        document.querySelectorAll('.form-check-input').forEach(input => {
            input.addEventListener('change', () => handleInvoiceChange(input.value));
        });
    } catch (error) {
        console.error('Error loading invoices:', error);
        alert('Không thể tải danh sách hóa đơn!');
    }
}

async function handleInvoiceChange(id) {
    formData.invoiceId = id;
    document.getElementById("search-product").disabled = false;
    document.getElementById("btnPayment").disabled = false;
    document.getElementById("btnCancel").disabled = false;
    const tongTienElement = document.getElementById('tongTien');
    const tongElement = document.getElementById('tong');
    const countProductElement = document.getElementById('count-product');
    const productTableBody = document.querySelector('.product-table tbody');

    try {
        const response = await fetch(`http://localhost:8080/api/v1/ban-hang/hoa-don/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const invoice = await response.json();
        countProductElement.innerHTML = invoice.data.listSanPham.length;
        tongTienElement.innerHTML = invoice.data.tongTien.toLocaleString("vi-VN") + " VNĐ";
        tongElement.innerHTML = invoice.data.tongTien.toLocaleString("vi-VN") + " VNĐ";

        productTableBody.innerHTML = '';

        if (!invoice.data.listSanPham || invoice.data.listSanPham.length === 0) {
            productTableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center">Chưa có sản phẩm nào được thêm</td>
                </tr>
            `;
            return;
        }

        invoice.data.listSanPham.forEach((sanPham, index) => {
            const row = `
                <tr>
                    <td>${index + 1}</td>
                    <td><img src="${sanPham.hinhAnh}" alt="Product Image" width="50"></td>
                    <td>${sanPham.tenSanPham}<br><small>${sanPham.maSanPham}</small></td>
                    <td>
                        <input 
                            type="number" 
                            min="1"
                            max="${sanPham.soLuongTonKho}" 
                            value="${sanPham.soLuong}" 
                            class="quantity-input"
                            data-product-id="${sanPham.id}"
                        />
                    </td>
                    <td class="text-right">${sanPham.gia.toLocaleString("vi-VN")}</td>
                    <td class="text-right">${(sanPham.soLuong * sanPham.gia).toLocaleString("vi-VN")}</td>
                    <td class="text-center"><span class="remove-btn" onclick="removeProduct(${sanPham.id})">X</span></td>
                </tr>
            `;
            productTableBody.insertAdjacentHTML('beforeend', row);
        });

        function debounce(func, delay) { // Debounce function (2000ms)
            let timer;
            return function (...args) {
                clearTimeout(timer);
                timer = setTimeout(() => func.apply(this, args), delay);
            };
        }

        const debouncedHandleQuantityChange = debounce((invoiceDetailId, quantity) => {
            console.log(`Product ID: ${invoiceDetailId}, Quantity: ${quantity}`);
            handleQuantityChange(invoiceDetailId, quantity);
        }, 2000);

        async function handleQuantityChange(invoiceDetailId, quantity) {
            console.log(invoiceDetailId, quantity, formData.invoiceId)
            try {
                await fetch(`http://localhost:8080/api/v1/ban-hang/update-quantity/${invoiceDetailId}/${quantity}`, {
                    method: "PUT",
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(e => e.json()).then(data => {
                    if (data.status === 202) {
                        handleInvoiceChange(formData.invoiceId);
                    }
                })
            } catch (e) {
                console.error(e);
            }
        }

        document.querySelectorAll('.quantity-input').forEach(input => {
            input.addEventListener('input', (event) => {
                const maxValue = Number(event.target.getAttribute('max'));
                let newQuantity = Number(event.target.value);

                if (newQuantity > maxValue) {
                    event.target.value = maxValue;
                    newQuantity = maxValue;
                }

                const invoiceDetailId = event.target.dataset.productId;
                debouncedHandleQuantityChange(invoiceDetailId, newQuantity);
            });
        });

    } catch (e) {
        console.error('Error loading invoice:', e);
    }
}

async function removeProduct(itemId) {
    await fetch(`http://localhost:8080/api/v1/ban-hang/delete-item/${itemId}`, {
        method: "DELETE",
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => response.json()).then(res => {
        if (res.status === 202) {
            handleInvoiceChange(formData.invoiceId);
        }
    })
}

$(document).ready(function () {
    let debounceTimer;

    function debounce(func, delay) {
        return function (...args) {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => func.apply(this, args), delay);
        };
    }

    async function searchProducts(query) { // The function calls API to search for products
        try {
            const response = await fetch(`http://localhost:8080/api/v1/ban-hang/san-pham?keyword=${query}`);
            const result = await response.json();

            if (result.status === 200 && result.data.length > 0) {
                displaySuggestions(result.data);
            } else {
                $('#suggestions').empty().hide();
            }
        } catch (error) {
            console.error(error);
            $('#suggestions').empty().hide();
        }
    }

    function displaySuggestions(products) { // Show suggestions from API
        const suggestionsContainer = $('#suggestions');
        suggestionsContainer.empty();

        products.forEach(product => {
            const suggestionItem = $('<div>').addClass('suggestion-item').addClass('suggestion-item')
                .attr('data-id', product.id);

            const productImage = $('<img>').attr('src', product.hinhAnh || 'https://via.placeholder.com/50').attr('alt', product.tenSanPham);
            suggestionItem.append(productImage);

            const productInfo = $('<div>').addClass('product-info');
            productInfo.append($('<div>').text(product.tenSanPham));
            productInfo.append($('<small>').text(product.maSanPham));
            suggestionItem.append(productInfo);

            const productQuantity = $('<div>').addClass('quantity').text("Số lượng: " + product.soLuong);
            suggestionItem.append(productQuantity);

            const productPrice = $('<div>').addClass('price').text("Giá bán: " + formatPrice(product.gia) + " VNĐ");
            suggestionItem.append(productPrice);

            suggestionsContainer.append(suggestionItem);
        });

        suggestionsContainer.show();
    }

    $('input[name="search-product"]').on('input', debounce(function () { // Processing the event in the search box (Debounce 500ms)
        const query = $(this).val().trim().toLowerCase();
        if (query) {
            searchProducts(query);
        } else {
            $('#suggestions').empty().hide();
        }
    }, 500));

    $(document).on('click', '.suggestion-item', function () {  // Processing when choosing products from the suggested list
        addToCart($(this).data('id'));
        $('#suggestions').empty().hide();
    });

    async function addToCart(productId) {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/ban-hang/add-to-cart/${productId}/${formData.invoiceId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({})
            });

            const result = await response.json();
            if (result.status === 202) {
                handleInvoiceChange(formData.invoiceId);
            }
        } catch (error) {
            console.error(error);
            $('#suggestions').empty().hide();
        }
    }

    $(document).on('click', function (e) { // Hide suggestions when clicking out
        if (!$(e.target).closest('input[name="search-product"], #suggestions').length) {
            $('#suggestions').empty().hide();
        }
    });

    function formatPrice(price) {
        return price.toLocaleString("vi-VN");
    }
});

document.addEventListener('DOMContentLoaded', loadInvoices);
document.getElementById('createInvoiceBtn').addEventListener('click', createInvoice);