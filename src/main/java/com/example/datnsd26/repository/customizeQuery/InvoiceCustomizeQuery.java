package com.example.datnsd26.repository.customizeQuery;

import com.example.datnsd26.controller.request.InvoiceParamRequest;
import com.example.datnsd26.controller.response.InvoicePageResponse;
import com.example.datnsd26.controller.response.InvoiceResponse;
import com.example.datnsd26.models.HoaDon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j(topic = "INVOICE-CUSTOMIZE-QUERY")
@Component
@RequiredArgsConstructor
public class InvoiceCustomizeQuery {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String LIKE_FORMAT = "%%%s%%";

    public InvoicePageResponse getInvoices(InvoiceParamRequest request) {
        //log.info("pageable {} {} date {} {} id {} status {}", request.getCurrentPage(), request.getPageSize(), request.getStartDate(), request.getEndDate(), request.getInvoiceCode(), request.getStatus());
        if (request.getCurrentPage() < 1) {
            request.setCurrentPage(1);
        }
        StringBuilder sql = new StringBuilder("SELECT i FROM HoaDon i");

        if (StringUtils.hasLength(request.getStatus())) {
            sql.append(" WHERE i.trangThai = :status");
        } else {
            sql.append(" WHERE i.trangThai not like :status");
        }

        if (StringUtils.hasLength(request.getInvoiceCode())) {
            sql.append(" AND i.maHoaDon like :invoiceCode");
        }

        if (request.getStartDate() != null) {
            sql.append(" AND i.ngayTao >= :startDate");
        }

        if (request.getEndDate() != null) {
            sql.append(" AND i.ngayTao <= :endDate");
        }

        if (StringUtils.hasLength(request.getCustomer())) {
            sql.append(" AND (i.tenNguoiNhan LIKE :customer OR i.sdtNguoiNhan LIKE :customer)");
        }

        sql.append(" ORDER BY i.ngayTao ASC");

        TypedQuery<HoaDon> query = entityManager.createQuery(sql.toString(), HoaDon.class);
        if (StringUtils.hasLength(request.getStatus())) {
            query.setParameter("status", String.format(LIKE_FORMAT, request.getStatus()));
        }else{
            query.setParameter("status", String.format(LIKE_FORMAT, "Đang chờ"));
        }

        if (StringUtils.hasLength(request.getInvoiceCode())) {
            query.setParameter("invoiceCode", String.format(LIKE_FORMAT, request.getInvoiceCode()));
        }

        if (request.getStartDate() != null) {
            query.setParameter("startDate", request.getStartDate());
        }

        if (request.getEndDate() != null) {
            query.setParameter("endDate", request.getEndDate());
        }

        if (StringUtils.hasLength(request.getCustomer())) {
            query.setParameter("customer", String.format(LIKE_FORMAT, request.getCustomer()));
        }

        query.setFirstResult((request.getCurrentPage() - 1) * request.getPageSize());
        query.setMaxResults(request.getPageSize());

        List<InvoiceResponse> invoices = query.getResultList().stream().map(i -> InvoiceResponse.builder()
                .id(i.getMaHoaDon())
                .customer(i.getKhachHang1() == null ? null : i.getKhachHang1().getHoTen())
                .purchaseMethod(i.getHinhThucMuaHang())
                .creationDate(i.getNgayTao())
                .status(i.getTrangThai())
                .value(i.getTongTien())
                .recipientName(i.getTenNguoiNhan())
                .phoneNumber(i.getSdtNguoiNhan())
                .address(i.getDiaChiNguoiNhan())
                .build()).toList();

        // TODO count invoices
        StringBuilder countPage = new StringBuilder("SELECT count(i) FROM HoaDon i");
        if (StringUtils.hasLength(request.getStatus())) {
            countPage.append(" WHERE i.trangThai = :status");
        } else {
            countPage.append(" WHERE i.trangThai not like :status");
        }

        if (StringUtils.hasLength(request.getInvoiceCode())) {
            countPage.append(" AND i.maHoaDon like :invoiceCode");
        }

        if (request.getStartDate() != null) {
            countPage.append(" AND i.ngayTao >= :startDate");
        }

        if (request.getEndDate() != null) {
            countPage.append(" AND i.ngayTao <= :endDate");
        }

        if (StringUtils.hasLength(request.getCustomer())) {
            countPage.append(" AND (i.tenNguoiNhan LIKE :customer OR i.sdtNguoiNhan LIKE :customer)");
        }

        TypedQuery<Long> countQuery = entityManager.createQuery(countPage.toString(), Long.class);
        if (StringUtils.hasLength(request.getStatus())) {
            countQuery.setParameter("status", String.format(LIKE_FORMAT, request.getStatus()));
        } else {
            countQuery.setParameter("status", String.format(LIKE_FORMAT, "Đang chờ"));
        }

        if (StringUtils.hasLength(request.getInvoiceCode())) {
            countQuery.setParameter("invoiceCode", String.format(LIKE_FORMAT, request.getInvoiceCode()));
        }

        if (request.getStartDate() != null) {
            countQuery.setParameter("startDate", request.getStartDate());
        }

        if (request.getEndDate() != null) {
            countQuery.setParameter("endDate", request.getEndDate());
        }

        if (StringUtils.hasLength(request.getCustomer())) {
            countQuery.setParameter("customer", String.format(LIKE_FORMAT, request.getCustomer()));
        }

        Pageable pageable = PageRequest.of(request.getCurrentPage() - 1, request.getPageSize());
        Page<?> page = new PageImpl<>(invoices, pageable, countQuery.getSingleResult());

        InvoicePageResponse response = new InvoicePageResponse();
        response.setPageNumber(request.getCurrentPage());
        response.setPageSize(request.getPageSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setContent(invoices);

        return response;
    }
}
