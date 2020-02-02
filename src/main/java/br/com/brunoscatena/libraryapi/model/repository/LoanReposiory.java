package br.com.brunoscatena.libraryapi.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.brunoscatena.libraryapi.model.entity.Loan;

public interface LoanReposiory extends JpaRepository<Loan, Long> {

    Boolean existsByBookIdAndReturned(Long bookId, Boolean returned);
}
