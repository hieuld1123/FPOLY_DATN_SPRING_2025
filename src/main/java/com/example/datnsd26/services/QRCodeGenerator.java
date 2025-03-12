package com.example.datnsd26.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeGenerator {

    private static final String QR_CODE_IMAGE_PATH = "src/main/resources/static/qrcodes/";

    public String generateQRCodeImage(String text, String fileName) throws WriterException, IOException {
        int width = 200;
        int height = 200;
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        File qrFile = new File(QR_CODE_IMAGE_PATH + fileName + ".png");
        qrFile.getParentFile().mkdirs();
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", Paths.get(qrFile.getAbsolutePath()));

        return "/qrcodes/" + fileName + ".png";
    }

}
