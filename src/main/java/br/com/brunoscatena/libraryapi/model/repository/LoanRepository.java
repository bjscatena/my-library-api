package br.com.brunoscatena.libraryapi.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.brunoscatena.libraryapi.model.entity.Book;
import br.com.brunoscatena.libraryapi.model.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // @formatter:off
    @Query(value =  "SELECT "
	    	+        "CASE "
	    	+            "WHEN ( COUNT(l.id) > 0 ) THEN true "
	    	+ 	     "ELSE false "
	    	+        "END "	    	
	    	+    "FROM Loan l "
	    	+    "WHERE l.book = :book "
	    	+    "AND ( l.returned is null or l.returned is false )")
    boolean existsByBookIdAndNotReturned(@Param("book") Book book);
    // @formatter:on

    Page<Loan> findByBookIsbnOrCustomer(String isbn, String customer, Pageable pageRequest);
}
