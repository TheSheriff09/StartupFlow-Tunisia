package tn.esprit.Services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * QRCodeService — Generate and decode QR codes for startups.
 *
 * Encoded payload format:  {@code STARTUPFLOW::<startupID>::<startupName>}
 *
 * This allows fast lookup by ID while embedding a human-readable name
 * for debugging / display purposes.
 */
public final class QRCodeService {

    private static final String PREFIX = "STARTUPFLOW::";
    private static final int    DEFAULT_SIZE = 300;   // px

    private QRCodeService() {}

    // ─────────────────────────────────────────────────────────
    // Encode
    // ─────────────────────────────────────────────────────────

    /**
     * Builds the payload string that will be encoded into the QR.
     */
    public static String buildPayload(int startupID, String startupName) {
        String safeName = (startupName != null) ? startupName.replace("::", "-") : "";
        return PREFIX + startupID + "::" + safeName;
    }

    /**
     * Generates a QR code as a JavaFX Image.
     *
     * @param startupID   PK of the startup
     * @param startupName human-readable name
     * @param size        width/height in pixels (square)
     * @return JavaFX Image of the QR code
     */
    public static Image generateQRImage(int startupID, String startupName, int size)
            throws WriterException {
        String payload = buildPayload(startupID, startupName);

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size, hints);
        BufferedImage buffered = MatrixToImageWriter.toBufferedImage(matrix);
        return SwingFXUtils.toFXImage(buffered, null);
    }

    /** Convenience overload — uses default 300 px size. */
    public static Image generateQRImage(int startupID, String startupName)
            throws WriterException {
        return generateQRImage(startupID, startupName, DEFAULT_SIZE);
    }

    /**
     * Generates a QR code that encodes an arbitrary URL (e.g. a local web-server URL).
     * Phones scanning this will open their browser and navigate to the page.
     *
     * @param url  the full URL to encode (e.g. "http://192.168.1.10:54321/startup/5")
     * @param size width/height in pixels
     */
    public static Image generateQRImageFromUrl(String url, int size)
            throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size, hints);
        BufferedImage buffered = MatrixToImageWriter.toBufferedImage(matrix);
        return SwingFXUtils.toFXImage(buffered, null);
    }

    /**
     * Saves a URL-based QR code to a PNG file.
     *
     * @param url  the full URL to encode
     * @param size pixel size
     * @param file destination file
     */
    public static void saveQRToPNGFromUrl(String url, int size, File file)
            throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size, hints);
        MatrixToImageWriter.writeToPath(matrix, "PNG", file.toPath());
    }

    /**
     * Saves a QR code to a PNG file.
     *
     * @param startupID   PK of the startup
     * @param startupName human-readable name
     * @param size        pixel size
     * @param file        destination file
     */
    public static void saveQRToPNG(int startupID, String startupName, int size, File file)
            throws WriterException, IOException {
        String payload = buildPayload(startupID, startupName);

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size, hints);
        MatrixToImageWriter.writeToPath(matrix, "PNG", file.toPath());
    }

    // ─────────────────────────────────────────────────────────
    // Decode
    // ─────────────────────────────────────────────────────────

    /**
     * Decodes a QR image file and returns the startup ID if valid.
     *
     * @param imageFile the PNG/JPG QR image
     * @return the startup ID encoded in the QR, or -1 if invalid / unreadable
     */
    public static int decodeStartupID(File imageFile) throws IOException, NotFoundException {
        BufferedImage buffered = ImageIO.read(imageFile);
        if (buffered == null) throw new IOException("Cannot read image: " + imageFile.getName());

        BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(buffered)));

        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        Result result = new MultiFormatReader().decode(bitmap, hints);
        String text = result.getText();

        return parseStartupID(text);
    }

    /**
     * Parses the startup ID from a decoded QR payload string.
     *
     * Supports two formats:
     *   • Legacy text  : "STARTUPFLOW::42::EcoTech"
     *   • URL (new)    : "http://192.168.x.x:PORT/startup/42"
     *
     * @param payload the raw string decoded from the QR
     * @return the startup ID, or -1 if the format is unrecognised
     */
    public static int parseStartupID(String payload) {
        if (payload == null) return -1;

        // ── New format: URL containing /startup/{id} ──────────
        if (payload.startsWith("http://") || payload.startsWith("https://")) {
            // Extract the last path segment after "/startup/"
            int idx = payload.lastIndexOf("/startup/");
            if (idx >= 0) {
                String idPart = payload.substring(idx + "/startup/".length());
                // Strip any trailing query string or fragment
                int qIdx = idPart.indexOf('?');
                if (qIdx >= 0) idPart = idPart.substring(0, qIdx);
                int hIdx = idPart.indexOf('#');
                if (hIdx >= 0) idPart = idPart.substring(0, hIdx);
                try {
                    return Integer.parseInt(idPart.trim());
                } catch (NumberFormatException ignored) {}
            }
            return -1;
        }

        // ── Legacy format: STARTUPFLOW::42::EcoTech ──────────
        if (!payload.startsWith(PREFIX)) return -1;
        String rest = payload.substring(PREFIX.length()); // "42::EcoTech"
        int sep = rest.indexOf("::");
        String idStr = (sep > 0) ? rest.substring(0, sep) : rest;
        try {
            return Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

