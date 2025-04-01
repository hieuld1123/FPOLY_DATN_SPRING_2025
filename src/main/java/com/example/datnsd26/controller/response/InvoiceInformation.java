package com.example.datnsd26.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@Builder
public class InvoiceInformation {
    private boolean isConfirm;

    private boolean isDelivery;

    private boolean allowCancel;

    private boolean isCompleted;

    private String order_id;

    private String seller;

    private Date order_date;

    private String note;

    private List<StatusTimeline> status_timeline;

    private Customer customer;

    private Payment payment;

    private List<Product> products;

    private Summary summary;

    @Getter
    @Builder
    public static class StatusTimeline{
        private String status;

        private Date time;

        private boolean completed;
    }

    @Getter
    @Builder
    public static class Customer{
        private String name;

        private String phone;

        private String delivery_address;
    }

    @Getter
    @Builder
    public static class Payment{
        private float total_amount;

        private float paid_amount;

        private float remaining_amount;
    }

    @Getter
    @Builder
    public static class Product{
        private int id;

        private String image;

        private String name;

        private String code;

        private int quantity;

        private float unit_price;

        private float total_price;
    }

    @Getter
    @Builder
    public static class Summary{
        private float subtotal;

        private float shipping_fee;

        private float discount;

        private float total;
    }
}
