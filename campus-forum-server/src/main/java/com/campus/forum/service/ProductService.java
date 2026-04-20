package com.campus.forum.service;

import com.campus.forum.common.PageResult;
import com.campus.forum.entity.ServiceProduct;
import com.campus.forum.entity.ServiceProductOrder;

public interface ProductService {

    PageResult<ServiceProduct> getProductList(Long current, Long size, Long categoryId, Integer tradeType, Integer status, String keyword);

    ServiceProduct getProductDetail(Long id);

    ServiceProduct createProduct(ServiceProduct product);

    ServiceProduct updateProduct(ServiceProduct product, Long userId);

    void deleteProduct(Long id, Long userId);

    void wantProduct(Long productId, Long userId);

    PageResult<ServiceProduct> getMyProducts(Long userId, Long current, Long size);

    void updateSaleStatus(Long productId, Long userId, Integer status);

    void updateSaleStatusByAdmin(Long productId, Integer status);

    ServiceProductOrder createOrder(Long productId, Long buyerId);

    void acceptOrder(Long orderId, Long userId);

    void rejectOrder(Long orderId, Long userId, String reason);

    void scheduleMeetup(Long orderId, Long userId, String meetupPlace, java.time.LocalDateTime meetupTime);

    void rescheduleMeetup(Long orderId, Long userId, String meetupPlace, java.time.LocalDateTime meetupTime);

    void verifyMeetupCode(Long orderId, Long userId, String meetupCode);

    void cancelOrder(Long orderId, Long userId, String cancelReason);

    void confirmReceipt(Long orderId, Long userId);

    PageResult<ServiceProductOrder> getMyOrders(Long userId, String role, Long current, Long size);

    PageResult<ServiceProduct> getAdminProductList(Long current, Long size, Long categoryId, Integer status, Integer auditStatus, Integer tradeType, String keyword);

    void updateWantedStatus(Long productId, Long userId, Integer status);

    void auditProduct(Long productId, Integer auditStatus);
}
