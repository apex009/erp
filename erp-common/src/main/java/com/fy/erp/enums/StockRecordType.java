package com.fy.erp.enums;

import java.util.HashMap;
import java.util.Map;

public enum StockRecordType {
    IN(1, "入库"),
    OUT(2, "出库");

    private final int code;
    private final String label;

    StockRecordType(int code, String label) {
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
        for (StockRecordType type : values()) {
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
