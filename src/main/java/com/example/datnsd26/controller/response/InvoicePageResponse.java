package com.example.datnsd26.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class InvoicePageResponse extends PageResponseAbstract implements Serializable {
    private List<InvoiceResponse> content;
}
