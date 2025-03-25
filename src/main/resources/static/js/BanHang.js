let formData = {
    invoiceId: null,
    totalInvoice: 0,
    totalItem: 0,
    note: null,
    type: null,
    recipient_name: null,
    phone_number: null,
    email: null,
    province: null,
    district: null,
    ward: null,
    addressDetail: null,
    paymentMethod: 'Thanh toán tại cửa hàng'
}

const clearData = async () => {
    formData = {
        invoiceId: null,
        totalInvoice: 0,
        totalItem: 0,
        note: null,
        type: null,
        recipient_name: null,
        phone_number: null,
        email: null,
        province: null,
        district: null,
        ward: null,
        addressDetail: null,
        paymentMethod: 'Thanh toán tại cửa hàng'
    }
    document.getElementById("search-product").disabled = true;
    document.getElementById("search-product").value = "";
    document.getElementById("btnPayment").disabled = true;
    document.getElementById("btnCancel").disabled = true;
    document.getElementById("input-note").disabled = true;
    document.getElementById("input-note").value = "";
    document.getElementById("count-product").innerHTML = "X";
    document.getElementById("tongTien").innerHTML = "X";
    document.getElementById("tong").innerHTML = "X";

    await loadInvoices();
    const productTableBody = document.querySelector('.product-table tbody');
    productTableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-secondary">
                        <img class="mt-3" src="/icon/empty_box_icon.png" alt="Empty Box Icon">
                        <div class="mt-3">Chưa có sản phẩm nào được thêm</div>
                    </td>
                </tr>
            `;
}

const enableElement = () => {
    document.getElementById("search-product").disabled = false;
    document.getElementById("input-note").disabled = false;
    document.getElementById("btnPayment").disabled = false;
    document.getElementById("btnCancel").disabled = false;
}

function checkRequiredFields(data) {
    const requiredFields = {
        recipient_name: "Tên người nhận",
        phone_number: "Số điện thoại",
        province: "Tỉnh/Thành Phố",
        district: "Quận/Huyện",
        ward: "Xã/Phường",
        addressDetail: "Địa chỉ cụ thể"
    };

    for (let key in requiredFields) {
        if (data[key] === null || data[key] === undefined || data[key] === "") {
            alert(`${requiredFields[key]} không được để trống!`);
            return false;
        }
    }
    return true;
}

const handlePayment = async () => {
    if (formData.totalItem < 1) {
        alert("Chưa có sản phẩm để thanh toán!")
        return;
    }
    if ((formData.type === "Có giao hàng") && (!checkRequiredFields(formData))) {
        return;
    }
    const data = {
        invoiceId: formData.invoiceId,
        customerId: null,
        employeeId: null,
        type: formData.type == null ? 'Offline' : formData.type,
        recipient_name: formData.recipient_name,
        phone_number: formData.phone_number,
        email: formData.email,
        province: formData.province,
        district: formData.district,
        ward: formData.ward,
        addressDetail: formData.addressDetail,
        paymentMethod: formData.paymentMethod
    }
    console.log(data)
    // return;
    const confirm = window.confirm("Xác nhận thanh toán");
    if (!confirm) {
        return;
    }

    await fetch('http://localhost:8080/api/v1/ban-hang/payment', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    }).then(response => response.json()).then(res => {
        if (res.status === 202) {
            window.location.reload();
        }
    })
};

const createInvoice = async () => {
    if (formData.totalInvoice > 5) {
        alert("Chỉ có thể tạo tối đa 6 hóa đơn!")
        return;
    }
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
            clearData();
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
        formData.totalInvoice = invoices.data.length;

        const invoiceContainer = document.getElementById('invoices');

        if (!invoices.data || invoices.data.length === 0) {
            document.getElementById("div-invoice").className = "text-center mb-3"
            invoiceContainer.innerHTML = `
                <div class="text-center text-secondary p-3">
                    <img style="width: 50px" class="mt-3" src="/icon/invoice_icon.png" alt="Invoice Icon">
                    <div class="mt-3">Không tìm thấy hóa đơn nào</div>
                </div>`;
            return;
        }

        let htmlContent = '';
        invoices.data.forEach((invoice) => {
            document.getElementById("div-invoice").className = "mb-3"
            htmlContent += `
                <label class="form-selectgroup-item">
                    <input type="radio" name="name" value="${invoice.id}" class="form-selectgroup-input radio-invoice" />
                    <span class="form-selectgroup-label">${invoice.maHoaDon} - ${invoice.tranThai}</span>
                </label>
            `
        });

        invoiceContainer.innerHTML = htmlContent;

        document.querySelectorAll('.radio-invoice').forEach(input => {
            input.addEventListener('change', () => handleInvoiceChange(input.value));
        });
    } catch (error) {
        console.error('Error loading invoices:', error);
    }
}

async function handleInvoiceChange(id) {
    formData.invoiceId = id;
    enableElement();
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
        document.getElementById('input-note').value = invoice.data.ghiChu;
        countProductElement.innerHTML = invoice.data.listSanPham.length;
        tongTienElement.innerHTML = invoice.data.tongTien.toLocaleString("vi-VN") + " VNĐ";
        tongElement.innerHTML = invoice.data.tongTien.toLocaleString("vi-VN") + " VNĐ";

        productTableBody.innerHTML = '';

        formData.totalItem = invoice.data.listSanPham.length;
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
                            max="${sanPham.soLuongTonKho + sanPham.soLuong}" 
                            value="${sanPham.soLuong}" 
                            class="quantity-input form-control"
                            data-product-id="${sanPham.id}"
                        />
                    </td>
                    <td class="text-right">${sanPham.gia.toLocaleString("vi-VN")}</td>
                    <td class="text-right">${(sanPham.soLuong * sanPham.gia).toLocaleString("vi-VN")}</td>
                    <td class="text-center"><img src="/icon/delete_icon.png" class="remove-btn" onclick="removeProduct(${sanPham.id})"/></td>
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
            handleQuantityChange(invoiceDetailId, quantity);
        }, 2000);

        async function handleQuantityChange(invoiceDetailId, quantity) {
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
                if (newQuantity < 1) {
                    event.target.value = 1;
                    newQuantity = 1;
                }
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
    const confirm = window.confirm("Xác nhận xóa?");
    if (!confirm) {
        return;
    }
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

function debounce(func, delay) {
    let timer;
    return function (...args) {
        clearTimeout(timer);
        timer = setTimeout(() => func.apply(this, args), delay);
    };
}

const handleInput = async (e) => {
    await fetch(`http://localhost:8080/api/v1/ban-hang/update-note/${formData.invoiceId}`, {
        method: "PATCH",
        headers: {
            'Content-Type': 'application/json'
        },
        body: e.target.value.length === 0 ? " " : e.target.value
    });

};
document.getElementById('input-note').addEventListener('input', debounce(handleInput, 2000));

