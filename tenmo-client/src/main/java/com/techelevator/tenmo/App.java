package com.techelevator.tenmo;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                approvalMenu();
            } else if (menuSelection == 4) {
                accountService.getAllAccountsAndUsernames(currentUser); // Display all users and accounts
                sendBucks(); // Here we can change balances for both accounts
                // In sendBucks(), I implemented POST the 'Send transfer' into transfer DB (transfer_status_id = 2, 'Approved') because it's sending
            } else if (menuSelection == 5) {
                accountService.getAllAccountsAndUsernames(currentUser); // Display all users and accounts
                requestBucks();
                // In requestBucks(), I implemented POST the 'Send transfer' into transfer DB (transfer_status_id = 1, 'Pending') because it's request
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void approvalMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            viewPendingRequests();
            consoleService.printApprovalRejection();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleApprovalRequest();
            } else if (menuSelection == 2) {
                handleRejectRequest();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
        System.out.println(accountService.getBalance(currentUser));
	}

	private void viewTransferHistory() {
        transferService.getTransferHistory(currentUser);
	}

	private void viewPendingRequests() { transferService.getPendingRequests(currentUser); }

    private void handleApprovalRequest() {
        // Below will change the transfer from pending to approved, and return true.
        // If true, then will execute sendBucks method to change balances for both accounts
        // For requester, requester's current balance + amount to be transferred
        // For sender, sender's current balance - amount to be transferred
        int recipientTransferId = consoleService.promptForInt("Please input transfer ID: ");
        // For approval :
        // currentUser = money receiver,
        if (transferService.approveRequest(recipientTransferId, currentUser)) {
            // Get the transfer by using the transfer ID
            Transfer transfer = transferService.getTransferByTransferId(currentUser, recipientTransferId);
            // Update balances for both current user(current balance - amount) and the recipient(current balance + amount)
            // 1. Update account with new balance for current user (current balance - amount)
            Account updatedAccountForCurrentUser = accountService.getAccountByUserId(currentUser);
            updatedAccountForCurrentUser.setBalance(updatedAccountForCurrentUser.getBalance().subtract(transfer.getAmount()));
            // 2. Update account with new balance for recipient user (current balance + amount)
            Account updatedAccountForTargetUser = accountService.getAccountByAccountId(currentUser, transfer.getAccountTo());
            updatedAccountForTargetUser.setBalance(updatedAccountForTargetUser.getBalance().add(transfer.getAmount()));
            // 3. Update balances for both users
            accountService.updateAccountBucks(updatedAccountForCurrentUser, currentUser, updatedAccountForCurrentUser.getAccount_id());
            accountService.updateAccountBucks(updatedAccountForTargetUser, currentUser, transfer.getAccountTo());
        }
    }

    private void handleRejectRequest() { transferService.rejectRequest(currentUser); }

    // Request : currentUser = money receiver, accountFromId = money sender
    private void requestBucks() {
        int accountFromId = consoleService.promptForInt("Please choose recipient's account ID you are requesting money from: ");
        BigDecimal amount = consoleService.promptForBigDecimal("Please input amount in two decimal: ");

        if (accountFromId == accountService.getAccountByUserId(currentUser).getAccount_id()) {
            System.out.println("Error: You cannot request money from yourself.");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Error: Amount must be greater than zero.");
            return;
        }

        // POST into Transfer table, need to get account object by user ID
        // transfer_status_id & transfer_type_id = 1 for 'Pending' and 'Request'
        transferService.postTransfer(1, accountFromId, amount, currentUser, accountService.getAccountByUserId(currentUser));
    }

    //TODO
	private void sendBucks() {
        while (true) {
            int accountToId = consoleService.promptForInt("Please choose recipient's account ID: ");
            BigDecimal amount = consoleService.promptForBigDecimal("Please input amount with 2 decimal places (examples: 10.50, 20, 19.69): ");

            // checking if user is sending bucks to themselves
            if (accountToId == accountService.getAccountByUserId(currentUser).getAccount_id()) {
                System.out.println("Error: You cannot send money to yourself.");
               return;
            }

            //checking if the user is sending 0 or negative amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Error: Amount must be greater than zero.");
                return;
            }

            // Checking if amount is less than/equal to the current balance of the current user
            if (amount.compareTo(accountService.getBalance(currentUser)) <= 0) {
                // POST into Transfer table
                // transfer_status_id & transfer_type_id = 2 for 'Approved' and 'Send'
                transferService.postTransfer(2, accountToId, amount, currentUser, accountService.getAccountByUserId(currentUser));
                // Change balances for currentUser and receiver in account DB
                // 1. Update account with new balance for current user (current balance - amount)
                Account updatedAccountForCurrentUser = accountService.getAccountByUserId(currentUser);
                updatedAccountForCurrentUser.setBalance(updatedAccountForCurrentUser.getBalance().subtract(amount));
                // 2. Update account with new balance for recipient user (current balance + amount)
                Account updatedAccountForTargetUser = accountService.getAccountByAccountId(currentUser, accountToId);
                updatedAccountForTargetUser.setBalance(updatedAccountForTargetUser.getBalance().add(amount));
                // 3. Update balances for both users
                accountService.updateAccountBucks(updatedAccountForCurrentUser, currentUser, updatedAccountForCurrentUser.getAccount_id());
                accountService.updateAccountBucks(updatedAccountForTargetUser, currentUser, accountToId);
                break;
            } else {
                System.out.println("Not enough TE Bucks in account!");
            }
        }
	}
}