package com.vibevault.productservice.constants;

public enum ErrorCode {
    PRODUCT_NOT_FOUND,
    PRODUCT_CREATION_FAILED,
    EXTERNAL_API_ERROR,
    DATABASE_ERROR,
    PRODUCT_DELETION_FAILED;

    @Override
    public String toString() {
        return name();
    }
}
