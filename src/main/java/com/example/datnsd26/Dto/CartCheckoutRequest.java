package com.example.datnsd26.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartCheckoutRequest {

    private List<Integer> selectedIds;

}
