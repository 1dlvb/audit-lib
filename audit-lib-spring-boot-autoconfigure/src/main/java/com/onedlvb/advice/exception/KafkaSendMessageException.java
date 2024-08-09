package com.onedlvb.advice.exception;

public class KafkaSendMessageException extends Exception {

    public KafkaSendMessageException(String errorMessage) {
        super(errorMessage);
    }

}
