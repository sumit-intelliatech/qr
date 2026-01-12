package com.generator.qr.controller;

import com.generator.qr.service.QrService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateQr(
            @RequestParam String upiId,
            @RequestParam String name) throws Exception {

        byte[] qr = qrService.generateUpiQr(upiId, name);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(qr);
    }
}
