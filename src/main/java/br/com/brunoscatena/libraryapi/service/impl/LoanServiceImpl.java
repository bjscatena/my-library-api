package br.com.brunoscatena.libraryapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.brunoscatena.libraryapi.exception.BusinessException;
import br.com.brunoscatena.libraryapi.model.entity.Loan;
import br.com.brunoscatena.libraryapi.model.repository.LoanReposiory;
import br.com.brunoscatena.libraryapi.service.LoanService;

@Service
public class LoanServiceImpl implements LoanService {

    private LoanReposiory loanRepository;

    @Autowired
    public LoanServiceImpl(LoanReposiory loanRepository) {
	super();
	this.loanRepository = loanRepository;
    }

    @Override
    public Loan save(Loan loan) {

	if (loanRepository.existsByBookIdAndReturned(loan.getBook().getId(), false)) {
	    throw new BusinessException("Book already loaned");
	}

	return loanRepository.save(loan);
    }

}
