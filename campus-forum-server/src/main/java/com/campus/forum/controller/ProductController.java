package com.campus.forum.controller;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.Result;
import com.campus.forum.entity.ServiceProduct;
import com.campus.forum.entity.ServiceProductOrder;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/products")
public class ProductController {

    public static class ProductOrderCancelRequest {
        private String cancelReason;

        public String getCancelReason() {
            return cancelReason;
        }

        public void setCancelReason(String cancelReason) {
            this.cancelReason = cancelReason;
        }
    }

    public static class ProductOrderDecisionRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class ProductOrderMeetupRequest {
        private String meetupPlace;
        private String meetupTime;

        public String getMeetupPlace() {
            return meetupPlace;
        }

        public void setMeetupPlace(String meetupPlace) {
            this.meetupPlace = meetupPlace;
        }

        public String getMeetupTime() {
            return meetupTime;
        }

        public void setMeetupTime(String meetupTime) {
            this.meetupTime = meetupTime;
        }
    }

    public static class ProductOrderVerifyRequest {
        private String meetupCode;

        public String getMeetupCode() {
            return meetupCode;
        }

        public void setMeetupCode(String meetupCode) {
            this.meetupCode = meetupCode;
        }
    }

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<PageResult<ServiceProduct>> getProducts(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(productService.getProductList(pageNo, size, categoryId, tradeType, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<ServiceProduct> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @PostMapping
    public Result<ServiceProduct> createProduct(@RequestBody ServiceProduct product,
            @AuthenticationPrincipal SysUser currentUser) {
        product.setUserId(currentUser.getId());
        return Result.success(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public Result<ServiceProduct> updateProduct(@PathVariable Long id,
            @RequestBody ServiceProduct product,
            @AuthenticationPrincipal SysUser currentUser) {
        product.setId(id);
        return Result.success(productService.updateProduct(product, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        productService.deleteProduct(id, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/{id}/want")
    public Result<Void> wantProduct(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        productService.wantProduct(id, currentUser.getId());
        return Result.success();
    }

    @GetMapping("/my")
    public Result<PageResult<ServiceProduct>> getMyProducts(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(productService.getMyProducts(currentUser.getId(), pageNo, size));
    }

    @PostMapping("/{id}/off")
    public Result<Void> offProduct(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        productService.updateSaleStatus(id, currentUser.getId(), 0);
        return Result.success();
    }

    @PostMapping("/{id}/sold")
    public Result<Void> soldProduct(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        productService.updateSaleStatus(id, currentUser.getId(), 2);
        return Result.success();
    }

    @PostMapping("/{id}/wanted/status")
    public Result<Void> updateWantedStatus(@PathVariable Long id,
                                           @RequestParam Integer status,
                                           @AuthenticationPrincipal SysUser currentUser) {
        productService.updateWantedStatus(id, currentUser.getId(), status);
        return Result.success();
    }

    @PostMapping("/{id}/order")
    public Result<ServiceProductOrder> createOrder(@PathVariable Long id,
            @AuthenticationPrincipal SysUser currentUser) {
        return Result.success(productService.createOrder(id, currentUser.getId()));
    }

    @PostMapping("/orders/{orderId}/accept")
    public Result<Void> acceptOrder(@PathVariable Long orderId,
                                    @AuthenticationPrincipal SysUser currentUser) {
        productService.acceptOrder(orderId, currentUser.getId());
        return Result.success();
    }

    @PostMapping("/orders/{orderId}/reject")
    public Result<Void> rejectOrder(@PathVariable Long orderId,
                                    @RequestBody(required = false) ProductOrderDecisionRequest request,
                                    @AuthenticationPrincipal SysUser currentUser) {
        productService.rejectOrder(orderId, currentUser.getId(), request == null ? null : request.getReason());
        return Result.success();
    }

    @PostMapping("/orders/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId,
            @RequestBody(required = false) ProductOrderCancelRequest request,
            @AuthenticationPrincipal SysUser currentUser) {
        String cancelReason = request == null ? null : request.getCancelReason();
        productService.cancelOrder(orderId, currentUser.getId(), cancelReason);
        return Result.success();
    }

    @PostMapping("/orders/{orderId}/meetup")
    public Result<Void> scheduleMeetup(@PathVariable Long orderId,
                                       @RequestBody ProductOrderMeetupRequest request,
                                       @AuthenticationPrincipal SysUser currentUser) {
        if (request == null) {
            throw new BusinessException(com.campus.forum.common.ResultCode.PARAM_ERROR, "约见参数不能为空");
        }
        productService.scheduleMeetup(orderId, currentUser.getId(), request.getMeetupPlace(), parseDateTime(request.getMeetupTime()));
        return Result.success();
    }

    @PostMapping("/orders/{orderId}/meetup/reschedule")
    public Result<Void> rescheduleMeetup(@PathVariable Long orderId,
                                         @RequestBody ProductOrderMeetupRequest request,
                                         @AuthenticationPrincipal SysUser currentUser) {
        if (request == null) {
            throw new BusinessException(com.campus.forum.common.ResultCode.PARAM_ERROR, "改约参数不能为空");
        }
        productService.rescheduleMeetup(orderId, currentUser.getId(), request.getMeetupPlace(), parseDateTime(request.getMeetupTime()));
        return Result.success();
    }

    @PostMapping("/orders/{orderId}/meetup/verify")
    public Result<Void> verifyMeetupCode(@PathVariable Long orderId,
                                         @RequestBody ProductOrderVerifyRequest request,
                                         @AuthenticationPrincipal SysUser currentUser) {
        if (request == null) {
            throw new BusinessException(com.campus.forum.common.ResultCode.PARAM_ERROR, "核销参数不能为空");
        }
        productService.verifyMeetupCode(orderId, currentUser.getId(), request.getMeetupCode());
        return Result.success();
    }

    @PostMapping("/orders/{orderId}/confirm")
    public Result<Void> confirmReceipt(@PathVariable Long orderId,
            @AuthenticationPrincipal SysUser currentUser) {
        productService.confirmReceipt(orderId, currentUser.getId());
        return Result.success();
    }

    @GetMapping("/orders/my")
    public Result<PageResult<ServiceProductOrder>> getMyOrders(
            @AuthenticationPrincipal SysUser currentUser,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(productService.getMyOrders(currentUser.getId(), role, keyword, pageNo, size));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('market:manage')")
    @GetMapping("/admin")
    public Result<PageResult<ServiceProduct>> getAdminProducts(
            @RequestParam(required = false) Long current,
            @RequestParam(required = false) Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(required = false) String keyword) {
        Long pageNo = current != null ? current : page;
        if (pageNo == null) {
            pageNo = 1L;
        }
        return Result.success(productService.getAdminProductList(pageNo, size, categoryId, status, auditStatus, tradeType, keyword));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('market:manage')")
    @PostMapping("/{id}/audit")
    public Result<Void> auditProduct(@PathVariable Long id,
            @RequestParam Integer auditStatus) {
        productService.auditProduct(id, auditStatus);
        return Result.success();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('market:manage')")
    @PostMapping("/admin/{id}/status")
    public Result<Void> updateProductStatusByAdmin(@PathVariable Long id,
            @RequestParam Integer status) {
        productService.updateSaleStatusByAdmin(id, status);
        return Result.success();
    }

    /** 管理员删除商品（不限制 owner） */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') or hasAuthority('market:manage')")
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteProductByAdmin(@PathVariable Long id) {
        productService.deleteProductByAdmin(id);
        return Result.success();
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(com.campus.forum.common.ResultCode.PARAM_ERROR, "约见时间不能为空");
        }
        try {
            return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception ignore) {
            try {
                return LocalDateTime.parse(value.trim());
            } catch (Exception e) {
                throw new BusinessException(com.campus.forum.common.ResultCode.PARAM_ERROR, "约见时间格式错误，示例：2026-04-18 19:30");
            }
        }
    }
}
