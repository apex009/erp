package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.dto.PurchaseOrderCreateRequest;
import com.fy.erp.dto.PurchaseOrderItemRequest;
import com.fy.erp.dto.PurchaseRequestCreateRequest;
import com.fy.erp.dto.PurchaseRequestItemRequest;
import com.fy.erp.entities.PurchaseRequest;
import com.fy.erp.entities.PurchaseRequestItem;
import com.fy.erp.exception.BizException;
import com.fy.erp.mapper.PurchaseRequestMapper;
import com.fy.erp.util.OrderNoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseRequestService extends ServiceImpl<PurchaseRequestMapper, PurchaseRequest> {
    private final PurchaseRequestItemService itemService;
    private final PurchaseOrderService orderService;

    public PurchaseRequestService(PurchaseRequestItemService itemService, PurchaseOrderService orderService) {
        this.itemService = itemService;
        this.orderService = orderService;
    }

    @Transactional
    public PurchaseRequest createRequest(PurchaseRequestCreateRequest request) {
        PurchaseRequest pr = new PurchaseRequest();
        pr.setRequestNo(OrderNoUtil.generate("PR"));
        pr.setSupplierId(request.getSupplierId());
        pr.setRemark(request.getRemark());
        pr.setStatus(0);
        pr.setTotalAmount(BigDecimal.ZERO);
        save(pr);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseRequestItemRequest itemReq : request.getItems()) {
            BigDecimal amount = itemReq.getPrice().multiply(itemReq.getQuantity());
            total = total.add(amount);

            PurchaseRequestItem item = new PurchaseRequestItem();
            item.setRequestId(pr.getId());
            item.setProductId(itemReq.getProductId());
            item.setWarehouseId(itemReq.getWarehouseId());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            item.setAmount(amount);
            itemService.save(item);
        }
        pr.setTotalAmount(total);
        updateById(pr);
        return pr;
    }

    @Transactional
    public PurchaseRequest approve(Long id) {
        PurchaseRequest pr = getById(id);
        if (pr == null) {
            throw new BizException(404, "request not found");
        }
        if (pr.getStatus() != null && pr.getStatus() != 0) {
            return pr;
        }
        pr.setStatus(1);
        updateById(pr);
        return pr;
    }

    @Transactional
    public PurchaseRequest reject(Long id) {
        PurchaseRequest pr = getById(id);
        if (pr == null) {
            throw new BizException(404, "request not found");
        }
        if (pr.getStatus() != null && pr.getStatus() == 3) {
            return pr;
        }
        pr.setStatus(3);
        updateById(pr);
        return pr;
    }

    @Transactional
    public PurchaseRequest toOrder(Long id) {
        PurchaseRequest pr = getById(id);
        if (pr == null) {
            throw new BizException(404, "request not found");
        }
        if (pr.getStatus() != null && pr.getStatus() == 2) {
            return pr;
        }
        if (pr.getStatus() != null && pr.getStatus() == 3) {
            throw new BizException(400, "request rejected");
        }
        List<PurchaseRequestItem> items = itemService.lambdaQuery().eq(PurchaseRequestItem::getRequestId, id).list();
        if (items.isEmpty()) {
            throw new BizException(400, "request items empty");
        }
        List<PurchaseOrderItemRequest> orderItems = new ArrayList<>();
        for (PurchaseRequestItem item : items) {
            PurchaseOrderItemRequest o = new PurchaseOrderItemRequest();
            o.setProductId(item.getProductId());
            o.setWarehouseId(item.getWarehouseId());
            o.setQuantity(item.getQuantity());
            o.setPrice(item.getPrice());
            orderItems.add(o);
        }
        PurchaseOrderCreateRequest orderRequest = new PurchaseOrderCreateRequest();
        orderRequest.setSupplierId(pr.getSupplierId());
        orderRequest.setRemark("由采购申请生成: " + pr.getRequestNo());
        orderRequest.setItems(orderItems);
        var order = orderService.createOrder(orderRequest);
        pr.setStatus(2);
        pr.setOrderId(order.getId());
        updateById(pr);
        return pr;
    }
}
