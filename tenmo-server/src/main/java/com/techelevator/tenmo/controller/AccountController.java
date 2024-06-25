package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.exception.AccountExceptions;
import com.techelevator.tenmo.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/user")
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private AccountDao accountDao;

    public AccountController(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    // Get all accounts
    @RequestMapping(path = "/all/account", method = RequestMethod.GET)
    @PreAuthorize("hasRole('USER')")
    public List<Account> list() {

        try {
            return accountDao.getAccounts();
        } catch (AccountExceptions.AccountListNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @RequestMapping(path = "/{id}/account", method = RequestMethod.GET)
    public Account get(@PathVariable int id) {
        try {
            return accountDao.getAccountByUserId(id);
        } catch (AccountExceptions.AccountNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Get one account by account id
    @RequestMapping(path = "/{id}/account/{accountId}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('USER')")
    public Account getAccountByAccountId(@PathVariable int id, @PathVariable int accountId) {
        try {
            return accountDao.getAccountObjByAccountId(id, accountId);
        } catch(AccountExceptions.AccountNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Update balance
    @RequestMapping(path = "/{id}/account/{accountId}", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('USER')")
    public Account updateAccountAddBalance(@PathVariable int id, @PathVariable int accountId, @RequestBody Account account) {
        account.setAccount_id(accountId);
        try {
            return accountDao.updateAccountBalance(id, accountId, account);
        } catch(AccountExceptions.AccountUpdateException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

}



