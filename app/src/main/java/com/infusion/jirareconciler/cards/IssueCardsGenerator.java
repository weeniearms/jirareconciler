package com.infusion.jirareconciler.cards;

import android.content.Context;

import com.infusion.jirareconciler.model.Issue;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by rcieslak on 27/04/2015.
 */
@Singleton
public class IssueCardsGenerator {
    private final Context context;

    @Inject
    public IssueCardsGenerator(Context context) {
        this.context = context;
    }

    public void generateCards(List<Issue> issues, String fileName) throws DocumentException, IOException {
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, outputStream);
                document.open();

                generateIssueCards(issues, document);
            }
            finally {
                document.close();
                outputStream.close();
            }
        } catch (FileNotFoundException e) {
            // Ignore
        }
    }

    private static void generateIssueCards(List<Issue> issues, Document document) throws DocumentException {
        for (int i = 0; i < issues.size(); i+=3) {
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);

            for (int j = i; j < i + 3; j++) {
                if (j >= issues.size()) {
                    table.addCell(new PdfPCell());
                    continue;
                }

                Font idFont = new Font(Font.FontFamily.HELVETICA, 25, Font.BOLD);

                PdfPCell idCell = new PdfPCell(new Phrase(issues.get(j).getId(), idFont));
                idCell.setPadding(5f);
                table.addCell(idCell);
            }

            for (int j = i; j < i + 3; j++) {
                if (j >= issues.size()) {
                    table.addCell(new PdfPCell());
                    continue;
                }

                PdfPCell titleCell = new PdfPCell(new Paragraph(issues.get(j).getTitle()));
                titleCell.setPadding(5f);
                table.addCell(titleCell);
            }

            for (int j = i; j < i + 3; j++) {
                if (j >= issues.size()) {
                    table.addCell(new PdfPCell());
                    continue;
                }

                Image image = new BarcodeQRCode(issues.get(j).getId(), 170, 170, new HashMap<EncodeHintType, Object>()).getImage();
                PdfPCell qrCodeCell = new PdfPCell(image);
                qrCodeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                qrCodeCell.setVerticalAlignment(Element.ALIGN_CENTER);
                qrCodeCell.setPadding(1f);
                table.addCell(qrCodeCell);
            }

            document.add(table);
        }
    }
}
