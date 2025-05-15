package Controllers;

import Models.Invoice;
import Services.InvoiceService;

import java.util.Date;
import java.util.List;


public class InvoiceController {
    private InvoiceService invoiceService;

    public InvoiceController() {
        invoiceService = new InvoiceService();
    }

    public List<Invoice> searchInvoices(String keyword, Date fromDate, Date toDate) {
        return invoiceService.search(keyword, fromDate, toDate);
    }
}
