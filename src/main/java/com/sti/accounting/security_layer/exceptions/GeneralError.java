package com.sti.accounting.security_layer.exceptions;

import lombok.Data;

@Data
public class GeneralError {
    private int code;
    private String message;
}
