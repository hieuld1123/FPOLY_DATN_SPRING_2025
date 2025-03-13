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
});
