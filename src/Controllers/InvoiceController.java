package Controllers;

import Models.Customer;
import Models.Sales.Invoice;
import Models.Sales.InvoiceItem;
import Services.InvoiceService;
import Views.Sales.InvoiceSearchFrame;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InvoiceController {
    private final InvoiceService invoiceService;
    private final InvoiceSearchFrame view;

    public InvoiceController(InvoiceSearchFrame view) {
        this.invoiceService = new InvoiceService();
        this.view = view;
    }

    public void search(String keyword, Date fromDate, Date toDate) {
        try {
            if (fromDate != null && toDate != null && toDate.before(fromDate)) {
                view.showError("Ngày kết thúc không được nhỏ hơn ngày bắt đầu.");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedFrom = fromDate != null ? sdf.format(fromDate) : "1970-01-01";
            String formattedTo = toDate != null ? sdf.format(toDate) : sdf.format(new Date());

            List<Invoice> invoices = invoiceService.searchInvoices(keyword, formattedFrom, formattedTo);
            view.updateTable(invoices);

            if (invoices.isEmpty()) {
                view.showInfo("Không tìm thấy hóa đơn nào phù hợp.");
            }
        } catch (RuntimeException e) {
            view.showError("Lỗi tìm kiếm hóa đơn: " + e.getMessage());
        }
    }


    public void reset() {
        try {
            view.updateTable(new ArrayList<>());
            search("", null, null);
        } catch (Exception e) {
            view.showError("Lỗi làm mới: " + e.getMessage());
        }
    }

    public void showInvoiceDetail(int invoiceId) {
        try {
            List<InvoiceItem> items = invoiceService.getInvoiceItems(invoiceId);
            Customer customer = invoiceService.getCustomerByInvoicesId(invoiceId);
            BigDecimal discount = invoiceService.getDiscountByInvoiceId(invoiceId);
            discount = discount != null ? discount : BigDecimal.ZERO;

            view.showInvoiceDetail(invoiceId, items, customer, discount);
        } catch (RuntimeException e) {
            view.showError("Lỗi hiển thị chi tiết hóa đơn: " + e.getMessage());
        }
    }
}