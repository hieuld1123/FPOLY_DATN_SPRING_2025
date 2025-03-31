// Constants
const DEFAULT_FORM_DATA = {
  invoiceId: null,
  totalInvoice: 0,
  totalItem: 0,
  note: null,
  type: null,
  customer: null,
  paymentMethod: "Thanh toán tại cửa hàng",
};

// Global state
let formData = { ...DEFAULT_FORM_DATA };

// Utility Functions
const debounce = (func, delay) => {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => func.apply(this, args), delay);
  };
};

const formatPrice = (price) => price.toLocaleString("vi-VN");

// DOM Manipulation Functions
const clearData = async () => {
  formData = { ...DEFAULT_FORM_DATA };

  $("#search-product").prop("disabled", true).val("");
  $("#btnPayment, #btnCancel, #input-note").prop("disabled", true);
  $("#input-note").val("");
  $("#count-product, #tongTien, #tong").html("X");

  await loadInvoices();

  $(".product-table tbody").html(`
        <tr>
            <td colspan="7" class="text-center text-secondary">
                <img class="mt-3" src="/icon/empty_box_icon.png" alt="Empty Box Icon">
                <div class="mt-3">Chưa có sản phẩm nào được thêm</div>
            </td>
        </tr>
    `);
};

const enableElement = () => {
  $("#search-product, #input-note, #btnPayment, #btnCancel").prop(
    "disabled",
    false
  );
};

// API Functions
const handlePayment = async () => {
  if (formData.totalItem < 1) {
    alert("Chưa có sản phẩm để thanh toán!");
    return;
  }

  if (formData.type === "Có giao hàng" && formData.customer === null) {
    alert("Bạn hãy thêm thông tin khách hàng để sử dụng dịch vụ giao hàng.");
    return;
  }

  const payload = {
    invoiceId: formData.invoiceId,
    type: formData.type || "Offline",
    ...Object.fromEntries(Object.entries(formData).slice(5)),
  };

  if (!confirm("Xác nhận thanh toán")) return;

  try {
    const response = await fetch(
      "http://localhost:8080/api/v1/ban-hang/payment",
      {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      }
    );

    const result = await response.json();
    if (result.status === 202) window.location.reload();
  } catch (error) {
    console.error("Payment error:", error);
  }
};

const createInvoice = async () => {
  if (formData.totalInvoice > 5) {
    alert("Chỉ có thể tạo tối đa 6 hóa đơn!");
    return;
  }

  try {
    const response = await fetch(
      "http://localhost:8080/api/v1/ban-hang/hoa-don",
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({}),
      }
    );

    const result = await response.json();
    if (result.status === 200) window.location.reload();
  } catch (error) {
    console.error("Error creating invoice:", error);
  }
};

const loadInvoices = async () => {
  try {
    const response = await fetch(
      "http://localhost:8080/api/v1/ban-hang/hoa-don",
      {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      }
    );

    const { data: invoices } = await response.json();
    formData.totalInvoice = invoices.length;

    const $invoiceContainer = $("#invoices");
    if (!invoices.length) {
      $("#div-invoice").addClass("text-center mb-3").removeClass("mb-3");
      $invoiceContainer.html(`
                <div class="text-center text-secondary p-3">
                    <img style="width: 50px" class="mt-3" src="/icon/invoice_icon.png" alt="Invoice Icon">
                    <div class="mt-3">Không tìm thấy hóa đơn nào</div>
                </div>
            `);
      return;
    }

    $("#div-invoice").removeClass("text-center mb-3").addClass("mb-3");
    $invoiceContainer.html(
      invoices
        .map(
          (invoice) => `
                <label class="form-selectgroup-item">
                    <input type="radio" name="name" value="${invoice.id}" class="form-selectgroup-input radio-invoice">
                    <span class="form-selectgroup-label">${invoice.maHoaDon} - ${invoice.tranThai}</span>
                </label>
            `
        )
        .join("")
    );

    $(".radio-invoice").on("change", function () {
      handleInvoiceChange(this.value);
    });
  } catch (error) {
    console.error("Error loading invoices:", error);
  }
};

