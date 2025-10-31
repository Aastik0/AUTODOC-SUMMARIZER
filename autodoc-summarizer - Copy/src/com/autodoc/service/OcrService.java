package com.autodoc.service;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
// We do not need 'org.apache.pdfbox.Loader' for this older library version

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class OcrService {

    private final Tesseract tesseract;

    public OcrService() {
        this.tesseract = new Tesseract();
        // The TESSDATA_PREFIX environment variable must be set
    }

    /**
     * Extracts text from a given file (PDF or image).
     *
     * @param file The file to process.
     * @return The extracted text as a string.
     * @throws IOException        If there is an error reading the file.
     * @throws TesseractException If there is an error during OCR processing.
     */
    public String extractText(File file) throws IOException, TesseractException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist or is null.");
        }

        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".tiff")) {
            return extractTextFromImage(file);
        } else {
            throw new IOException("Unsupported file type: " + fileName);
        }
    }

    /**
     * Extracts text from an image file using Tesseract OCR.
     */
    private String extractTextFromImage(File imageFile) throws TesseractException {
        return tesseract.doOCR(imageFile);
    }

    /**
     * Extracts text from a PDF file using the older PDFBox method.
     */
    private String extractTextFromPdf(File pdfFile) throws IOException {
        // This is the correct method for the version of PDFBox bundled with Tess4J 3.4.8
        try (PDDocument document = PDDocument.load(pdfFile)) {
            if (document.isEncrypted()) {
                throw new IOException("Cannot extract text from an encrypted PDF.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}

