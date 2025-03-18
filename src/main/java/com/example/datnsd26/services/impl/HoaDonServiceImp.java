package com.example.datnsd26.services.impl;

import com.example.datnsd26.controller.request.InvoiceParamRequest;
import com.example.datnsd26.controller.response.InvoicePageResponse;
import com.example.datnsd26.repository.customizeQuery.InvoiceCustomizeQuery;
import com.example.datnsd26.services.HoaDonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "HOA-DON-SERVICE")
public class HoaDonServiceImp implements HoaDonService {

    private final InvoiceCustomizeQuery invoiceCustomizeQuery;

    @Override
    public InvoicePageResponse getInvoices(InvoiceParamRequest request) {
        log.info("GET/hoa-don");
        return invoiceCustomizeQuery.getInvoices(request);
    }
}
