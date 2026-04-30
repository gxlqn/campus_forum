package com.campus.forum.service.impl;

import com.campus.forum.common.PageResult;
import com.campus.forum.common.ResultCode;
import com.campus.forum.entity.ForumPost;
import com.campus.forum.entity.ForumSection;
import com.campus.forum.entity.ServiceProduct;
import com.campus.forum.entity.ServiceProductOrder;
import com.campus.forum.entity.SysUser;
import com.campus.forum.exception.BusinessException;
import com.campus.forum.mapper.ForumPostMapper;
import com.campus.forum.mapper.ForumSectionMapper;
import com.campus.forum.mapper.ServiceProductMapper;
import com.campus.forum.mapper.ServiceProductOrderMapper;
import com.campus.forum.mapper.SysUserMapper;
import com.campus.forum.service.NoticeService;
import com.campus.forum.service.ProductService;
import com.campus.forum.service.SmartAuditService;
import com.campus.forum.search.SearchSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ServiceProductMapper productMapper;

    @Autowired
    private ServiceProductOrderMapper productOrderMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private ForumSectionMapper sectionMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private SmartAuditService smartAuditService;

    @Autowired
    private SearchSyncService searchSyncService;

    @Override
    public PageResult<ServiceProduct> getProductList(Long current, Long size, Long categoryId, Integer tradeType, Integer status, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<ServiceProduct> records = productMapper.selectPage(categoryId, tradeType, status, keyword, offset, pageSize);
        fillRelations(records);
        Long total = productMapper.countPage(categoryId, tradeType, status, keyword);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public ServiceProduct getProductDetail(Long id) {
        ServiceProduct product = productMapper.selectById(id);
        if (product == null || (product.getDeleted() != null && product.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        productMapper.increaseViewCount(id);
        product.setViewCount((product.getViewCount() == null ? 0 : product.getViewCount()) + 1);
        fillRelations(List.of(product));
        return product;
    }

    @Override
    public ServiceProduct createProduct(ServiceProduct product) {
        if (product == null || product.getUserId() == null || !StringUtils.hasText(product.getTitle()) || product.getPrice() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品信息不完整");
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getAuditStatus() == null) {
            product.setAuditStatus(0);
        }
        if (product.getIsNegotiable() == null) {
            product.setIsNegotiable(0);
        }
        if (product.getTradeType() == null) {
            product.setTradeType(1);
        }
        if (product.getProductCondition() == null) {
            product.setProductCondition(3);
        }

        // 先审核，再入库
        SmartAuditService.AuditResult auditResult = smartAuditService.auditProduct(
                product.getUserId(), product.getTitle(), product.getDescription(), null);

        if (auditResult.getAuditStatus() == SmartAuditService.AuditStatus.AUTO_REJECT.getCode()) {
            log.warn("商品内容被拦截(含违规关键词): userId={}, reason={}", product.getUserId(), auditResult.getReason());
            throw new BusinessException(ResultCode.CONTENT_AUDIT_BLOCKED, auditResult.getReason());
        }

        if (!auditResult.isPassed()) {
            log.warn("商品内容需人工审核: userId={}, reason={}", product.getUserId(), auditResult.getReason());
        }
        if (auditResult.getAuditStatus() > 0) {
            product.setAuditStatus(mapAuditStatusToContentStatus(auditResult.getAuditStatus()));
        } else if (product.getAuditStatus() == null) {
            product.setAuditStatus(0);
        }

        productMapper.insert(product);
        bindForumPost(product);

        ServiceProduct saved = productMapper.selectById(product.getId());
        fillRelations(List.of(saved));
        searchSyncService.syncProduct(saved.getId());
        return saved;
    }

    @Override
    public ServiceProduct updateProduct(ServiceProduct product, Long userId) {
        if (product == null || product.getId() == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (!StringUtils.hasText(product.getTitle()) || product.getPrice() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品标题和价格不能为空");
        }
        product.setUserId(userId);
        int changed = productMapper.updateByOwner(product);
        if (changed == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND, "商品不存在或无权限编辑");
        }
        ServiceProduct saved = productMapper.selectById(product.getId());
        fillRelations(List.of(saved));
        searchSyncService.syncProduct(saved.getId());
        return saved;
    }

    @Override
    public void deleteProduct(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        int changed = productMapper.deleteByOwner(id, userId);
        if (changed == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND, "商品不存在或无权限删除");
        }
        searchSyncService.deleteProduct(id);
    }

    @Override
    public void deleteProductByAdmin(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        int changed = productMapper.deleteById(id);
        if (changed == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND, "商品不存在或已被删除");
        }
        searchSyncService.deleteProduct(id);
    }

    @Override
    public void wantProduct(Long productId, Long userId) {
        int changed = productMapper.insertWant(productId, userId);
        if (changed > 0) {
            productMapper.updateWantCount(productId, 1);
        }
    }

    @Override
    public PageResult<ServiceProduct> getMyProducts(Long userId, Long current, Long size) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        List<ServiceProduct> records = productMapper.selectMyProducts(userId, offset, pageSize);
        fillRelations(records);
        Long total = productMapper.countMyProducts(userId);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void updateSaleStatus(Long productId, Long userId, Integer status) {
        ServiceProduct product = productMapper.selectById(productId);
        if (product == null || (product.getDeleted() != null && product.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作该商品");
        }
        product.setStatus(status);
        product.setUserId(userId);
        productMapper.updateByOwner(product);
        searchSyncService.syncProduct(productId);
    }

    @Override
    public void updateWantedStatus(Long productId, Long userId, Integer status) {
        ServiceProduct product = productMapper.selectById(productId);
        if (product == null || (product.getDeleted() != null && product.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作该求购信息");
        }
        if (!Integer.valueOf(2).equals(product.getTradeType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅求购信息支持该操作");
        }
        if (!Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status) && !Integer.valueOf(3).equals(status)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "求购状态仅支持 0-已关闭 1-开放中 3-已匹配");
        }

        product.setStatus(status);
        product.setUserId(userId);
        productMapper.updateByOwner(product);
        searchSyncService.syncProduct(productId);
    }

    @Override
    public void updateSaleStatusByAdmin(Long productId, Integer status) {
        ServiceProduct product = productMapper.selectById(productId);
        if (product == null || (product.getDeleted() != null && product.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        int changed = productMapper.updateStatusById(productId, status);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或状态更新失败");
        }
        searchSyncService.syncProduct(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceProductOrder createOrder(Long productId, Long buyerId) {
        if (productId == null || buyerId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ServiceProduct product = productMapper.selectById(productId);
        if (product == null || (product.getDeleted() != null && product.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(product.getAuditStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品未通过审核，暂不可下单");
        }
        if (product.getUserId().equals(buyerId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能购买自己发布的商品");
        }
        if (Integer.valueOf(2).equals(product.getStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_SOLD_OUT);
        }
        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品已进入交易中或不可申请");
        }

        ServiceProductOrder existing = productOrderMapper.selectActiveByProductAndBuyer(productId, buyerId);
        if (existing != null) {
            throw new BusinessException(ResultCode.REPEAT_OPERATION, "你已提交申请，请勿重复操作");
        }

        ServiceProductOrder order = new ServiceProductOrder();
        order.setOrderNo(generateOrderNo());
        order.setProductId(productId);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getUserId());
        order.setAmount(product.getPrice());
        order.setStatus(0);
        order.setMeetupCode(generateMeetupCode());
        order.setMeetupVerified(0);
        order.setRescheduleCount(0);
        productOrderMapper.insert(order);

        ServiceProductOrder saved = productOrderMapper.selectById(order.getId());
        fillOrderRelations(List.of(saved));
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long orderId, Long userId) {
        ServiceProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null || (order.getDeleted() != null && order.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_NOT_FOUND);
        }
        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅卖家可确认成交对象");
        }
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_STATUS_ERROR, "该申请已处理");
        }

        ServiceProduct product = productMapper.selectById(order.getProductId());
        if (product == null || (product.getDeleted() != null && product.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        int locked = productMapper.updateStatusWithExpected(product.getId(), 1, 3);
        if (locked == 0) {
            throw new BusinessException(ResultCode.REPEAT_OPERATION, "商品已被其他交易占用");
        }

        int accepted = productOrderMapper.acceptPendingOrder(orderId);
        if (accepted == 0) {
            throw new BusinessException(ResultCode.REPEAT_OPERATION, "申请状态已变更");
        }
        productOrderMapper.rejectOtherPendingByProduct(product.getId(), orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(Long orderId, Long userId, String reason) {
        ServiceProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null || (order.getDeleted() != null && order.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_NOT_FOUND);
        }
        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅卖家可处理申请");
        }
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_STATUS_ERROR, "该申请已处理");
        }
        int changed = productOrderMapper.rejectPendingOrder(orderId, StringUtils.hasText(reason) ? reason.trim() : "卖家暂未选择你");
        if (changed == 0) {
            throw new BusinessException(ResultCode.REPEAT_OPERATION, "申请状态已变更");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scheduleMeetup(Long orderId, Long userId, String meetupPlace, LocalDateTime meetupTime) {
        ServiceProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null || (order.getDeleted() != null && order.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_NOT_FOUND);
        }
        assertOrderMember(order, userId);
        validateMeetup(meetupPlace, meetupTime);

        int changed = productOrderMapper.updateMeetup(orderId, meetupPlace.trim(), meetupTime);
        if (changed == 0) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_STATUS_ERROR, "订单当前状态不可安排约见");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rescheduleMeetup(Long orderId, Long userId, String meetupPlace, LocalDateTime meetupTime) {
        ServiceProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null || (order.getDeleted() != null && order.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_NOT_FOUND);
        }
        assertOrderMember(order, userId);
        validateMeetup(meetupPlace, meetupTime);

        int changed = productOrderMapper.rescheduleMeetup(orderId, meetupPlace.trim(), meetupTime);
        if (changed == 0) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_STATUS_ERROR, "改约次数已达上限或订单状态不可改约");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyMeetupCode(Long orderId, Long userId, String meetupCode) {
        ServiceProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null || (order.getDeleted() != null && order.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_NOT_FOUND);
        }
        assertOrderMember(order, userId);
        if (!StringUtils.hasText(meetupCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "见面码不能为空");
        }

        int changed = productOrderMapper.verifyMeetupCode(orderId, meetupCode.trim());
        if (changed == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "见面码错误或订单状态异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId, String cancelReason) {
        ServiceProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null || (order.getDeleted() != null && order.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_NOT_FOUND);
        }
        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限取消该订单");
        }
        if (!Integer.valueOf(0).equals(order.getStatus()) && !Integer.valueOf(1).equals(order.getStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_STATUS_ERROR);
        }

        int changed = productOrderMapper.cancelOrder(orderId, StringUtils.hasText(cancelReason) ? cancelReason.trim() : null);
        if (changed == 0) {
            throw new BusinessException(ResultCode.REPEAT_OPERATION, "订单状态已变更");
        }
        if (Integer.valueOf(1).equals(order.getStatus())) {
            productMapper.updateStatusWithExpected(order.getProductId(), 3, 1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long orderId, Long userId) {
        ServiceProductOrder order = productOrderMapper.selectById(orderId);
        if (order == null || (order.getDeleted() != null && order.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_NOT_FOUND);
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅买家可确认收货");
        }
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_STATUS_ERROR);
        }
        if (order.getMeetupTime() != null && !Integer.valueOf(1).equals(order.getMeetupVerified())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请先完成见面码核销");
        }

        int changed = productOrderMapper.completeOrder(orderId);
        if (changed == 0) {
            throw new BusinessException(ResultCode.REPEAT_OPERATION, "订单状态已变更");
        }
        productMapper.updateStatusWithExpected(order.getProductId(), 3, 2);
    }

    @Override
    public PageResult<ServiceProductOrder> getMyOrders(Long userId, String role, String keyword, Long current, Long size) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        String normalizedRole = "buyer".equalsIgnoreCase(role) ? "buyer" : ("seller".equalsIgnoreCase(role) ? "seller" : null);
        String searchKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        List<ServiceProductOrder> records = productOrderMapper.selectMyOrders(userId, normalizedRole, searchKeyword, offset, pageSize);
        fillOrderRelations(records);
        Long total = productOrderMapper.countMyOrders(userId, normalizedRole, searchKeyword);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public PageResult<ServiceProduct> getAdminProductList(Long current, Long size, Long categoryId, Integer status, Integer auditStatus, Integer tradeType, String keyword) {
        long pageNo = current == null || current < 1 ? 1 : current;
        long pageSize = size == null || size < 1 ? 10 : size;
        long offset = (pageNo - 1) * pageSize;
        log.info("商品管理查询: categoryId={}, status={}, auditStatus={}, tradeType={}, keyword={}, page={}, size={}",
                categoryId, status, auditStatus, tradeType, keyword, pageNo, pageSize);
        List<ServiceProduct> records = productMapper.selectAdminPage(categoryId, status, auditStatus, tradeType, keyword, offset, pageSize);
        fillRelations(records);
        Long total = productMapper.countAdminPage(categoryId, status, auditStatus, tradeType, keyword);
        log.info("商品管理查询结果: records={}, total={}", records.size(), total);
        return new PageResult<>(pageNo, pageSize, total == null ? 0L : total, records);
    }

    @Override
    public void auditProduct(Long id, Integer auditStatus) {
        ServiceProduct product = productMapper.selectById(id);
        if (product == null || (product.getDeleted() != null && product.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        int changed = productMapper.updateAuditStatus(id, auditStatus);
        if (changed == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或无权限操作");
        }
        searchSyncService.syncProduct(id);
        // 发送审核通知
        noticeService.sendServiceAuditNotice(product.getUserId(), "商品", product.getTitle(), auditStatus, product.getId(), 3);
    }

    private void fillRelations(List<ServiceProduct> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        for (ServiceProduct product : products) {
            SysUser seller = userMapper.selectById(product.getUserId());
            if (seller != null) {
                seller.setPassword(null);
            }
            product.setSeller(seller);
            product.setUser(seller);
            product.setImage(extractFirstImage(product.getImages()));
        }
    }

    private void fillOrderRelations(List<ServiceProductOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        for (ServiceProductOrder order : orders) {
            ServiceProduct product = productMapper.selectById(order.getProductId());
            if (product != null) {
                product.setImage(extractFirstImage(product.getImages()));
                order.setProduct(product);
            }
            SysUser buyer = userMapper.selectById(order.getBuyerId());
            if (buyer != null) {
                buyer.setPassword(null);
            }
            order.setBuyer(buyer);
            SysUser seller = userMapper.selectById(order.getSellerId());
            if (seller != null) {
                seller.setPassword(null);
            }
            order.setSeller(seller);
        }
    }

    private String extractFirstImage(String images) {
        if (!StringUtils.hasText(images)) {
            return "";
        }
        String value = images.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1).trim();
            if (!StringUtils.hasText(value)) {
                return "";
            }
            String first = value.split(",")[0].trim();
            return first.replace("\"", "").replace("'", "").trim();
        }
        if (value.contains(",")) {
            return value.split(",")[0].trim();
        }
        return value;
    }

    private void bindForumPost(ServiceProduct product) {
        ForumSection section = sectionMapper.selectByCode("FLEA_MARKET");
        if (section == null) {
            return;
        }

        ForumPost post = new ForumPost();
        post.setUserId(product.getUserId());
        post.setSectionId(section.getId());
        post.setTitle(product.getTitle());
        post.setContent(product.getDescription());
        post.setImages(product.getImages());
        post.setAuditStatus(product.getAuditStatus());
        post.setSourceType(1);
        post.setSourceId(product.getId());
        post.setIsAnonymous(0);
        forumPostMapper.insert(post);
        productMapper.updatePostId(product.getId(), post.getId());
        product.setPostId(post.getId());
    }

    private int mapAuditStatusToContentStatus(int auditStatus) {
        switch (auditStatus) {
            case 1: return 1;
            case 2: return 2;
            case 3: return 0;
            case 4: return 0;
            default: return 0;
        }
    }

    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "PO" + ts + random;
    }

    private String generateMeetupCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(code);
    }

    private void assertOrderMember(ServiceProductOrder order, Long userId) {
        boolean isBuyer = order.getBuyerId() != null && order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId() != null && order.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作该订单");
        }
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_ORDER_STATUS_ERROR, "仅进行中订单支持该操作");
        }
    }

    private void validateMeetup(String meetupPlace, LocalDateTime meetupTime) {
        if (!StringUtils.hasText(meetupPlace) || meetupTime == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "约见地点和时间不能为空");
        }
        if (meetupTime.isBefore(LocalDateTime.now().plusMinutes(10))) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "约见时间需晚于当前时间10分钟");
        }
    }
}
