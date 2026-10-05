package com.medguide.common.exception;

import com.medguide.modules.user.domain.UserStatus;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an account cannot authenticate due to its lifecycle status (e.g. SUSPENDED or DEACTIVATED).
 */
public class AccountStatusException extends ApiException {

    public AccountStatusException(UserStatus status) {
        super(String.format("Account authentication failed: status is %s", status), HttpStatus.FORBIDDEN);
    }
}
