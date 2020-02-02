package br.com.brunoscatena.libraryapi.api.exceptions;

import br.com.brunoscatena.libraryapi.exception.BusinessException;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiError {

    List<String> errors;

    public ApiError(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach( error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiError(BusinessException ex) {
        this.errors = Arrays.asList(ex.getMessage());
    }

    public List<String> getErrors() {
        return errors;
    }
}
