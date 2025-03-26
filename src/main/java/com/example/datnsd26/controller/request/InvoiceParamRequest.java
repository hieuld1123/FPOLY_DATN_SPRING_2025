package com.example.datnsd26.controller.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class InvoiceParamRequest {
    private int currentPage;

    private int pageSize = 5;

    private String invoiceCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date endDate;

    private String status;

    private String customer; // name or phone number

    private String sortDirection;
}
