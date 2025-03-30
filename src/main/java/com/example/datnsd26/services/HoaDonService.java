package com.example.datnsd26.services;

import com.example.datnsd26.controller.request.InvoiceParamRequest;
import com.example.datnsd26.controller.response.InvoiceInformation;
import com.example.datnsd26.controller.response.InvoicePageResponse;

public interface HoaDonService {
    InvoicePageResponse getInvoices(InvoiceParamRequest request);

    InvoiceInformation getInvoice(String code);
}
