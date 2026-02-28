package com.fy.erp.dto.report;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesFunnelItem {
    private String stageCode; // 阶段代码（与 DB 中 stage 字段一致）
    private String stageName; // 阶段显示名（中文）
    private Long count; // 线索数
    private BigDecimal amount; // 预期金额
    private BigDecimal conversionRate; // 转化率（相对上一阶段，百分比）
}
