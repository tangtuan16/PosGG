package Controllers;

import Models.Sales.Invoice;
import Models.Sales.InvoiceItem;
import Services.InvoiceService;

import java.util.Date;
import java.util.List;


public class InvoiceController {
    private InvoiceService invoiceService;

    public InvoiceController() {
        invoiceService = new InvoiceService();
    }

    public List<Invoice> searchInvoices(String keyword, String fromDate, String toDate) {
        return invoiceService.search(keyword, fromDate, toDate);
    }

    public List<InvoiceItem> getInvoiceItems(int invoiceId) {
        return invoiceService.getInvoiceItems(invoiceId);
    }
}
