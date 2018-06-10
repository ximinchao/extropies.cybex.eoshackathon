package com.extropies.www.eoshackathon.BleUtil;

import java.io.Serializable;

/**
 * Created by inst on 18-6-9.
 */

public class ExBleException implements Serializable {
    private static final long serialVersionUID = 8004414918500865564L;

    public static final int ERROR_CODE_TIMEOUT = 100;
    public static final int ERROR_CODE_GATT = 101;
    public static final int ERROR_CODE_OTHER = 102;

    private int code;
    private String description;

    public ExBleException(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public ExBleException setCode(int code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ExBleException setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "BleException { " +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
