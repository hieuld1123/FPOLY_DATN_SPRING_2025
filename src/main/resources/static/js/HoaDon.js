let currentPage = 1;
let pageSize = 5;
let totalPages = 0;
let sortField = 'creationDate';
let sortDirection = 'asc';
let filterParams = {
    invoiceCode: '',
    startDate: '',
    endDate: '',
    status: '',
    customer: ''
};

function formatDateToISO(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toISOString();
}

// Hàm gọi API để lấy dữ liệu hóa đơn
function fetchInvoices(page, size, params) {
    // Convert startdate and enddate into ISO 8601 format
    const formattedStartDate = formatDateToISO(params.startDate);
    const formattedEndDate = formatDateToISO(params.endDate);

    const queryParams = new URLSearchParams({
        currentPage: page,
        pageSize: size,
        invoiceCode: params.invoiceCode.trim(),
        startDate: formattedStartDate,
        endDate: formattedEndDate,
        status: params.status,
        customer: params.customer,
        sortBy: sortField,
        sortDirection: sortDirection
    }).toString();

    $.ajax({
        url: `http://localhost:8080/api/v1/hoa-don?${queryParams}`,
        method: 'GET',
        success: function (response) {
            if (response.status === 200) {
                loadInvoices(response.data.content, response.data.pageNumber, response.data.totalPages);
                totalPages = response.data.totalPages;
            } else {
                alert('Không thể tải danh sách hóa đơn');
            }
        },
        error: function (xhr, status, error) {
            console.error('Lỗi khi gọi API:', error);
            alert('Lỗi khi gọi API');
        }
    });
}

// Display data
function loadInvoices(data, page, total) {
    const tbody = $("#invoice-table");
    tbody.empty();

    if (data.length === 0) {
        // Nếu không có dữ liệu, hiển thị thông báo
        tbody.append(`
            <tr>
                <td colspan="6" class="text-center text-muted">Không có dữ liệu</td>
            </tr>
        `);
    } else {
        // Nếu có dữ liệu, hiển thị danh sách hóa đơn
        data.forEach(invoice => {
            const customerName = invoice.customer === null ? "Khách lẻ" : invoice.customer;
            const formattedValue = invoice.value.toLocaleString('vi-VN') + " VNĐ";
            const creationDate = new Date(invoice.creationDate);
            const formattedDate = `${creationDate.getDate().toString().padStart(2, '0')}/` +
                `${(creationDate.getMonth() + 1).toString().padStart(2, '0')}/` +
                `${creationDate.getFullYear()} ` +
                `${creationDate.getHours().toString().padStart(2, '0')}:` +
                `${creationDate.getMinutes().toString().padStart(2, '0')}:` +
                `${creationDate.getSeconds().toString().padStart(2, '0')}`;
            const row = `
                <tr>
                    <td><a href="/admin/hoa-don/${invoice.id}">${invoice.id}</a></td>
                    <td>${customerName}</td>
                    <td>${invoice.purchaseMethod}</td>
                    <td>${formattedDate}</td>
                    <td>${invoice.status}</td>
                    <td>${formattedValue}</td>
                </tr>
            `;
            tbody.append(row);
        });
    }

    currentPage = page;
    totalPages = total;
    updatePagination(totalPages, page);
}

function updateSortIcons() {
    const sortIcon = $("#sort-creation-date .sort-icon");
    if (sortField === 'creationDate') {
        sortIcon.text(sortDirection === 'asc' ? '↑' : '↓');
    } else {
        sortIcon.text('↕');
    }
}

// Handle pagination
function updatePagination(totalPages, currentPage) {
    const pagination = $("#pagination");
    pagination.empty();

    if (totalPages < 2) {
        pagination.hide();
        return;
    }

    pagination.show();

    const maxVisiblePages = 5;

    // Button Previous
    pagination.append(`
        <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage - 1}">Previous</a>
        </li>
    `);

    // First page
    pagination.append(`
        <li class="page-item ${1 === currentPage ? 'active' : ''}">
            <a class="page-link" href="#" data-page="1">1</a>
        </li>
    `);

    if (totalPages > 1) {
        let startPage = Math.max(2, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages - 1, currentPage + Math.floor(maxVisiblePages / 2));

        if (currentPage - startPage < endPage - currentPage) {
            startPage = Math.max(2, endPage - maxVisiblePages + 1);
        } else if (endPage - currentPage < currentPage - startPage) {
            endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
        }

        if (startPage > 2) {
            pagination.append(`
                <li class="page-item disabled">
                    <span class="page-link">...</span>
                </li>
            `);
        }

        for (let i = startPage; i <= endPage; i++) {
            pagination.append(`
                <li class="page-item ${i === currentPage ? 'active' : ''}">
                    <a class="page-link" href="#" data-page="${i}">${i}</a>
                </li>
            `);
        }

        if (endPage < totalPages - 1) {
            pagination.append(`
                <li class="page-item disabled">
                    <span class="page-link">...</span>
                </li>
            `);
        }

        pagination.append(`
            <li class="page-item ${totalPages === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${totalPages}">${totalPages}</a>
            </li>
        `);
    }

    // Button Next
    pagination.append(`
        <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage + 1}">Next</a>
        </li>
    `);
}

// The function applies the filter
function applyFilters() {
    filterParams.invoiceCode = $("#filter-id").val();
    filterParams.startDate = $("#filter-start-date").val();
    filterParams.endDate = $("#filter-end-date").val();
    filterParams.status = $("#filter-status").val();
    filterParams.customer = $("#filter-customer").val();

    currentPage = 1; // Return to the first page after filtering
    fetchInvoices(currentPage, pageSize, filterParams, sortField, sortDirection);
}

// Download data for the first time and process events
$(document).ready(function () {
    fetchInvoices(currentPage, pageSize, filterParams, sortField, sortDirection);

    // Apply the filter
    $("#apply-filter").click(function () {
        applyFilters();
    });

    // Change pagesize
    $("#page-size-select").change(function () {
        pageSize = parseInt($(this).val());
        currentPage = 1;
        fetchInvoices(currentPage, pageSize, filterParams, sortField, sortDirection);
    });

    // Transfer
    $("#pagination").on("click", ".page-link", function (e) {
        e.preventDefault();
        const page = $(this).data("page");
        if (page && page >= 1 && page <= totalPages) {
            currentPage = page;
            fetchInvoices(currentPage, pageSize, filterParams, sortField, sortDirection);
        }
    });
    $("#sort-creation-date").click(function (e) {
        e.preventDefault();
        if (sortField === 'creationDate') {
            // Nếu đã sort theo creationDate thì đổi hướng
            sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            // Nếu chưa sort theo creationDate thì set mặc định desc
            sortField = 'creationDate';
            sortDirection = 'desc';
        }
        currentPage = 1;
        fetchInvoices(currentPage, pageSize, filterParams, sortField, sortDirection);
    });
});