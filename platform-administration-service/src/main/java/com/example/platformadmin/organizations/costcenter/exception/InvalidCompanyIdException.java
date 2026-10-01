package com.example.platformadmin.organizations.costcenter.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidCompanyIdException extends AppException {

    public InvalidCompanyIdException(Long companyId) {
        super("Invalid company ID: " + companyId, HttpStatus.BAD_REQUEST);
    }
}