// Event Handlers
const handleInvoiceChange = async (id) => {
  formData.invoiceId = id;
  enableElement();
  $("#search-customer").prop("disabled", false); // Bỏ disable input tìm kiếm khách hàng

  try {
    const response = await fetch(
      `http://localhost:8080/api/v1/ban-hang/hoa-don/${id}`,
      {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      }
    );

    const { data: invoice } = await response.json();

    formData.customer = invoice.khachHang;
    $("#input-note").val(invoice.ghiChu);
    $("#count-product").html(invoice.listSanPham.length);
    $("#tongTien, #tong").html(
      `${invoice.tongTien.toLocaleString("vi-VN")} VNĐ`
    );

    const $productTableBody = $(".product-table tbody");
    formData.totalItem = invoice.listSanPham.length;

    // Check and display customer information if any
    if (invoice.khachHang && invoice.khachHang.id) {
      const customer = invoice.khachHang;
      const customerId = String(customer.id);
      formData.customerId = customerId;

      const $customerInfo = $(".customer-info");
      const hasCustomerInfo = true;
      $customerInfo
        .html(
          `
                <div class="customer-name">
                    ${customer.tenKhachHang} - ${customer.soDienThoai} 
                    <span class="remove-customer-btn" style="cursor: pointer; color: #dc3545; margin-left: 5px;" data-customer-id="${customerId}">✕</span>
                    </div>
                ${
                  customer.diaChi &&
                  customer.diaChi !== "null, null, null, null"
                    ? `<div class="customer-address">${customer.diaChi}</div>`
                    : '<div class="customer-address">Chưa có địa chỉ</div>'
                }
                <div class="add-address-btn" data-customer-id="${customerId}">Thay đổi</div>
            `
        )
        .removeClass("text-center text-start")
        .addClass(hasCustomerInfo ? "text-start" : "text-center");
      // Handle remove customer
      $(document).on("click", ".remove-customer-btn", async function () {
        const customerId = $(this).data("customer-id");
        const invoiceId = formData.invoiceId;
        if (
          !confirm("Bạn có chắc chắn muốn xóa khách hàng khỏi hóa đơn này?")
        ) {
          return;
        }

        try {
          const response = await $.ajax({
            url: `http://localhost:8080/api/v1/ban-hang/hoa-don/${invoiceId}/remove-customer`,
            type: "DELETE",
            contentType: "application/json",
            dataType: "json",
          });

          if (response.status === 204) {
            // Reset thông tin khách hàng
            const $customerInfo = $(".customer-info");
            $customerInfo
              .html(
                `
                <img style="width: 50px" class="mt-3" src="/icon/id_card_icon.png" alt="Id Card Icon">
                <div class="mt-3">Chưa có thông tin khách hàng</div>
            `
              )
              .removeClass("text-center text-start")
              .addClass("text-center");

            formData.customer = null;
          } else {
            alert("Xóa khách hàng thất bại!");
          }
        } catch (error) {
          console.error("Error removing customer:", error);
          alert("Đã có lỗi xảy ra khi xóa khách hàng!");
        }
      });
    } else {
      const $customerInfo = $(".customer-info");
      const hasCustomerInfo = false;
      $customerInfo
        .html(
          `
                <img style="width: 50px" class="mt-3" src="/icon/id_card_icon.png" alt="Id Card Icon">
                <div class="mt-3">Chưa có thông tin khách hàng</div>
            `
        )
        .removeClass("text-center text-start")
        .addClass(hasCustomerInfo ? "text-start" : "text-center");
      formData.customerId = null;
      formData.currentCustomerId = null;
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
                            max="${sanPham.soLuongTonKho + sanPham.soLuong}" 
                            value="${sanPham.soLuong}" 
                            class="quantity-input form-control"
                            data-product-id="${sanPham.id}"
                        />
                    </td>
                    <td class="text-right">${formatPrice(sanPham.gia)}</td>
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
      removeProduct($(this).data("id"));
    });
  } catch (error) {
    console.error("Error loading invoice:", error);
  }
};

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
    if (data.status === 202) await handleInvoiceChange(formData.invoiceId);
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
    if (res.status === 202) await handleInvoiceChange(formData.invoiceId);
  } catch (error) {
    console.error("Error removing product:", error);
  }
};

