package info.kgeorgiy.ja.ponomarenko.bank.person;

import info.kgeorgiy.ja.ponomarenko.bank.account.Account;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Person extends Remote {
    /**
     * Returns person's first name.
     */
    String getFirstName() throws RemoteException;

    /**
     * Returns person's last name.
     */
    String getLastName() throws RemoteException;

    /**
     * Returns person's id.
     */
    String getId() throws RemoteException;

    /**
     * Returns person's accounts.
     */
    Set<Account> getAccounts() throws RemoteException;

    /**
     * Returns person's account by subId.
     */
    Account getAccount(String subId) throws RemoteException;

    /**
     * Creates person's account by subId.
     */
    void createAccount(String subId) throws RemoteException;

    /**
     * Person's type.
     */
    enum Type {
        LOCAL,
        REMOTE
    }
}
