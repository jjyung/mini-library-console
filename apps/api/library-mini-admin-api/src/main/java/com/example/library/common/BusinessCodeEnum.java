package com.example.library.common;

public enum BusinessCodeEnum {
    SUCCESS("00000"),
    CLIENT_ERROR("A0000"),
    SYSTEM_ERROR("B0000"),
    THIRD_PARTY_ERROR("C0000");

    private final String code;

    BusinessCodeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
