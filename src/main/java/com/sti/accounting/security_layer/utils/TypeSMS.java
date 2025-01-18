package com.sti.accounting.security_layer.utils;

import lombok.Getter;

@Getter
public enum TypeSMS {
    WHATSAPP("whatsapp:"),
    SMS(""),
    GBM("gbm:"),
    RCS(""),
    FB("messenger:");

    private final String value;

    TypeSMS(String type) {
        this.value = type;
    }
}
