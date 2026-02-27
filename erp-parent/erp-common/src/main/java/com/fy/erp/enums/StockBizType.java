package com.fy.erp.enums;

import java.util.HashMap;
import java.util.Map;

public enum StockBizType {
    PURCHASE(1, "采购"),
    SALE(2, "销售"),
    TRANSFER(3, "调拨"),
    STOCK_CHECK(4, "盘点");

    private final int code;
    private final String label;

    StockBizType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<Integer, String> LABEL_MAP = new HashMap<>();

    static {
        for (StockBizType type : values()) {
            LABEL_MAP.put(type.code, type.label);
        }
    }

    public static String labelOf(Integer code) {
        if (code == null) {
            return "";
        }
        return LABEL_MAP.getOrDefault(code, "");
    }
}
