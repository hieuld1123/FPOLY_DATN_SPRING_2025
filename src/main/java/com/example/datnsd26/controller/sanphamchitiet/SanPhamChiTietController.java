package com.example.datnsd26.controller.sanphamchitiet;

import com.example.datnsd26.models.*;
import com.example.datnsd26.info.SanPhamChiTietInfo;
import com.example.datnsd26.info.ThuocTinhInfo;
import com.example.datnsd26.repository.*;
import com.example.datnsd26.services.CapNhatGiaKMServie;
import com.example.datnsd26.services.impl.*;
import com.example.datnsd26.utilities.CloudinaryUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@Controller
public class SanPhamChiTietController {
    @Autowired
    ChatLieuRepository chatLieuRepository;
    @Autowired
    MauSacRepository mauSacRepository;
    @Autowired
    KichCoRepository kichCoRepository;
    @Autowired
    DeGiayRepository deGiayRepository;
    @Autowired
    ThuongHieuRepository thuongHieuRepository;
    @Autowired
    SanPhamRepositoty sanPhamRepositoty;
    @Autowired
    SanPhamChiTietRepository sanPhamChiTietRepository;
    @Autowired
    SanPhamChiTietImp sanPhamChiTietImp;
    @Autowired
    SanPhamImp sanPhamImp;
    @Autowired
    ThuongHieuImp thuongHieuImp;

    @Autowired
    MauSacImp mauSacImp;

    @Autowired
    KichCoImp kichCoImp;

    @Autowired
    DeGiayImp deGiayImp;

    @Autowired
    ChatLieuImp chatLieuImp;

    @Autowired
    HinhAnhImp anhImp;

    @Autowired
    HinhAnhRepository hinhAnhRepository;

    @Autowired
    CloudinaryUtil cloudinaryUtil;

    @Autowired
    HinhAnhRepository anhRepository;


    //Cập nhật trạng thái sản phẩm chi tiết

    @PostMapping("/admin/chi-tiet-san-pham/updateTrangThai/{id}")
    public String updateTrangThaiCTSP(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(id).orElse(null);
        if (spct != null) {
            spct.setTrangThai(!spct.getTrangThai());
            sanPhamChiTietRepository.save(spct);
        }
        List<SanPhamChiTiet> listSPCT = sanPhamChiTietRepository.findBySanPhamId(spct.getSanPham().getId());
        int check = 1;
        for (SanPhamChiTiet spct1 : listSPCT) {
            if (spct1.getTrangThai() == false) {
                check = 2;
            }
            if (spct1.getTrangThai() == true) {
                check = 1;
            }
        }
        if (check == 2) {
            SanPham sanPham = sanPhamRepositoty.findById(spct.getSanPham().getId()).orElse(null);
            if (sanPham != null) {
                sanPham.setTrangThai(false);
                sanPhamRepositoty.save(sanPham);
            }
        }
        if (check == 1) {
            SanPham sanPham = sanPhamRepositoty.findById(spct.getSanPham().getId()).orElse(null);
            if (sanPham != null) {
                sanPham.setTrangThai(true);
                sanPhamRepositoty.save(sanPham);
            }
        }
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/quan-ly/chi-tiet-san-pham/" + spct.getSanPham().getId();
    }


