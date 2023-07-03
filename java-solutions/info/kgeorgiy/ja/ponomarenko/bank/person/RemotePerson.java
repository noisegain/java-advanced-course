package info.kgeorgiy.ja.ponomarenko.bank.person;

import info.kgeorgiy.ja.ponomarenko.bank.account.Account;
import info.kgeorgiy.ja.ponomarenko.bank.bank.Bank;

import java.rmi.RemoteException;
import java.util.Set;

/**
 * Remote person implementation.
 *
 * @author Ilya Ponomarenko
 */
public class RemotePerson extends AbstractPerson {
    private final Bank bank;

    /** Creates remote person with specified first name, last name, identifier and bank. */
    public RemotePerson(String firstName, String lastName, String id, Bank bank) {
        super(firstName, lastName, id);
        this.bank = bank;
    }

    @Override
    public Set<Account> getAccounts() throws RemoteException {
        return bank.getAccounts(this);
    }

    @Override
    public Account getAccount(String subId) throws RemoteException {
        return bank.getAccount(id + ":" + subId);
    }

    @Override
    public void createAccount(String subId) throws RemoteException {
        bank.createAccount(id + ":" + subId);
    }
}
