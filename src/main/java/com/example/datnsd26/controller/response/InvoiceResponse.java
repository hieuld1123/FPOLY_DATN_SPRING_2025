package com.example.datnsd26.controller.response;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse implements Serializable {
    private String id;
    private String customer;
    private String purchaseMethod;
    private Date creationDate;
    private String status;
    private double value;
    private String recipientName;
    private String phoneNumber;
    private String address;
}
