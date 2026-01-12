package com.generator.qr.service;

import com.generator.qr.utils.QrCodeUtil;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class QrService {

    public byte[] generateUpiQr(String upiId, String name) throws Exception {

        String upiUrl = "upi://pay?pa=" + upiId +
                "&pn=" + URLEncoder.encode(name, StandardCharsets.UTF_8) +
                "&cu=INR";

        // ✅ Generate QR WITHOUT logo
        BufferedImage bufferedImage = QrCodeUtil.generateQr(upiUrl);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);

        return baos.toByteArray();
    }
}