    @GetMapping("/quan-ly/san-pham-chi-tiet")
    public String allSPCT(Model model, @ModelAttribute("search") SanPhamChiTietInfo info) {
        List<SanPhamChiTiet> list;
        String trimmedKey = (info.getKey() != null) ? info.getKey().trim().replaceAll("\\s+", " ") : null;
        boolean isKeyEmpty = (trimmedKey == null || trimmedKey.isEmpty());
        Boolean parsedTrangThai = info.getTrangThaiBoolean(); // Lấy giá trị trạng thái dưới dạng Boolean
        boolean isAllFiltersNull = (
                isKeyEmpty &&
                        info.getIdChatLieu() == null &&
                        info.getIdDeGiay() == null &&
                        info.getIdKichCo() == null &&
                        info.getIdMauSac() == null &&
                        info.getIdThuongHieu() == null &&
                        info.getGioiTinh() == null &&
                        info.getTrangThai() == null
        );
        if (isAllFiltersNull) {
            list = sanPhamChiTietRepository.findAll();
        } else {
            list = sanPhamChiTietRepository.search(
                    "%" + trimmedKey + "%",
                    "%" + trimmedKey + "%",
                    info.getIdThuongHieu(),
                    info.getIdDeGiay(),
                    info.getIdKichCo(),
                    info.getIdMauSac(),
                    info.getIdChatLieu(),
                    info.getGioiTinh(),
                    parsedTrangThai
            );
        }
        List<SanPham> listSanPham = sanPhamImp.findAll();
        List<ThuongHieu> listThuongHieu = thuongHieuRepository.getAll();
        List<MauSac> listMauSac = mauSacRepository.getAll();
        List<KichCo> listKichCo = kichCoRepository.getAll();
        List<DeGiay> listDeGiay = deGiayRepository.getAll();
        List<ChatLieu> listChatLieu = chatLieuRepository.getAll();
        List<SanPhamChiTiet> listSanPhamChiTiet = sanPhamChiTietRepository.findAll();
        model.addAttribute("sp", listSanPham);
        model.addAttribute("th", listThuongHieu);
        model.addAttribute("ms", listMauSac);
        model.addAttribute("kc", listKichCo);
        model.addAttribute("dg", listDeGiay);
        model.addAttribute("cl", listChatLieu);
        model.addAttribute("spct", listSanPhamChiTiet);
        model.addAttribute("listAllCTSP", list);
        model.addAttribute("fillSearch", trimmedKey);
        model.addAttribute("fillThuongHieu", info.getIdThuongHieu());
        model.addAttribute("fillDeGiay", info.getIdDeGiay());
        model.addAttribute("fillKichCo", info.getIdKichCo());
        model.addAttribute("fillMauSac", info.getIdMauSac());
        model.addAttribute("fillChatLieu", info.getIdChatLieu());
        model.addAttribute("fillGioiTinh", info.getGioiTinh());
        model.addAttribute("fillTrangThai", info.getTrangThai());
        return "admin/allchitietsanpham";
    }


    @GetMapping("/admin/updateCTSP/{id}")
    public String viewupdateCTSP(@PathVariable Integer id, Model model,
                                 @ModelAttribute("thuonghieu") ThuongHieu thuongHieu,
                                 @ModelAttribute("chatlieu") ChatLieu chatLieu,
                                 @ModelAttribute("kichco") KichCo kichCo,
                                 @ModelAttribute("degiay") DeGiay deGiay,
                                 @ModelAttribute("mausac") MauSac mauSac,
                                 @ModelAttribute("tim") ThuocTinhInfo info

    ) {
        //dùng cho validate
        List<DeGiay> listDG = deGiayRepository.findAll();
        model.addAttribute("listDG", listDG);
        List<ThuongHieu> listTH = thuongHieuRepository.findAll();
        model.addAttribute("listTH", listTH);
        List<ChatLieu> listCL = chatLieuRepository.findAll();
        model.addAttribute("listCL", listCL);
        List<MauSac> listMS = mauSacRepository.findAll();
        model.addAttribute("listMS", listMS);
        List<KichCo> listKC = kichCoRepository.findAll();
        model.addAttribute("listKC", listKC);

        List<SanPham> listSanPham = sanPhamImp.findAll();
        List<SanPhamChiTiet> listSPCT = sanPhamChiTietImp.findAll();
        List<ThuongHieu> listThuongHieu = thuongHieuRepository.getAll();
        List<MauSac> listMauSac = mauSacRepository.getAll();
        List<KichCo> listKichCo = kichCoRepository.getAll();
        List<DeGiay> listDeGiay = deGiayRepository.getAll();
        List<ChatLieu> listChatLieu = chatLieuRepository.getAll();
        List<HinhAnh> listHinhAnh = anhImp.findAll();
        model.addAttribute("sp", listSanPham);
        model.addAttribute("spct", listSPCT);
        model.addAttribute("th", listThuongHieu);
        model.addAttribute("ms", listMauSac);
        model.addAttribute("kc", listKichCo);
        model.addAttribute("dg", listDeGiay);
        model.addAttribute("cl", listChatLieu);
        model.addAttribute("a", listHinhAnh);
        model.addAttribute("hehe", sanPhamChiTietImp.findById(id));
        int soAnhConLai = 3 - sanPhamChiTietImp.findById(id).getHinhAnh().size();
        model.addAttribute("soAnhConLai", soAnhConLai);

        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietImp.findById(id);

        Float giagoc = sanPhamChiTiet.getGiaBan();
        model.addAttribute("hehe", sanPhamChiTiet);
        return "admin/detailCTSP";
    }


