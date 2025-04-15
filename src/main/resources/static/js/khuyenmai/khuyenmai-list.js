
document.addEventListener("DOMContentLoaded", function () {


    const searchForm = document.querySelector("form");

    searchForm.addEventListener("input", function () {
        clearTimeout(window.searchTimeout);
        window.searchTimeout = setTimeout(() => {
            searchForm.submit();
        }, 500); // Đợi 500ms sau khi nhập liệu xong mới gửi request
    });

    searchForm.addEventListener("change", function () {
        searchForm.submit();
    });
});

function searchKhuyenMai() {
    let tenChienDich = document.querySelector("input[name='tenChienDich']").value;
    let trangThai = document.querySelector("select[name='trangThai']").value;
    let startDate = document.querySelector("input[name='startDate']").value;
    let endDate = document.querySelector("input[name='endDate']").value;

    let url = `/admin/khuyen-mai/search?tenChienDich=${encodeURIComponent(tenChienDich)}&trangThai=${trangThai}&startDate=${startDate}&endDate=${endDate}`;

    fetch(url)
        .then(response => response.text())
        .then(data => {
            document.getElementById("khuyenMaiTable").innerHTML = data;
        })
        .catch(error => console.error("Lỗi tìm kiếm: ", error));
}

document.querySelector("input[name='tenChienDich']").addEventListener("input", searchKhuyenMai);
document.querySelector("select[name='trangThai']").addEventListener("change", searchKhuyenMai);
document.querySelector("input[name='startDate']").addEventListener("change", searchKhuyenMai);
document.querySelector("input[name='endDate']").addEventListener("change", searchKhuyenMai);

setTimeout(function() {
    document.querySelectorAll('.alert').forEach(function(alert) {
        bootstrap.Alert.getOrCreateInstance(alert).close();
    });
}, 5000);

function confirmDelete(id) {
    document.getElementById('khuyenMaiId').value = id;
    const deleteForm = document.getElementById('deleteForm');
    deleteForm.action = `/admin/khuyen-mai/delete/${id}`;
    new bootstrap.Modal(document.getElementById('deleteModal')).show();
}


function changePageSize(size) {
    window.location.href = `/admin/khuyen-mai?page=0&size=${size}`;
}

function restoreKhuyenMai(id) {
    Swal.fire({
        title: 'Xác nhận khôi phục',
        text: 'Bạn có chắc chắn muốn khôi phục khuyến mãi này?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Có',
        cancelButtonText: 'Không'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`/admin/khuyen-mai/restore/${id}`, {
                method: 'GET'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Khôi phục thất bại!');
                    }
                    return response.text();
                })
                .then(() => {
                    Swal.fire({
                        icon: 'success',
                        title: 'Khôi phục thành công!',
                        showConfirmButton: false,
                        timer: 1500
                    }).then(() => {
                        location.reload();
                    });
                })
                .catch((error) => {
                    Swal.fire({
                        icon: 'error',
                        title: 'Lỗi!',
                        text: error.message || 'Không thể khôi phục khuyến mãi.'
                    });
                });
        }
    });
}

function endKhuyenMai(id) {
    Swal.fire({
        title: 'Xác nhận kết thúc',
        text: 'Bạn có chắc chắn muốn kết thúc khuyến mãi này?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Có',
        cancelButtonText: 'Không'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`/admin/khuyen-mai/stop/${id}`, {
                method: 'GET'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Kết thúc thất bại!');
                    }
                    return response.text();
                })
                .then(() => {
                    Swal.fire({
                        icon: 'success',
                        title: 'Kết thúc thành công!',
                        showConfirmButton: false,
                        timer: 1500
                    }).then(() => {
                        location.reload();
                    });
                })
                .catch((error) => {
                    Swal.fire({
                        icon: 'error',
                        title: 'Lỗi!',
                        text: error.message || 'Không thể kết thúc khuyến mãi.'
                    });
                });
        }
    });
}
