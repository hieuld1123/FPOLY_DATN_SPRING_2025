package com.example.datnsd26.controller;

import com.cloudinary.Cloudinary;
import com.example.datnsd26.utilities.CloudinaryUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cloudinary")
@RequiredArgsConstructor
public class BinhCloudinaryController {

    //Lấy Ultil
    private final CloudinaryUtil cloudinaryUtil;

    //hàm upload
    @PostMapping("/upload")
    public void uploadImage(@ModelAttribute("file") HinhAnhRequest file) {
        Map<String, String> upload = cloudinaryUtil.upload(file.getFile());
        log.info("url {}, public_id {}", upload.get("url"), upload.get("publicId"));
    }

    //hàm xóa như dưới:
    @DeleteMapping("/delete/{id}")
    public void deleteImage(@PathVariable("id") String id) {
        cloudinaryUtil.removeByPublicId(id);
    }

    @Getter
    @Setter
    public class HinhAnhRequest {
        private Integer id;

        private MultipartFile file;

        private String publicId;
    }
}