document.querySelectorAll('.radio-order-type').forEach(input => {
    input.addEventListener('change', () => {
        formData.type = input.value;
        if (input.value !== 'Offline') {
            document.getElementById('div-address').style.display = 'block';
        } else {
            document.getElementById('div-address').style.display = 'none';
        }
    });
});
document.addEventListener('DOMContentLoaded', loadInvoices);
document.getElementById('createInvoiceBtn').addEventListener('click', createInvoice);
document.getElementById("btnCancel").addEventListener('click', async () => {
    const confirm = window.confirm("Xác nhận hủy hóa đơn?");
    if (!confirm) {
        return;
    }
    await fetch(`http://localhost:8080/api/v1/ban-hang/cancel-invoice/${formData.invoiceId}`, {
        method: "DELETE",
        headers: {
            'Content-Type': 'application/json'
        },
    }).then(response => {
        if (response.status === 200) {
            clearData();
            window.location.reload();
        }
    })
})


document.addEventListener("DOMContentLoaded", function () {
    const provinceSelect = document.getElementById("province");
    const districtSelect = document.getElementById("district");
    const wardSelect = document.getElementById("ward");

    // Gọi API lấy danh sách tỉnh/thành
    fetch("https://provinces.open-api.vn/api/?depth=3")
        .then(response => response.json())
        .then(data => {
            data.forEach(province => {
                let option = document.createElement("option");
                option.value = province.code; // Sử dụng code thay vì name
                option.textContent = province.name;
                provinceSelect.appendChild(option);
            });
            // Lưu dữ liệu vào localStorage để dùng lại
            localStorage.setItem("provinceData", JSON.stringify(data));
        });

    // Xử lý khi chọn tỉnh/thành
    provinceSelect.addEventListener("change", function () {
        let selectedProvinceCode = this.value;
        districtSelect.innerHTML = '<option value="">Chọn quận/huyện</option>';
        wardSelect.innerHTML = '<option value="">Chọn xã/phường</option>';
        districtSelect.disabled = true;
        wardSelect.disabled = true;

        if (selectedProvinceCode) {
            let provinceData = JSON.parse(localStorage.getItem("provinceData"));
            let selectedProvince = provinceData.find(p => p.code == selectedProvinceCode);
            formData.province = selectedProvince.name;
            if (selectedProvince) {
                selectedProvince.districts.forEach(district => {
                    let option = document.createElement("option");
                    option.value = district.code;
                    option.textContent = district.name;
                    districtSelect.appendChild(option);
                });

                districtSelect.disabled = false;
            }
        }
    });

    // Xử lý khi chọn quận/huyện
    districtSelect.addEventListener("change", function () {
        let selectedDistrictCode = this.value;
        wardSelect.innerHTML = '<option value="">Chọn xã/phường</option>';
        wardSelect.disabled = true;

        if (selectedDistrictCode) {
            let provinceData = JSON.parse(localStorage.getItem("provinceData"));
            let selectedProvince = provinceData.find(p =>
                p.districts.some(d => d.code == selectedDistrictCode)
            );

            if (selectedProvince) {
                let selectedDistrict = selectedProvince.districts.find(d => d.code == selectedDistrictCode);
                if (selectedDistrict) {
                    formData.district = selectedDistrict.name;
                    selectedDistrict.wards.forEach(ward => {
                        let option = document.createElement("option");
                        option.value = ward.name;
                        option.textContent = ward.name;
                        wardSelect.appendChild(option);
                    });
                    wardSelect.disabled = false;
                }
            }
        }
    });
    document.getElementById('ward').addEventListener('change', (e) => {
        formData.ward = e.target.value;
    })
    document.getElementById('address-detail').addEventListener('input', (e) => {
        formData.addressDetail = e.target.value;
    })

    document.getElementById("recipient_name").addEventListener('input', (e) => {
        formData.recipient_name = e.target.value;
    })

    document.getElementById("phone_number").addEventListener('input', (e) => {
        formData.phone_number = e.target.value;
    })

    document.getElementById("email").addEventListener('input', (e) => {
        formData.email = e.target.value;
    })

    document.getElementsByName('paymentMethod').forEach(input => {
        input.addEventListener('change', () => {
            formData.paymentMethod = input.value;
        });
    });
});
