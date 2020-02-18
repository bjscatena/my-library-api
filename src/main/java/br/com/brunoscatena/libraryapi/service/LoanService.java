package br.com.brunoscatena.libraryapi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.brunoscatena.libraryapi.api.dto.LoanFilterDTO;
import br.com.brunoscatena.libraryapi.model.entity.Loan;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> findById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO loan, Pageable pageRequest);

}
