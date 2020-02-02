package br.com.brunoscatena.libraryapi.exception;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 5602877145739497363L;

    public BusinessException(String message) {
	super(message);
    }
}