    @GetMapping("/admin/updateAllCTSP/{id}")
    public String viewupdateAllCTSP(@PathVariable Integer id, Model model,
                                    @ModelAttribute("thuonghieu") ThuongHieu thuongHieu,
                                    @ModelAttribute("chatlieu") ChatLieu chatLieu,
                                    @ModelAttribute("kichco") KichCo kichCo,
                                    @ModelAttribute("degiay") DeGiay deGiay,
                                    @ModelAttribute("mausac") MauSac mauSac,
                                    @ModelAttribute("tim") ThuocTinhInfo info

    ) {
        //dùng cho validate
        List<DeGiay> listDG = deGiayRepository.findAll();
        model.addAttribute("listDG", listDG);
        List<ThuongHieu> listTH = thuongHieuRepository.findAll();
        model.addAttribute("listTH", listTH);
        List<ChatLieu> listCL = chatLieuRepository.findAll();
        model.addAttribute("listCL", listCL);
        List<MauSac> listMS = mauSacRepository.findAll();
        model.addAttribute("listMS", listMS);
        List<KichCo> listKC = kichCoRepository.findAll();
        model.addAttribute("listKC", listKC);

        List<SanPham> listSanPham = sanPhamImp.findAll();
        List<SanPhamChiTiet> listSPCT = sanPhamChiTietImp.findAll();
        List<ThuongHieu> listThuongHieu = thuongHieuRepository.getAll();
        List<MauSac> listMauSac = mauSacRepository.getAll();
        List<KichCo> listKichCo = kichCoRepository.getAll();
        List<DeGiay> listDeGiay = deGiayRepository.getAll();
        List<ChatLieu> listChatLieu = chatLieuRepository.getAll();
        List<HinhAnh> listHinhAnh = anhImp.findAll();
        model.addAttribute("sp", listSanPham);
        model.addAttribute("spct", listSPCT);
        model.addAttribute("th", listThuongHieu);
        model.addAttribute("ms", listMauSac);
        model.addAttribute("kc", listKichCo);
        model.addAttribute("dg", listDeGiay);
        model.addAttribute("cl", listChatLieu);
        model.addAttribute("a", listHinhAnh);
        model.addAttribute("AllCTSP", sanPhamChiTietImp.findById(id));
        int soAnhConLai = 3 - sanPhamChiTietImp.findById(id).getHinhAnh().size();
        model.addAttribute("soAnhConLai", soAnhConLai);

        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietImp.findById(id);
        Float giagoc = sanPhamChiTiet.getGiaBan();
        model.addAttribute("hehe", sanPhamChiTiet);
        return "admin/ViewAllCTSP";
    }


