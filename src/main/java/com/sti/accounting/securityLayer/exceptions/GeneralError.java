package com.sti.accounting.securityLayer.exceptions;

import lombok.Data;

@Data
public class GeneralError {
    private int code;
    private String message;
}
