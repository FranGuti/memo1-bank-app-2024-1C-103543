package com.aninfo.service;

import com.aninfo.exceptions.AccountNotExistsException;
import com.aninfo.exceptions.DepositNegativeSumException;
import com.aninfo.exceptions.TransactionNotExistsException;
import com.aninfo.model.Account;
import com.aninfo.model.Transaction;
import com.aninfo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountService accountService;

    public Transaction createTransaction(Transaction transaction) {
        if(transaction.getAccount() == null || StringUtils.isEmpty(transaction.getAccount().getCbu()))
            throw new AccountNotExistsException("La transaccion ingresada no contiene cuenta");

        Optional<Account> account = accountService.findById(transaction.getAccount().getCbu());

        Long cbu = account.map(Account::getCbu).orElseThrow();
        if("DEPOSIT".equalsIgnoreCase(transaction.getType()))
            accountService.deposit(cbu, transaction.getAmount());
        else if("WITHDRAW".equalsIgnoreCase(transaction.getType()))
            accountService.withdraw(cbu, transaction.getAmount());
        else
            throw new TransactionNotExistsException("El tipo de transaccion no existe");
        return null;
    }

    public Collection<Transaction> getTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public void deleteById(Long id) {
        transactionRepository.deleteById(id);
    }

    @Transactional
    public Transaction withdraw(Account account, Double amount){
        Transaction trans = new Transaction();
        trans.setAccount(account);
        trans.setAmount(amount);
        trans.setType("WITHDRAW");
        return createTransaction(trans);
    }

    @Transactional
    public Transaction deposit(Account account, Double amount) {
        Transaction trans = new Transaction();
        trans.setAccount(account);
        trans.setAmount(amount);
        trans.setType("DEPOSIT");
        return createTransaction(trans);
    }

    @Transactional
    public Collection<Transaction> getByCbu(Long cbu){
        return transactionRepository.findAllByAccountCbu(cbu);
    }

    @Transactional
    public void rollback(Long id){
        Transaction transaction = transactionRepository.findTransactionById(id);
        if(transaction == null || transaction.getAccount() == null)
            throw new TransactionNotExistsException("La transaccion no existe");
        if(StringUtils.isEmpty(transaction.getAccount().getCbu()))
            throw new TransactionNotExistsException("El cbu de la cuenta es vacio");
        Optional<Account> account = accountService.findById(transaction.getAccount().getCbu());
        Long cbu = account.map(Account::getCbu).orElseThrow();
        if("DEPOSIT".equalsIgnoreCase(transaction.getType())){
            accountService.subtractFounds(cbu, transaction.getAmount());
        }else if("WITHDRAW".equalsIgnoreCase(transaction.getType())){
            accountService.addFounds(cbu, transaction.getAmount());
        }
        transactionRepository.deleteById(id);
    }
}
