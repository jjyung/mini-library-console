package com.example.library.dto;

public class ErrorDetailDTO {
    private String field;
    private String reason;

    public ErrorDetailDTO() { }

    public ErrorDetailDTO(String field, String reason) {
        this.field = field;
        this.reason = reason;
    }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
