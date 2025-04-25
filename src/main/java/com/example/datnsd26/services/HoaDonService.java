package com.example.datnsd26.services;

import com.example.datnsd26.controller.request.InvoiceParamRequest;
import com.example.datnsd26.controller.request.InvoiceRecipientInfoRequest;
import com.example.datnsd26.controller.response.HoaDonChiTietResponse;
import com.example.datnsd26.controller.response.InvoiceInformation;
import com.example.datnsd26.controller.response.InvoicePageResponse;

public interface HoaDonService {
    InvoicePageResponse getInvoices(InvoiceParamRequest request);

    InvoiceInformation getInvoice(String code);

    void confirmInvoice(String code);

    void payment(String code);

    void confirmDelivery(String code);

    void cancel(String code);

    void edit(String code);

    HoaDonChiTietResponse editInvoice(String code);

    void completed(String code);

    void createHistoryModify(String invoiceCode);

    void updateRecipient(String orderId, InvoiceRecipientInfoRequest request);
}
