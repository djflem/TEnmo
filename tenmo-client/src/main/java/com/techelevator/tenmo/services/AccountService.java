package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;

public class AccountService {

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public AccountService(String url) {
        this.baseUrl = url;
    }

    public Account[] getAllAccounts(AuthenticatedUser currentUser) {
        Account[] accounts = null;
        try {
            ResponseEntity<Account[]> response =
                    restTemplate.exchange(baseUrl + "user/all/account", HttpMethod.GET, makeAuthEntity(currentUser), Account[].class);
            accounts = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log("Error retrieving accounts: " + e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log("No account found: " + e.getMessage());
        }
        return accounts;
    }

    public Account[] getAllAccountsAndUsernames(AuthenticatedUser currentUser) {
        Account[] accounts = null;
        try {
            ResponseEntity<Account[]> response =
                    restTemplate.exchange(baseUrl + "user/all/account", HttpMethod.GET, makeAuthEntity(currentUser), Account[].class);
            accounts = response.getBody();
            System.out.println("-------------------------------------");
            System.out.println("         Accounts in Database        ");
            System.out.println("-------------------------------------");
            for (Account account : accounts) {
                System.out.println("Username: " + getUserNameByAccountId(currentUser, account.getAccount_id()) + " || Account Id: " + account.getAccount_id());
            }
            System.out.println("-------------------------------------");
        } catch (RestClientResponseException e) {
            BasicLogger.log("Error retrieving accounts: " + e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log("No account found: " + e.getMessage());
        }
        return accounts;
    }

    public BigDecimal getBalance(AuthenticatedUser currentUser) {
        BigDecimal balance = null;
        try {
            ResponseEntity<Account> response =
                    restTemplate.exchange(baseUrl + "user/{id}/account", HttpMethod.GET, makeAuthEntity(currentUser), Account.class, currentUser.getUser().getId());
            balance = Objects.requireNonNull(response.getBody()).getBalance();
        } catch (RestClientResponseException e) {
            BasicLogger.log("Error retrieving balance: " + e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log("No account found: " + e.getMessage());
        }
        return balance;
    }

    public Account getAccountByUserId(AuthenticatedUser currentUser) {
        Account account = null;
        try {
            ResponseEntity<Account> response = restTemplate.exchange(baseUrl + "user/{id}/account", HttpMethod.GET, makeAuthEntity(currentUser), Account.class, currentUser.getUser().getId());
            account = response.getBody();
        } catch (RestClientResponseException e) {
            BasicLogger.log("Error retrieving account associated with the user's ID" + " : " + e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log("No account found: " + e.getMessage());
        }
        return account;
    }

    public Account getAccountByAccountId(AuthenticatedUser currentUser, int accountId) {
        Account[] accounts = getAllAccounts(currentUser);
        Account newAccount = null;
        for (Account account : accounts) {
            if (account.getAccount_id() == accountId) {
                newAccount = account;
                break;
            }
        }
        return newAccount;
    }

    public void updateAccountBucks(Account updatedAccount, AuthenticatedUser currentUser, int accountId) {
        HttpEntity<Account> entity = makeAccountEntity(updatedAccount, currentUser);
        try {
            restTemplate.exchange(baseUrl + "user/{id}/account/{accountId}", HttpMethod.PUT,
                    entity, Account.class, currentUser.getUser().getId(), accountId);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log("Unable to update balance: " + e.getMessage());
        }
    }

    //
    // Helper methods
    //

    private String getUserNameByAccountId(AuthenticatedUser currentUser, int accountId) {
        String userName = null;
        int userId = 0;
        try {
            ResponseEntity<Account[]> responseAccount = restTemplate.exchange(baseUrl + "user/all/account", HttpMethod.GET, makeAuthEntity(currentUser), Account[].class);
            Account[] accounts = responseAccount.getBody();
            for (Account account : accounts) {
                if (account.getAccount_id() == accountId) {
                    userId = account.getUser_id();
                }
            }
            ResponseEntity<User> responseUser = restTemplate.exchange(baseUrl + "user/userId/{id}", HttpMethod.GET, makeAuthEntity(currentUser), User.class, userId);
            User user = responseUser.getBody();
            userName = user.getUsername();
        } catch (RestClientResponseException e) {
            BasicLogger.log("Error retrieving pending Account/User record: " + e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log("No account/username found!" + e.getMessage());
        }
        return userName;
    }

    private HttpEntity<Account> makeAccountEntity(Account account, AuthenticatedUser currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(account, headers);
    }

    private HttpEntity<Account> makeAuthEntity(AuthenticatedUser currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(headers);
    }
}
