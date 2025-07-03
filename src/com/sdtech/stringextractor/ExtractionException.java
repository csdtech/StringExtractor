package com.sdtech.stringextractor;

public class ExtractionException extends RuntimeException{
    public ExtractionException(Throwable cause){
        super("Unable to extract strings.",cause);
    }
}