    @PostMapping("/admin/updateCTSP/{id}")
    public String updateCTSP(@PathVariable Integer id, @ModelAttribute("hehe") SanPhamChiTiet sanPhamChiTiet,
                             @RequestParam(name = "anhs", required = false) List<MultipartFile> anhFiles,
                             @RequestParam(name = "spctIds") Integer spctId,
                             RedirectAttributes redirectAttributes, HttpSession session) throws IOException {

        LocalDateTime currentTime = LocalDateTime.now();
        SanPham sanPham = sanPhamRepositoty.findById(sanPhamChiTiet.getSanPham().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        ThuongHieu thuongHieu = thuongHieuRepository.findById(sanPhamChiTiet.getThuongHieu().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
        ChatLieu chatLieu = chatLieuRepository.findById(sanPhamChiTiet.getChatLieu().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chất liệu"));
        DeGiay deGiay = deGiayRepository.findById(sanPhamChiTiet.getDeGiay().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đế giày"));
        MauSac mauSac = mauSacRepository.findById(sanPhamChiTiet.getMauSac().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"));
        KichCo kichCo = kichCoRepository.findById(sanPhamChiTiet.getKichCo().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích cỡ"));
        SanPhamChiTiet existingSanPhamChiTiet = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        existingSanPhamChiTiet.setSanPham(sanPham);
        existingSanPhamChiTiet.setThuongHieu(thuongHieu);
        existingSanPhamChiTiet.setChatLieu(chatLieu);
        existingSanPhamChiTiet.setDeGiay(deGiay);
        existingSanPhamChiTiet.setMauSac(mauSac);
        existingSanPhamChiTiet.setKichCo(kichCo);
        existingSanPhamChiTiet.setNgayCapNhat(currentTime);
        existingSanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong());
        existingSanPhamChiTiet.setGiaBan(sanPhamChiTiet.getGiaBan());
        existingSanPhamChiTiet.setGiaBanSauGiam(sanPhamChiTiet.getGiaBan());
        existingSanPhamChiTiet.setMoTa(sanPhamChiTiet.getMoTa());
        existingSanPhamChiTiet.setTrangThai(sanPhamChiTiet.getTrangThai());
        existingSanPhamChiTiet.setGioiTinh(sanPhamChiTiet.getGioiTinh());

        sanPhamChiTietRepository.save(existingSanPhamChiTiet);

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(spctId).orElse(null);

        if (anhFiles != null && !anhFiles.isEmpty()) {
            for (MultipartFile file : anhFiles) {
                System.out.println("File name: " + file.getOriginalFilename());
                System.out.println("File size: " + file.getSize());
                byte[] fileBytes = file.getBytes();
                LocalDateTime now = LocalDateTime.now();
                String url = null;
                if (fileBytes.length > 0) {
                    Map<String, String> uploadResult = cloudinaryUtil.upload(file);
                    url = uploadResult.get("url");
                }
                System.out.println("URL trên Cloudinary:" + url);

                if (url != null) {
                    HinhAnh anh = new HinhAnh();
                    anh.setTenAnh(url); // Save the URL from Cloudinary
                    anh.setNgayTao(now);
                    anh.setTrangThai(true);
                    anh.setNgayCapNhat(now);
                    anh.setSanPhamChiTiet(spct);
                    anhRepository.save(anh);
                }
            }
        }

        Integer firstProductId = sanPhamChiTiet.getSanPham().getId();
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/quan-ly/chi-tiet-san-pham/" + firstProductId;
    }

    @GetMapping("/admin/updateCTSP/deleteImage/{idCTSP}/{idImage}")
    public String deleteImageInCTSPOfProduct(@PathVariable Integer idCTSP,
                              @PathVariable Integer idImage,
                              RedirectAttributes redirectAttributes) {

        hinhAnhRepository.deleteByIdAndSanPhamChiTietId(idImage, idCTSP);
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/admin/updateCTSP/" + idCTSP;
    }


    @PostMapping("/admin/updateAllCTSP/{id}")
    public String updateAllCTSP(@PathVariable Integer id, @ModelAttribute("AllCTSP") SanPhamChiTiet sanPhamChiTiet,
                                @RequestParam(name = "anhs", required = false) List<MultipartFile> anhFiles,
                                @RequestParam(name = "spctIds") Integer spctId,
                                RedirectAttributes redirectAttributes, HttpSession session) throws IOException {

        LocalDateTime currentTime = LocalDateTime.now();
        SanPham sanPham = sanPhamRepositoty.findById(sanPhamChiTiet.getSanPham().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        ThuongHieu thuongHieu = thuongHieuRepository.findById(sanPhamChiTiet.getThuongHieu().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
        ChatLieu chatLieu = chatLieuRepository.findById(sanPhamChiTiet.getChatLieu().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chất liệu"));
        DeGiay deGiay = deGiayRepository.findById(sanPhamChiTiet.getDeGiay().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đế giày"));
        MauSac mauSac = mauSacRepository.findById(sanPhamChiTiet.getMauSac().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"));
        KichCo kichCo = kichCoRepository.findById(sanPhamChiTiet.getKichCo().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích cỡ"));
        SanPhamChiTiet existingSanPhamChiTiet = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        existingSanPhamChiTiet.setSanPham(sanPham);
        existingSanPhamChiTiet.setThuongHieu(thuongHieu);
        existingSanPhamChiTiet.setChatLieu(chatLieu);
        existingSanPhamChiTiet.setDeGiay(deGiay);
        existingSanPhamChiTiet.setMauSac(mauSac);
        existingSanPhamChiTiet.setKichCo(kichCo);
        existingSanPhamChiTiet.setNgayCapNhat(currentTime);
        existingSanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong());
        existingSanPhamChiTiet.setGiaBan(sanPhamChiTiet.getGiaBan());
        existingSanPhamChiTiet.setGiaBanSauGiam(sanPhamChiTiet.getGiaBan());
        existingSanPhamChiTiet.setMoTa(sanPhamChiTiet.getMoTa());
        existingSanPhamChiTiet.setTrangThai(sanPhamChiTiet.getTrangThai());
        existingSanPhamChiTiet.setGioiTinh(sanPhamChiTiet.getGioiTinh());

        sanPhamChiTietRepository.save(existingSanPhamChiTiet);

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(spctId).orElse(null);

        if (anhFiles != null && !anhFiles.isEmpty()) {
            for (MultipartFile file : anhFiles) {
                System.out.println("File name: " + file.getOriginalFilename());
                System.out.println("File size: " + file.getSize());
                byte[] fileBytes = file.getBytes();
                LocalDateTime now = LocalDateTime.now();
                String url = null;
                if (fileBytes.length > 0) {
                    Map<String, String> uploadResult = cloudinaryUtil.upload(file);
                    url = uploadResult.get("url");
                }
                System.out.println("URL trên Cloudinary:" + url);

                if (url != null) {
                    HinhAnh anh = new HinhAnh();
                    anh.setTenAnh(url); // Save the URL from Cloudinary
                    anh.setNgayTao(now);
                    anh.setTrangThai(true);
                    anh.setNgayCapNhat(now);
                    anh.setSanPhamChiTiet(spct);
                    anhRepository.save(anh);
                }
            }
        }
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/quan-ly/san-pham-chi-tiet";
    }

    @GetMapping("/admin/updateAllCTSP/deleteImage/{idCTSP}/{idImage}")
    public String deleteImageInCTSPOfAllCTSP(@PathVariable Integer idCTSP,
                                             @PathVariable Integer idImage,
                                             RedirectAttributes redirectAttributes) {

        hinhAnhRepository.deleteByIdAndSanPhamChiTietId(idImage, idCTSP);
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/admin/updateAllCTSP/" + idCTSP;
    }

    //    private String saveImage(MultipartFile file) {
//        String uploadDir = "G:\\Ki7\\DATN\\DATN\\src\\main\\resources\\static\\upload";
//        try {
//            File directory = new File(uploadDir);
//            if (!directory.exists()) {
//                directory.mkdirs();
//            }
//            String originalFileName = file.getOriginalFilename();
//            String filePath = uploadDir + File.separator + originalFileName;
//            File dest = new File(filePath);
//            file.transferTo(dest);
//            return filePath;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


    private String saveImage(MultipartFile file) {
        String uploadDir = "D:\\DATN\\src\\main\\resources\\static\\upload";
        String dbUploadDir = "/upload";
        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String originalFileName = file.getOriginalFilename();
            String filePath = uploadDir + File.separator + originalFileName;
            File dest = new File(filePath);
            file.transferTo(dest);
            return dbUploadDir + "/" + originalFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/admin/updateGiaAndSoLuongCTSP")
    public String updateGiaAndSoLuong(
            Model model,
            @RequestParam("soluong") Integer soluong,
            @RequestParam("giatien") BigDecimal giatien,
            @RequestParam("choncheckbox") String[] choncheckbox,
            RedirectAttributes redirectAttributes
    ) {
        List<String> listString = Arrays.asList(choncheckbox);
        List<Integer> listInt = new ArrayList<>();
        for (String s : listString) {
            Integer i = Integer.parseInt(s);
            listInt.add(i);
        }
        listInt.remove(Integer.valueOf(-1));
        Integer firstProductId = null;
        if (!listInt.isEmpty()) {
            Integer firstSPCTId = listInt.get(0);
            firstProductId = sanPhamChiTietRepository.findIdBySanpham(firstSPCTId);
        }
        for (Integer id : listInt) {
            SanPhamChiTiet spctNeedToUpdateGia = sanPhamChiTietImp.findById(id);
            BigDecimal giaBanSauGiam = new BigDecimal(String.valueOf(giatien));
            spctNeedToUpdateGia.setGiaBanSauGiam(giaBanSauGiam.floatValue());
            sanPhamChiTietRepository.save(spctNeedToUpdateGia);
            sanPhamChiTietRepository.updateSoLuongVaGiaTienById(id, soluong, giatien);

        }

        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/quan-ly/chi-tiet-san-pham/" + firstProductId;
    }
}