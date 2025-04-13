let formData = null;

const debounce = (func, delay) => {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => func.apply(this, args), delay);
    };
};

const formatPrice = (price) => price.toLocaleString("vi-VN");

$(document).ready(function () {
    const invoiceCode = window.location.pathname.split('/').pop();

    async function init() {
        await fetchInvoice();

    }

    $(document).on("click", "#btn-back", function () {
        location.href = `/quan-ly/hoa-don/${invoiceCode}`;
    });


    const fetchInvoice = async () => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/v1/hoa-don/edit/${invoiceCode}`,
                {
                    method: "GET",
                    headers: {"Content-Type": "application/json"},
                }
            );
            const {data: invoice} = await response.json();
            formData = invoice;

            const ship = invoice.shippingFee < 1 ? formData.shippingFee : invoice.shippingFee;

            const total = parseFloat(invoice.tongTien) + parseFloat(ship);

            formData.customer = invoice.khachHang;
            $("#input-note").val(invoice.ghiChu);
            $("#count-product").html(invoice.listSanPham.length);
            $("#tongTien").html(`${invoice.tongTien.toLocaleString("vi-VN")} đ`);
            $("#tong").html(`${total.toLocaleString("vi-VN")} đ`);
            $("#shipping-fee-info").text(ship.toLocaleString("vi-VN") + " đ");
            $("#seller-info").text(invoice.seller);
            $("#created-date").text(new Date(invoice.ngayTao).toLocaleString());
            $("#updated-date").text(new Date(invoice.ngayCapNhat).toLocaleString());
            formData.totalMoney = invoice.tongTien;
            const $productTableBody = $(".product-table tbody");
            formData.totalItem = invoice.listSanPham.length;

            if (invoice.khachHang && invoice.khachHang.id) {
                const customer = invoice.khachHang;
                const customerId = String(customer.id);
                formData.customerId = customerId;
                const $customerInfo = $(".customer-info");
                const hasCustomerInfo = true;

                // Hiển thị thông tin khách hàng và địa chỉ
                const addressDisplay =
                    customer.diaChi && customer.diaChi !== "null, null, null, null"
                        ? `<div class="customer-address">${customer.diaChi}</div>`
                        : formData.province &&
                        formData.district &&
                        formData.ward &&
                        formData.addressDetail
                            ? `<div class="customer-address">${formData.addressDetail}, ${formData.ward}, ${formData.district}, ${formData.province}</div>`
                            : '<div class="customer-address">Chưa có địa chỉ</div>';

                $customerInfo
                    .html(
                        `
                    <div class="customer-name">
                        ${customer.tenKhachHang} - ${customer.soDienThoai} 
                    </div>
                    ${addressDisplay}
                    <div class="add-address-btn" data-customer-id="${customerId}">Thay đổi địa chỉ</div>
                    <div class="change-phone-btn" data-customer-id="${customerId}" style="display: inline-block; color: #dc3545 cursor: pointer;">Thay đổi số điện thoại</div>
                `
                    )
                    .removeClass("text-center text-start")
                    .addClass(hasCustomerInfo ? "text-start" : "text-center");
            }

            if (!invoice.listSanPham.length) {
                $productTableBody.html(`
                <tr>
                    <td colspan="7" class="text-center">Chưa có sản phẩm nào được thêm</td>
                </tr>
            `);
                return;
            }

            $productTableBody.html(
                invoice.listSanPham
                    .map(
                        (sanPham, index) => `
                        <tr>
                            <td>${index + 1}</td>
                            <td><img src="${
                            sanPham.hinhAnh
                        }" alt="Product Image" width="50"></td>
                            <td>${sanPham.tenSanPham}<br><small>${
                            sanPham.maSanPham
                        }</small></td>
                            <td>
                                <input 
                                    type="number" 
                                    min="1"
                                    max="${
                            sanPham.soLuongTonKho + sanPham.soLuong
                        }" 
                                    value="${sanPham.soLuong}" 
                                    class="quantity-input form-control"
                                    data-product-id="${sanPham.id}"
                                />
                            </td>
                            <td class="text-right">${formatPrice(
                            sanPham.gia
                        )}</td>
                            <td class="text-right">${formatPrice(
                            sanPham.soLuong * sanPham.gia
                        )}</td>
                            <td class="text-center">
                                <img src="/icon/delete_icon.png" class="remove-btn" data-id="${
                            sanPham.id
                        }"/>
                            </td>
                        </tr>
                    `
                    )
                    .join("")
            );

            $(".quantity-input").on("input", function () {
                const $input = $(this);
                const maxValue = Number($input.attr("max"));
                let newQuantity = Number($input.val());
                if (newQuantity < 1) newQuantity = 1;
                if (newQuantity > maxValue) newQuantity = maxValue;
                $input.val(newQuantity);
                debounce(handleQuantityChange, 2000)(
                    $input.data("product-id"),
                    newQuantity
                );
            });

            $(".remove-btn").on("click", function () {
                if(formData.listSanPham.length <= 1) {
                    alert("Không thể xóa sản phẩm cuối cùng trong hóa đơn!");
                    return;
                }
                removeProduct($(this).data("id"));
            });
        } catch (error) {
            console.error("Error loading invoice:", error);
        }
    }

    $(document).on("click", ".change-phone-btn", function () {
        const customerId = $(this).data("customer-id");
        formData.currentCustomerId = customerId;
        $("#changePhoneModal").modal("show");
    });

    $("#changePhoneModal").on("show.bs.modal", function () {
        $("#new-phone-number").val("");
        $("#changePhoneModal .text-danger").text("");
    });

    $("#savePhoneBtn").on("click", async function () {
        $("#changePhoneModal .text-danger").text("");

        const newPhoneNumber = $("#new-phone-number").val().trim();
        let isValid = true;

        if (!newPhoneNumber) {
            $("#new-phone-number")
                .siblings(".text-danger")
                .text("Số điện thoại không được để trống!");
            isValid = false;
        } else {
            const phonePattern = /^[0-9]{10,11}$/;
            if (!phonePattern.test(newPhoneNumber)) {
                $("#new-phone-number")
                    .siblings(".text-danger")
                    .text("Số điện thoại không hợp lệ! Vui lòng nhập 10-11 chữ số.");
                isValid = false;
            }
        }

        if (!isValid) return;

        // Gọi API để cập nhật số điện thoại
        try {
            const response = await fetch(
                `http://localhost:8080/api/v1/ban-hang/update-phone/${formData.customer.id}/${newPhoneNumber}/${formData.id}`,
                {
                    method: "PUT",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({}),
                }
            );

            const result = await response.json();
            if (result.status === 202) {
                $("#changePhoneModal").modal("hide");
                alert("Cập nhật số điện thoại thành công!");
                await fetchInvoice();
            } else {
                alert("Cập nhật số điện thoại thất bại: " + result.message);
            }
        } catch (error) {
            console.error("Error updating phone number:", error);
            alert("Đã có lỗi xảy ra khi cập nhật số điện thoại!");
        }
    });

    // Update address
    $(document).on("click", ".add-address-btn", function () {
        $("#addAddressModal").modal("show");
    });

    const loadProvinces = async (
        provinceSelect,
        districtSelect,
        wardSelect,
        formDataKey
    ) => {
        try {
            const response = await fetch("https://provinces.open-api.vn/api/?depth=3", {
                method: "GET",
                headers: {
                    Accept: "application/json",
                },
            });

            if (!response.ok) {
                throw new Error("Không thể tải danh sách tỉnh/thành phố");
            }

            const data = await response.json();
            provinceSelect.html(
                '<option value="">Chọn tỉnh/thành</option>' +
                data.map((p) => `<option value="${p.code}">${p.name}</option>`).join("")
            );
            localStorage.setItem("provinceData", JSON.stringify(data));

            provinceSelect.on("change", function () {
                const code = this.value;
                districtSelect
                    .html('<option value="">Chọn quận/huyện</option>')
                    .prop("disabled", true);
                wardSelect
                    .html('<option value="">Chọn xã/phường</option>')
                    .prop("disabled", true);

                if (code) {
                    const provinces = JSON.parse(localStorage.getItem("provinceData"));
                    const province = provinces.find((p) => p.code == code);
                    formData[formDataKey + "province"] = province.name;

                    districtSelect
                        .html(
                            '<option value="">Chọn quận/huyện</option>' +
                            province.districts
                                .map((d) => `<option value="${d.code}">${d.name}</option>`)
                                .join("")
                        )
                        .prop("disabled", false);
                }
            });

            districtSelect.on("change", function () {
                const code = this.value;
                wardSelect
                    .html('<option value="">Chọn xã/phường</option>')
                    .prop("disabled", true);

                if (code) {
                    const provinces = JSON.parse(localStorage.getItem("provinceData"));
                    const district = provinces
                        .flatMap((p) => p.districts)
                        .find((d) => d.code == code);
                    formData[formDataKey + "district"] = district.name;

                    wardSelect
                        .html(
                            '<option value="">Chọn xã/phường</option>' +
                            district.wards
                                .map((w) => `<option value="${w.name}">${w.name}</option>`)
                                .join("")
                        )
                        .prop("disabled", false);
                }
            });

            wardSelect.on("change", function (e) {
                formData[formDataKey + "ward"] = e.target.value;
            });
        } catch (error) {
            console.error("Error loading provinces:", error);
            provinceSelect.html('<option value="">Không thể tải tỉnh/thành</option>');
            alert(
                "Không thể tải danh sách tỉnh/thành phố. Vui lòng kiểm tra kết nối mạng hoặc thử lại sau!"
            );
        }
    };

    $("#addAddressModal").on("show.bs.modal", async function () {
        $("#existing-addresses").val("");
        $("#modal-province").val("");
        $("#modal-district").val("").prop("disabled", true);
        $("#modal-ward").val("").prop("disabled", true);
        $("#modal-address-detail").val("");
        $("#addAddressModal .text-danger").text("");

        $("#existingAddress").prop("checked", true);
        $("#existing-address-section").show();
        $("#new-address-section").hide();

        const $modalProvince = $("#modal-province");
        const $modalDistrict = $("#modal-district");
        const $modalWard = $("#modal-ward");
        loadProvinces($modalProvince, $modalDistrict, $modalWard, "modal_");

        $("#modal-address-detail").on(
            "input",
            (e) => (formData.modal_addressDetail = e.target.value)
        );

        try {
            const response = await fetch(
                `http://localhost:8080/api/v1/ban-hang/customer-addresses/${formData.customer.id}`,
                {
                    method: "GET",
                    headers: { "Content-Type": "application/json" },
                }
            );
            const result = await response.json();
            if (result.status === 200 && result.data?.length) {
                const addresses = result.data;
                $("#existing-addresses").html(
                    '<option value="">Chọn địa chỉ</option>' +
                    addresses
                        .map((address, index) => {
                            const fullAddress = `${address.addressDetail}, ${address.ward}, ${address.district}, ${address.province}`;
                            return `<option value="${index}" data-province="${address.province}" data-district="${address.district}" data-ward="${address.ward}" data-address-detail="${address.addressDetail}">${fullAddress}</option>`;
                        })
                        .join("")
                );
            } else {
                $("#existing-addresses").html(
                    '<option value="">Không có địa chỉ nào</option>'
                );
            }
        } catch (error) {
            console.error("Error loading addresses:", error);
            $("#existing-addresses").html(
                '<option value="">Không có địa chỉ nào</option>'
            );
        }
    });

    $(".address-option").on("change", function () {
        const option = $(this).val();
        if (option === "existing") {
            $("#existing-address-section").show();
            $("#new-address-section").hide();
        } else {
            $("#existing-address-section").hide();
            $("#new-address-section").show();
        }
    });

    $("#saveAddressBtn").on("click", async function () {
        $("#addAddressModal .text-danger").text("");
        const option = $(".address-option:checked").val();
        let addressData = {};

        if (option === "existing") {
            const selectedAddress = $("#existing-addresses option:selected");
            const addressIndex = selectedAddress.val();
            if (!addressIndex) {
                alert("Vui lòng chọn một địa chỉ!");
                return;
            }

            addressData = {
                province: selectedAddress.data("province"),
                district: selectedAddress.data("district"),
                ward: selectedAddress.data("ward"),
                addressDetail: selectedAddress.data("address-detail"),
            };
        } else {
            const province = $("#modal-province option:selected").text();
            const district = $("#modal-district option:selected").text();
            const ward = $("#modal-ward option:selected").text();
            const addressDetail = $("#modal-address-detail").val().trim();

            let isValid = true;

            if (!province || province === "Chọn tỉnh/thành") {
                $("#modal-province")
                    .siblings(".text-danger")
                    .text("Vui lòng chọn tỉnh/thành phố!");
                isValid = false;
            }

            if (!district || district === "Chọn quận/huyện") {
                $("#modal-district")
                    .siblings(".text-danger")
                    .text("Vui lòng chọn quận/huyện!");
                isValid = false;
            }

            if (!ward || ward === "Chọn xã/phường") {
                $("#modal-ward")
                    .siblings(".text-danger")
                    .text("Vui lòng chọn xã/phường!");
                isValid = false;
            }

            if (!addressDetail) {
                $("#modal-address-detail")
                    .siblings(".text-danger")
                    .text("Địa chỉ cụ thể không được để trống!");
                isValid = false;
            }

            if (!isValid) return;

            addressData = {
                province: province,
                district: district,
                ward: ward,
                addressDetail: addressDetail,
            };
        }

        try {
            const response = await fetch(
                `http://localhost:8080/api/v1/ban-hang/update-address/${formData.customer.id}/${formData.id}`,
                {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(addressData),
                }
            );

            const result = await response.json();
            if (result.status === 202) {
                formData.province = addressData.province;
                formData.district = addressData.district;
                formData.ward = addressData.ward;
                formData.addressDetail = addressData.addressDetail;

                $("#existing-addresses").val("");
                $("#modal-province").val("");
                $("#modal-district").val("").prop("disabled", true);
                $("#modal-ward").val("").prop("disabled", true);
                $("#modal-address-detail").val("");
                $("#addAddressModal .text-danger").text("");

                $("#addAddressModal").modal("hide");
                alert("Cập nhật địa chỉ thành công!");

                await fetchInvoice();
            } else {
                alert("Cập nhật địa chỉ thất bại: " + result.message);
            }
        } catch (error) {
            console.error("Error updating address:", error);
            alert("Đã có lỗi xảy ra khi cập nhật địa chỉ!");
        }
    });

    // Update product
    const loadProductSuggestions = async (query = "") => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/v1/ban-hang/san-pham?keyword=${encodeURIComponent(
                    query
                )}`
            );
            const { data } = await response.json();
            if (data?.length) {
                $("#suggestions")
                    .html(
                        data
                            .map(
                                (product) => `
                                    <div class="suggestion-item" data-id="${
                                    product.id
                                }">
                                        <img src="${
                                    product.hinhAnh ||
                                    "https://via.placeholder.com/50"
                                }" alt="${product.tenSanPham}">
                                        <div class="product-info">
                                            <div>${product.tenSanPham}</div>
                                            <small>${product.maSanPham}</small>
                                        </div>
                                        <div class="quantity">Số lượng: ${
                                    product.soLuong
                                }</div>
                                        <div class="price">Giá bán: ${formatPrice(
                                    product.gia
                                )} VNĐ</div>
                                    </div>
                                `
                            )
                            .join("")
                    )
                    .show();
            } else {
                $("#suggestions")
                    .html(
                        `
                        <div class="suggestion-item text-center text-secondary">
                            Không tìm thấy sản phẩm
                        </div>
                    `
                    )
                    .show();
            }
        } catch (error) {
            console.error("Search error:", error);
            $("#suggestions").empty().hide();
        }
    };

    $("#search-product")
        .on("focus", function () {
            const query = $(this).val().trim().toLowerCase();
            loadProductSuggestions(query);
        })
        .on(
            "input",
            debounce(async () => {
                const query = $("#search-product").val().trim().toLowerCase();
                loadProductSuggestions(query);
            }, 500)
        );

    $(document).on("click", ".suggestion-item", async function () {
        const productId = $(this).data("id");
        try {
            const response = await fetch(
                `http://localhost:8080/api/v1/ban-hang/add-to-cart/${productId}/${formData.id}`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({}),
                }
            );
            const result = await response.json();
            if (result.status === 202) await fetchInvoice();
        } catch (error) {
            console.error("Add to cart error:", error);
        }
        $("#suggestions").empty().hide();
    });

    $(document).on("click", (e) => {
        if (!$(e.target).closest("#search-product, #suggestions").length) {
            $("#suggestions").empty().hide();
        }
        if (
            !$(e.target).closest("#search-customer, #customer-suggestions").length
        ) {
            $("#customer-suggestions").empty().hide();
        }
    });

    const handleQuantityChange = async (invoiceDetailId, quantity) => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/v1/ban-hang/update-quantity/${invoiceDetailId}/${quantity}`,
                {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                }
            );
            const data = await response.json();
            if (data.status === 202) await fetchInvoice();
        } catch (error) {
            console.error("Quantity update error:", error);
        }
    };

    const removeProduct = async (itemId) => {
        if (!confirm("Xác nhận xóa?")) return;
        try {
            const response = await fetch(
                `http://localhost:8080/api/v1/ban-hang/delete-item/${itemId}`,
                {
                    method: "DELETE",
                    headers: { "Content-Type": "application/json" },
                }
            );
            const res = await response.json();
            if (res.status === 202) await fetchInvoice();
        } catch (error) {
            console.error("Error removing product:", error);
        }
    };

    $("#input-note").on(
        "input",
        debounce(async (e) => {
            await fetch(
                `http://localhost:8080/api/v1/ban-hang/update-note/${formData.id}`,
                {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json" },
                    body: e.target.value.trim() || " ",
                }
            );
        }, 2000)
    );
    init();
});