package com.mavenrepository.maven.auth;

import io.helidon.http.Header;
import io.helidon.http.Status;

import java.util.Optional;

public class AuthException extends IllegalStateException {

    private final Header header;
    private final Status status;

    public AuthException(int aStatus, String aReason, Header aHeader) {
        super(aReason);
        status = Status.create(aStatus, aReason);
        header = aHeader;
    }

    public AuthException(int aStatus, String aReason) {
        this(aStatus, aReason, null);
    }


    public Status getStatus() {
        return status;
    }

    public Optional<Header> getHeader() {
        return Optional.ofNullable(header);
    }

    public String getBody() {
        return getMessage();
    }
}
