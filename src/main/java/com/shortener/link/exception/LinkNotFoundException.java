package com.shortener.link.exception;

public class LinkNotFoundException extends RuntimeException {

    public LinkNotFoundException(String code) {
        super("No existe un link con el código '" + code + "'");
    }
}
