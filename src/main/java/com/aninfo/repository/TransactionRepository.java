package com.aninfo.repository;

import com.aninfo.model.Account;
import com.aninfo.model.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

import java.util.List;

@RepositoryRestController
public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    Transaction findTransactionById(Long id);
    List<Transaction> findAllByAccountCbu(Long cbu);

    @Override
    List<Transaction> findAll();
}
