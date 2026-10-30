package com.library.exception;

public class AccountRestrictedException extends ApiException {

    public AccountRestrictedException(String message) {
        super(message);
    }
}

