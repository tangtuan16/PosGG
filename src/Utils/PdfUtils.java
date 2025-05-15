package Utils;

import Models.Sales.CartItem;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfUtils {

    public static void generateInvoicePDF(
            String fileName,
            List<CartItem> cart,
            BigDecimal total,
            BigDecimal discountPercent,
            BigDecimal finalTotal,
            String paymentMethod,
            String note,
            String customerName,
            String phoneNumber
    ) throws IOException {
        Document document = new Document();

        try {
            BaseFont baseFont = BaseFont.createFont("C:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 16, Font.BOLD);
            Font boldFont = new Font(baseFont, 12, Font.BOLD);
            Font normalFont = new Font(baseFont, 12);

            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            String currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            Paragraph dateTimePara = new Paragraph("Ngày tạo: " + currentDateTime, normalFont);
            dateTimePara.setAlignment(Element.ALIGN_RIGHT);
            document.add(dateTimePara);

            Paragraph customerInfo = new Paragraph(
                    "Khách hàng: " + customerName, boldFont);
            customerInfo.setAlignment(Element.ALIGN_LEFT);
            Paragraph phoneInfo = new Paragraph("Số điện thoại: " + phoneNumber, boldFont);
            phoneInfo.setAlignment(Element.ALIGN_LEFT);
            document.add(customerInfo);
            document.add(phoneInfo);

            document.add(new Paragraph("------------------------------------------------", normalFont));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);
            table.setWidths(new float[]{4, 2, 3});

            PdfPCell cell1 = new PdfPCell(new Phrase("Sản phẩm", boldFont));
            PdfPCell cell2 = new PdfPCell(new Phrase("Số lượng", boldFont));
            PdfPCell cell3 = new PdfPCell(new Phrase("Thành tiền", boldFont));

            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);

            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);

            for (CartItem item : cart) {
                table.addCell(new Phrase(item.getProduct().getName(), normalFont));
                table.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                table.addCell(new Phrase(item.getTotalPrice().toString(), normalFont));
            }

            document.add(table);


            document.add(new Paragraph("Tổng tiền: " + total + " VND", boldFont));
            document.add(new Paragraph("Giảm giá: " + discountPercent + "%", boldFont));
            document.add(new Paragraph("Thành tiền: " + finalTotal + " VND", boldFont));
            document.add(new Paragraph("Phương thức thanh toán: " + paymentMethod, normalFont));
            document.add(new Paragraph("Ghi chú: " + note, normalFont));

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