// jQuery Document Ready
$(document).ready(() => {
  // Search Products
  $("#search-product").on(
    "input",
    debounce(async () => {
      const query = $("#search-product").val().trim().toLowerCase();
      if (!query) {
        $("#suggestions").empty().hide();
        return;
      }

      try {
        const response = await fetch(
          `http://localhost:8080/api/v1/ban-hang/san-pham?keyword=${query}`
        );
        const { data } = await response.json();

        if (data?.length) {
          $("#suggestions")
            .html(
              data
                .map(
                  (product) => `
                        <div class="suggestion-item" data-id="${product.id}">
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
          $("#suggestions").empty().hide();
        }
      } catch (error) {
        console.error("Search error:", error);
        $("#suggestions").empty().hide();
      }
    }, 500)
  );

  $(document).on("click", ".suggestion-item", async function () {
    const productId = $(this).data("id");
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/ban-hang/add-to-cart/${productId}/${formData.invoiceId}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({}),
        }
      );

      const result = await response.json();
      if (result.status === 202) await handleInvoiceChange(formData.invoiceId);
    } catch (error) {
      console.error("Add to cart error:", error);
    }
    $("#suggestions").empty().hide();
  });

  $(document).on("click", (e) => {
    if (!$(e.target).closest("#search-product, #suggestions").length) {
      $("#suggestions").empty().hide();
    }
  });

  // Note Update
  $("#input-note").on(
    "input",
    debounce(async (e) => {
      await fetch(
        `http://localhost:8080/api/v1/ban-hang/update-note/${formData.invoiceId}`,
        {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: e.target.value.trim() || " ",
        }
      );
    }, 2000)
  );

  // Order Type
  $(".radio-order-type").on("change", function () {
    formData.type = this.value;
    $("#div-address").toggle(this.value !== "Offline");
  });

  // Event Listeners
  $("#createInvoiceBtn").on("click", createInvoice);
  $("#btnCancel").on("click", async () => {
    if (!confirm("Xác nhận hủy hóa đơn?")) return;

    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/ban-hang/cancel-invoice/${formData.invoiceId}`,
        {
          method: "DELETE",
          headers: { "Content-Type": "application/json" },
        }
      );

      if (response.status === 200) {
        await clearData();
        window.location.reload();
      }
    } catch (error) {
      console.error("Cancel invoice error:", error);
    }
  });

  // Address Selection
  const $province = $("#province");
  const $district = $("#district");
  const $ward = $("#ward");

  fetch("https://provinces.open-api.vn/api/?depth=3")
    .then((res) => res.json())
    .then((data) => {
      $province.html(
        '<option value="">Chọn tỉnh/thành</option>' +
          data
            .map((p) => `<option value="${p.code}">${p.name}</option>`)
            .join("")
      );
      localStorage.setItem("provinceData", JSON.stringify(data));
    });

  $province.on("change", function () {
    const code = this.value;
    $district
      .html('<option value="">Chọn quận/huyện</option>')
      .prop("disabled", true);
    $ward
      .html('<option value="">Chọn xã/phường</option>')
      .prop("disabled", true);

    if (code) {
      const provinces = JSON.parse(localStorage.getItem("provinceData"));
      const province = provinces.find((p) => p.code == code);
      formData.province = province.name;

      $district
        .html(
          '<option value="">Chọn quận/huyện</option>' +
            province.districts
              .map((d) => `<option value="${d.code}">${d.name}</option>`)
              .join("")
        )
        .prop("disabled", false);
    }
  });

  $district.on("change", function () {
    const code = this.value;
    $ward
      .html('<option value="">Chọn xã/phường</option>')
      .prop("disabled", true);

    if (code) {
      const provinces = JSON.parse(localStorage.getItem("provinceData"));
      const district = provinces
        .flatMap((p) => p.districts)
        .find((d) => d.code == code);
      formData.district = district.name;

      $ward
        .html(
          '<option value="">Chọn xã/phường</option>' +
            district.wards
              .map((w) => `<option value="${w.name}">${w.name}</option>`)
              .join("")
        )
        .prop("disabled", false);
    }
  });

  // Form Inputs
  $("#ward").on("change", (e) => (formData.ward = e.target.value));
  $("#address-detail").on(
    "input",
    (e) => (formData.addressDetail = e.target.value)
  );
  $("#recipient_name").on(
    "input",
    (e) => (formData.recipient_name = e.target.value)
  );
  $("#phone_number").on(
    "input",
    (e) => (formData.phone_number = e.target.value)
  );
  $("#email").on("input", (e) => (formData.email = e.target.value));
  $('[name="paymentMethod"]').on("change", function () {
    formData.paymentMethod = this.value;
  });

  $("#search-customer").on(
    "input",
    debounce(async () => {
      const query = $("#search-customer").val().trim().toLowerCase();
      if (!query) {
        $("#customer-suggestions").empty().hide();
        return;
      }

      try {
        const response = await fetch(
          `http://localhost:8080/api/v1/ban-hang/khach-hang?keyword=${encodeURIComponent(
            query
          )}`,
          {
            method: "GET",
            headers: { "Content-Type": "application/json" },
          }
        );

        const { data: customers } = await response.json();

        if (customers?.length) {
          $("#customer-suggestions")
            .html(
              `
            <div class="customer-suggestion-item add-new-customer" style="font-weight: bold; color: #007bff;">
                <div class="customer-info">
                    <div>Thêm khách hàng mới</div>
                </div>
            </div>
            ${customers
              .map(
                (customer) => `
                    <div class="customer-suggestion-item" data-id="${customer.id}">
                        <div class="customer-info">
                            <div>${customer.tenKhachHang}</div>
                            <small>${customer.maKhachHang} - ${customer.soDienThoai}</small>
                        </div>
                    </div>
                `
              )
              .join("")}
        `
            )
            .show();
        } else {
          $("#customer-suggestions").empty().hide();
        }
      } catch (error) {
        console.error("Search customer error:", error);
        $("#customer-suggestions").empty().hide();
      }
    }, 500)
  );

  // Handle customer selection
  $(document).on("click", ".customer-suggestion-item", async function () {
    const customerId = String($(this).data("id"));
    console.log(
      `Mã khách hàng: ${customerId} - Mã hóa đơn: ${formData.invoiceId}`
    );

    try {
      console.log("Add");
      const response = await fetch(
        `http://localhost:8080/api/v1/ban-hang/add-customer/${formData.invoiceId}/${customerId}`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
        }
      );

      if (response.status === 200)
        await handleInvoiceChange(formData.invoiceId);
    } catch (error) {
      console.error("Search customer error:", error);
    } finally {
      $("#customer-suggestions").empty().hide();
    }
  });

  // Handle click on "Thay đổi" button to open modal
  $(document).on("click", ".add-address-btn", function () {
    const customerId = $(this).data("customer-id");
    formData.currentCustomerId = customerId;
    $("#addAddressModal").modal("show");
  });

    // Handle click on "Thêm khách hàng mới"
    $(document).on('click', '.add-new-customer', function () {
        $("#deliveryInfoModal").modal("show");
    });
  loadInvoices();
});
