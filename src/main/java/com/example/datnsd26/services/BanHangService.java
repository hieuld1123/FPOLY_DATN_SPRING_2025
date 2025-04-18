package com.example.datnsd26.services;

import com.example.datnsd26.controller.request.AddressRequest;
import com.example.datnsd26.controller.request.PaymentRequest;
import com.example.datnsd26.controller.request.StoreCustomerRequest;
import com.example.datnsd26.controller.response.*;
import jakarta.mail.MessagingException;

import java.util.List;

public interface BanHangService {
    List<HoaDonResponse> getHoaDon();

    HoaDonChiTietResponse getHoaDonChiTiet(int id);

    Integer createHoaDon();

    List<SanPhamResponse> getSanPhamByName(String keyword);

    int addToCart(int productId, int invoiceId);

    void updateSoLuong(int invoiceId, int quantity);

    void deleteItem(int itemId);

    void updateNote(int invoiceId, String note);

    void payment(PaymentRequest paymentRequest);

    void cancelInvoice(int invoiceId);

    List<CustomerResponse> getCustomerByInfo(String keyword);

    void addCustomerToInvoice(Integer invoiceId, Integer idKhachHang);

    void removeCustomer(Integer invoiceId);

    Integer createCustomer(Integer invoiceId, StoreCustomerRequest request) throws MessagingException;

    List<CustomerAddressResponse> customerAddresses(Integer customerId);

    void updateAddress(Integer customerId, Integer invoiceId, AddressRequest request);

    void updatePhone(Integer customerId, String phoneNumber, Integer invoiceId);

    void addCustomerInvoice(Integer invoiceId, StoreCustomerRequest request);
}
