package br.com.brunoscatena.libraryapi.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.brunoscatena.libraryapi.api.dto.LoanFilterDTO;
import br.com.brunoscatena.libraryapi.exception.BusinessException;
import br.com.brunoscatena.libraryapi.model.entity.Loan;
import br.com.brunoscatena.libraryapi.model.repository.LoanRepository;
import br.com.brunoscatena.libraryapi.service.LoanService;

@Service
public class LoanServiceImpl implements LoanService {

    private LoanRepository loanRepository;

    @Autowired
    public LoanServiceImpl(LoanRepository loanRepository) {
	super();
	this.loanRepository = loanRepository;
    }

    @Override
    public Loan save(Loan loan) {

	if (loanRepository.existsByBookIdAndNotReturned(loan.getBook())) {
	    throw new BusinessException("Book already loaned");
	}

	return loanRepository.save(loan);
    }

    @Override
    public Optional<Loan> findById(Long id) {
	return loanRepository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
	return loanRepository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO dto, Pageable pageRequest) {
	return loanRepository.findByBookIsbnOrCustomer(dto.getIsbn(), dto.getCustomer(),
		pageRequest);
    }

}
