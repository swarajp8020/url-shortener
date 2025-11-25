package com.swaraj.url_shortener.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class QrCodeGenerator {

    public static String generateQRCodeImage(String url) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);

            BufferedImage image = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < 250; x++) {
                for (int y = 0; y < 250; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
