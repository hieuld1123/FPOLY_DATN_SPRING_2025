var globalData = null;
$(document).ready(function () {
  function fetchOrderData(orderId) {
    $.ajax({
      url: `http://localhost:8080/api/v1/hoa-don/${orderId}`,
      method: "GET",
      dataType: "json",
      success: function (response) {
        if (response.status === 200) {
          const data = response.data;
          globalData = data;
          renderOrderDetails(data);
        } else {
          alert("Không thể tải dữ liệu hóa đơn: " + response.message);
        }
      },
      error: function (xhr, status, error) {
        console.error("Lỗi khi gọi API:", error);
        alert("Đã xảy ra lỗi khi tải dữ liệu hóa đơn.");
      },
    });
  }

  function renderOrderDetails(data) {
    const confirmInvoice = document.getElementById("confirm-invoice");
    confirmInvoice.disabled = !data.confirm;
    if (!data.confirm) {
      confirmInvoice.style.display = "none";
    }

    const confirmDelivery = document.getElementById("confirm-delivery");
    confirmDelivery.disabled = !data.delivery;
    if (!data.delivery) {
      confirmDelivery.style.display = "none";
    }

    const btnCancelInvoice = document.getElementById("btn-cancel-invoice");
    btnCancelInvoice.disabled = !data.allowCancel;

    const btnModifyInvoice = document.getElementById("btn-modify-invoice");
    btnModifyInvoice.disabled = !data.allowCancel;

    const btnCompleted = document.getElementById("btn-completed");
    btnCompleted.disabled = !data.completed;
    if (!data.completed) {
      btnCompleted.style.display = "none";
    }

    $("#order-id").text(data.order_id);

    // Xử lý thông tin khách hàng
    const customer = data.customer || {};
    const customerName = customer.name || "Khách lẻ";
    const customerPhone = customer.phone || "Chưa có số điện thoại";
    const customerAddress =
      customer.delivery_address &&
      customer.delivery_address !== "null, null, null, null"
        ? customer.delivery_address
        : "Chưa có địa chỉ";

    $("#customer-header").html(`
        <span class="text-primary fs-3 fw-bold">${customerName}</span>
        <span class="ms-2 me-2">-</span>
        <span class="fs-3 fw-bold">${customerPhone}</span>
    `);
    $("#customer-information").html(`
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

    let timelineHtml = "";
    data.status_timeline.forEach((item) => {
      timelineHtml += `
                    <div class="timeline-item ${
                      item.completed ? "completed" : ""
                    }">
                        <div class="timeline-icon">${
                          item.completed ? "✓" : ""
                        }</div>
                        <div class="timeline-label">${item.status}</div>
                        <div class="timeline-time">${
                          item.time
                            ? new Date(item.time).toLocaleString()
                            : "Chưa có"
                        }</div>
                    </div>
                `;
    });
    $("#timeline").html(timelineHtml);

    $("#payment-info").html(`
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
      $("#paymentButton").hide();
    }

    $("#order-info").html(`
                <div class="row mb-2">
                    <div class="col-4 text-muted">Bán bởi</div>
                    <div class="col-8">${data.seller}</div>
                </div>
                <div class="row mb-2">
                    <div class="col-4 text-muted">Ngày bán</div>
                    <div class="col-8">${new Date(
                      data.order_date
                    ).toLocaleString()}</div>
                </div>
            `);

    $("#order-note").html(`<span>${data.note}</span>`);

    let productHtml = "";
    data.products.forEach((product, index) => {
      productHtml += `
                    <tr>
                        <td>${index + 1}</td>
                        <td><img src="${
                          product.image
                        }" alt="Product Image" width="50"></td>
                        <td>${product.name}<br><small>${
        product.code
      }</small></td>
                        <td>${product.quantity}</td>
                        <td class="text-start">${product.unit_price.toLocaleString()}VND</td>
                        <td class="text-start">${product.total_price.toLocaleString()}VND</td>
                    </tr>
                `;
    });
    $("#product-list").html(productHtml);

    $("#summary").html(`
                <div class="row mb-2">
                    <div class="col-8">Tổng tiền (${
                      data.products.length
                    } sản phẩm)</div>
                    <div class="col-4 text-end">${data.summary.subtotal.toLocaleString()}</div>
                </div>
                <div class="row mb-2">
                    <div class="col-8">Phí giao hàng</div>
                    <div class="col-4 text-end">${data.summary.shipping_fee.toLocaleString()}</div>
                </div>
                <div class="row mb-2">
                    <div class="col-8">Mã giảm giá</div>
                    <div class="col-4 text-end">${
                      data.summary.discount > 0 ? "-" : ""
                    } ${data.summary.discount.toLocaleString()}</div>
                </div>
                <div class="row mb-2">
                    <div class="col-8 fw-semibold">Thành tiền</div>
                    <div class="col-4 text-end fw-semibold">${data.summary.total.toLocaleString()}</div>
                </div>
            `);

    $("#paymentModal").on("show.bs.modal", function () {
      $("#paymentAmount").val(data.payment.total_amount.toLocaleString());
      $("#paymentDate").val(new Date().toLocaleString());
      $("#paymentMethod").val("Chuyển khoản");
      $("#paymentReference").val("");
    });

    // Khi nhấn "Thanh toán đơn hàng" trong modal
    $("#confirmPayment").on("click", function () {
      $("#paymentModal").modal("hide");
    });

    // "In đơn hàng"
    $("#printButton").on("click", function () {
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
            <p><strong>Ngày bán:</strong> ${new Date(
              data.order_date
            ).toLocaleString()}</p>
            <p><strong>Người bán:</strong> ${data.seller}</p>
        </div>
        <div class="section">
            <h3>Thông tin khách hàng</h3>
            <p><strong>Tên:</strong> ${data.customer.name}</p>
            <p><strong>Số điện thoại:</strong> ${
              data.customer.phone || "Chưa có số điện thoại"
            }</p>
            <p><strong>Địa chỉ giao hàng:</strong>
                    ${
                      data.customer.delivery_address &&
                      data.customer.delivery_address !==
                        "null, null, null, null"
                        ? data.customer.delivery_address
                        : "Chưa có địa chỉ"
                    }
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
                        <th>Tổng tiền</th>
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
            <div><span>Tổng tiền (${
              data.products.length
            } sản phẩm):</span><span>${data.summary.subtotal.toLocaleString()} VND</span></div>
            <div><span>Phí giao hàng:</span><span>${data.summary.shipping_fee.toLocaleString()} VND</span></div>
            <div><span>Mã giảm giá:</span><span>${
              data.summary.discount > 0 ? "-" : ""
            } ${data.summary.discount.toLocaleString()} VND</span></div>
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

  $("#confirm-invoice").on("click", function () {
    const orderId = window.location.pathname.split("/").pop();
    if (confirm("Bạn có chắc chắn muốn xác nhận hóa đơn này?")) {
      $.ajax({
        url: `http://localhost:8080/api/v1/hoa-don/confirm/${orderId}`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({}),
        success: function (response) {
          if (response.status === 200) {
            alert("Xác nhận hóa đơn thành công!");
            window.location.reload();
          } else {
            alert("Xác nhận hóa đơn thất bại: " + response.message);
          }
        },
        error: function (xhr, status, error) {
          console.error("Lỗi khi xác nhận hóa đơn:", error);
          alert("Đã xảy ra lỗi khi xác nhận hóa đơn.");
        },
      });
    }
  });

  $("#confirm-delivery").on("click", function () {
    const orderId = window.location.pathname.split("/").pop();
    if (confirm("Bạn có chắc chắn muốn vận chuyển hóa đơn này?")) {
      $.ajax({
        url: `http://localhost:8080/api/v1/hoa-don/delivery/${orderId}`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({}),
        success: function (response) {
          if (response.status === 200) {
            alert("Vận chuyển hóa đơn thành công!");
            window.location.reload();
          } else {
            alert("Vận chuyển hóa đơn thất bại: " + response.message);
          }
        },
        error: function (xhr, status, error) {
          console.error("Lỗi khi vận chuyển hóa đơn:", error);
          alert("Đã xảy ra lỗi khi vận chuyển hóa đơn.");
        },
      });
    }
  });

  $("#confirmPayment").on("click", function () {
    const orderId = window.location.pathname.split("/").pop();
    if (confirm("Bạn có chắc chắn thanh toán hóa đơn này?")) {
      $.ajax({
        url: `http://localhost:8080/api/v1/hoa-don/payment/${orderId}`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({}),
        success: function (response) {
          if (response.status === 200) {
            alert("Thanh toán hóa đơn thành công!");
            window.location.reload();
          } else {
            alert("Thanh toán hóa đơn thất bại: " + response.message);
          }
        },
        error: function (xhr, status, error) {
          console.error("Lỗi khi thanh toán hóa đơn:", error);
          alert("Đã xảy ra lỗi khi thanh toán hóa đơn.");
        },
      });
    }
  });

  $("#btn-cancel-invoice").on("click", function () {
    const orderId = window.location.pathname.split("/").pop();
    if (confirm("Bạn có chắc chắn hủy hóa đơn này?")) {
      $.ajax({
        url: `http://localhost:8080/api/v1/hoa-don/cancel/${orderId}`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({}),
        success: function (response) {
          if (response.status === 200) {
            alert("Hủy hóa đơn thành công!");
            window.location.reload();
          } else {
            alert("Hủy hóa đơn thất bại: " + response.message);
          }
        },
        error: function (xhr, status, error) {
          console.error("Lỗi khi hủy hóa đơn:", error);
          alert("Đã xảy ra lỗi khi hủy hóa đơn.");
        },
      });
    }
  });

  $("#btn-modify-invoice").on("click", function () {
    const orderId = window.location.pathname.split("/").pop();
      $("#recipientName").val(globalData.customer.name || "");
      $("#recipientPhone").val(globalData.customer.phone || "");
      $("#editRecipientModal").modal("show");
    loadProvinces($("#province"), $("#district"), $("#ward"), "recipient_");
  });
  // Hàm tải danh sách tỉnh/thành phố, quận/huyện, xã/phường
  const loadProvinces = async (
    provinceSelect,
    districtSelect,
    wardSelect,
    formDataKey
  ) => {
    try {
      const response = await fetch(
        "https://provinces.open-api.vn/api/?depth=3",
        {
          method: "GET",
          headers: {
            Accept: "application/json",
          },
        }
      );

      if (!response.ok) {
        throw new Error("Không thể tải danh sách tỉnh/thành phố");
      }

      const data = await response.json();
      provinceSelect.html(
        '<option value="">Chọn tỉnh/thành</option>' +
          data
            .map((p) => `<option value="${p.code}">${p.name}</option>`)
            .join("")
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
        console.log(`Selected ward: ${e.target.value}`);
      });
    } catch (error) {
      console.error("Error loading provinces:", error);
      provinceSelect.html('<option value="">Không thể tải tỉnh/thành</option>');
      alert(
        "Không thể tải danh sách tỉnh/thành phố. Vui lòng kiểm tra kết nối mạng hoặc thử lại sau!"
      );
    }
  };

  // Lưu thông tin người nhận
  $("#saveRecipientInfo").on("click", async function () {
    const orderId = window.location.pathname.split("/").pop();
      // Xóa thông báo lỗi cũ
      $(".error-message").text("");

      // Lấy dữ liệu từ form
      const recipientName = $("#recipientName").val().trim();
      const recipientPhone = $("#recipientPhone").val().trim();
      const province = $("#province").val();
      const district = $("#district").val();
      const ward = $("#ward").val();
      const specificAddress = $("#specificAddress").val().trim();

      let isValid = true;

      // Kiểm tra độ dài tên
      if (recipientName.length === 0) {
          isValid = false;
          $("#recipientName")
              .next(".error-message")
              .text("Tên người nhận không được để trống.");
      } else if (recipientName.length > 50) {
          isValid = false;
          $("#recipientName")
              .next(".error-message")
              .text("Tên không được vượt quá 50 ký tự.");
      }

      // Kiểm tra định dạng số điện thoại
      const phoneRegex = /^[0-9]{10,11}$/;
      if (recipientPhone.length === 0) {
          isValid = false;
          $("#recipientPhone")
              .next(".error-message")
              .text("Số điện thoại không được để trống.");
      } else if (!phoneRegex.test(recipientPhone)) {
          isValid = false;
          $("#recipientPhone")
              .next(".error-message")
              .text("Số điện thoại không hợp lệ. Vui lòng nhập 10-11 chữ số.");
      }

      // Kiểm tra Tỉnh/Thành phố
      if (!province) {
          isValid = false;
          $("#province")
              .next(".error-message")
              .text("Vui lòng chọn Tỉnh/Thành phố.");
      }

      // Kiểm tra Quận/Huyện
      if (!district) {
          isValid = false;
          $("#district")
              .next(".error-message")
              .text("Vui lòng chọn Quận/Huyện.");
      }

      // Kiểm tra Xã/Phường
      if (!ward) {
          isValid = false;
          $("#ward")
              .next(".error-message")
              .text("Vui lòng chọn Xã/Phường.");
      }

      // Kiểm tra Địa chỉ cụ thể
      if (specificAddress.length === 0) {
          isValid = false;
          $("#specificAddress")
              .next(".error-message")
              .text("Địa chỉ cụ thể không được để trống.");
      } else if (specificAddress.length > 100) {
          isValid = false;
          $("#specificAddress")
              .next(".error-message")
              .text("Địa chỉ cụ thể không được vượt quá 100 ký tự.");
      }

      // Nếu không hợp lệ, dừng việc gửi dữ liệu
      if (!isValid) {
          return;
      }

    const recipientData = {
      name: $("#recipientName").val(),
      phone: $("#recipientPhone").val(),
      province: $("#province option:selected").text(),
      district: $("#district option:selected").text(),
      ward: $("#ward option:selected").text(),
      specificAddress: $("#specificAddress").val(),
    };
    console.log(recipientData);
    
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/hoa-don/update-recipient/${orderId}`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(recipientData),
        }
      );

      const result = await response.json();
      if (result.status === 202) {
        alert("Cập nhật thông tin người nhận thành công!");
        $("#editRecipientModal").modal("hide");
        window.location.reload();
      } else {
        alert("Cập nhật thất bại: " + result.message);
      }
    } catch (error) {
      console.error("Error updating recipient info:", error);
      alert("Đã xảy ra lỗi khi cập nhật thông tin người nhận.");
    }
  });

  $("#btn-completed").on("click", function () {
    const orderId = window.location.pathname.split("/").pop();
    if (confirm("Xác nhận hoàn thành?")) {
      $.ajax({
        url: `http://localhost:8080/api/v1/hoa-don/completed/${orderId}`,
        method: "PATCH",
        contentType: "application/json",
        data: JSON.stringify({}),
        success: function (response) {
          if (response.status === 200) {
            alert("Đơn hàng hoàn tất!");
            window.location.reload();
          } else {
            alert("Thất bại: " + response.message);
          }
        },
        error: function (xhr, status, error) {
          console.error("Lỗi khi xác nhận hóa đơn:", error);
          alert("Đã xảy ra lỗi khi xác nhận hóa đơn.");
        },
      });
    }
  });

  const urlPath = window.location.pathname;
  const orderId = urlPath.split("/").pop();
  fetchOrderData(orderId);
});
