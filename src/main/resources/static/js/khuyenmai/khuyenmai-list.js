
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