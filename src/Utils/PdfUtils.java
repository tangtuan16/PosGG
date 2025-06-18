package Utils;

import Models.Product;
import Models.Sales.CartItem;
import Services.SaleService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfUtils {
    private static SaleService saleService = new SaleService();

    public static void generateInvoicePDF(
            String fileName,
            List<CartItem> cart,
            int invoiceId,
            BigDecimal total,
            BigDecimal discountPercent,
            BigDecimal finalTotal,
            String paymentMethod,
            String note,
            String customerName,
            String phoneNumber
    ) throws IOException {
        Document document = new Document(PageSize.A6, 20, 20, 20, 20);

        try {
            BaseFont baseFont = BaseFont.createFont("C:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 12, Font.BOLD, BaseColor.BLACK);
            Font headerFont = new Font(baseFont, 9, Font.BOLD, BaseColor.BLACK);
            Font boldFont = new Font(baseFont, 9, Font.BOLD, BaseColor.BLACK);
            Font normalFont = new Font(baseFont, 9, Font.NORMAL, BaseColor.BLACK);

            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            try {
                String imageURL = "https://res.cloudinary.com/dgeaae7vv/image/upload/v1747402303/pos/logo.png";
                Image logo = Image.getInstance(new URL(imageURL));
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(100, 60);
                document.add(logo);
            } catch (Exception e) {
                System.err.println("Không tải được logo: " + e.getMessage());
            }

            Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG #" + invoiceId, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            String currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            Paragraph dateTimePara = new Paragraph("Ngày: " + currentDateTime, normalFont);
            dateTimePara.setAlignment(Element.ALIGN_RIGHT);
            dateTimePara.setSpacingAfter(10);
            document.add(dateTimePara);

            Paragraph customerInfo = new Paragraph("Khách: " + (customerName != null && !customerName.isEmpty() ? customerName : "Khách lẻ"), boldFont);
            customerInfo.setSpacingAfter(3);
            document.add(customerInfo);

            Paragraph phoneInfo = new Paragraph("SĐT: " + (phoneNumber != null && !phoneNumber.isEmpty() ? phoneNumber : "Không có"), boldFont);
            phoneInfo.setSpacingAfter(8);
            document.add(phoneInfo);

            LineSeparator ls = new LineSeparator();
            ls.setLineColor(BaseColor.GRAY);
            document.add(new Chunk(ls));

            PdfPTable table = new PdfPTable(new float[]{4, 2, 2, 3});
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setSpacingAfter(10);

            addTableHeader(table, "Sản phẩm", headerFont);
            addTableHeader(table, "Đơn giá", headerFont);
            addTableHeader(table, "SL", headerFont);
            addTableHeader(table, "Thành tiền", headerFont);

            DecimalFormat decimalFormat = new DecimalFormat("#,###");

            for (CartItem item : cart) {
                Product product = item.getProduct();
                int quantity = item.getQuantity();
                BigDecimal unitPrice = saleService.calculateFinalPrice(product, 1);
                BigDecimal itemTotal = saleService.calculateFinalPrice(product, quantity);

                table.addCell(createCell(product.getName(), normalFont, Element.ALIGN_LEFT));
                table.addCell(createCell(decimalFormat.format(unitPrice), normalFont, Element.ALIGN_RIGHT));
                table.addCell(createCell(String.valueOf(quantity), normalFont, Element.ALIGN_CENTER));
                table.addCell(createCell(decimalFormat.format(itemTotal), normalFont, Element.ALIGN_RIGHT));
            }

            document.add(table);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(5);
            summaryTable.setSpacingAfter(5);
            summaryTable.setWidths(new float[]{4, 4});

            addSummaryRow(summaryTable, "Tổng cộng:", decimalFormat.format(total) + " VND", boldFont, normalFont);
            addSummaryRow(summaryTable, "Giảm giá:", discountPercent.stripTrailingZeros().toPlainString() + " %", boldFont, normalFont);
            addSummaryRow(summaryTable, "Thành tiền:", decimalFormat.format(finalTotal) + " VND", boldFont, normalFont);

            document.add(summaryTable);

            Paragraph paymentPara = new Paragraph("Thanh toán: " + (paymentMethod != null && !paymentMethod.isEmpty() ? paymentMethod : "Không xác định"), normalFont);
            paymentPara.setSpacingBefore(5);
            document.add(paymentPara);

            Paragraph notePara = new Paragraph("Ghi chú: " + (note != null && !note.isEmpty() ? note : "<3"), normalFont);
            notePara.setSpacingBefore(3);
            document.add(notePara);

            Paragraph thankYou = new Paragraph("Cảm ơn quý khách!", boldFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            thankYou.setSpacingBefore(15);
            document.add(thankYou);

        } catch (DocumentException e) {
            throw new IOException("Lỗi tạo hóa đơn PDF: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    private static void addTableHeader(PdfPTable table, String headerTitle, Font font) {
        PdfPCell header = new PdfPCell(new Phrase(headerTitle, font));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setBackgroundColor(new BaseColor(230, 230, 230));
        header.setPadding(5);
        table.addCell(header);
    }

    private static PdfPCell createCell(String content, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        return cell;
    }

    private static void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setPadding(3);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(3);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
