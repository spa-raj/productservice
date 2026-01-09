package com.vibevault.productservice.constants;

public enum ErrorCode {
    PRODUCT_NOT_FOUND,
    PRODUCT_CREATION_FAILED,
    EXTERNAL_API_ERROR,
    DATABASE_ERROR,
    PRODUCT_DELETION_FAILED,
    CATEGORY_NOT_FOUND,
    CATEGORY_CREATION_FAILED,
    CATEGORY_ALREADY_EXISTS,
    INVALID_TOKEN,
    ACCESS_DENIED;

    @Override
    public String toString() {
        return name();
    }
}
