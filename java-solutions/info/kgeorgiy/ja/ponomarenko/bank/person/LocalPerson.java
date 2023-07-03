package info.kgeorgiy.ja.ponomarenko.bank.person;

import info.kgeorgiy.ja.ponomarenko.bank.account.Account;
import info.kgeorgiy.ja.ponomarenko.bank.account.LocalAccount;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Local person implementation.
 *
 * @author Ilya Ponomarenko
 */
public class LocalPerson extends AbstractPerson implements Serializable {
    private final ConcurrentMap<String, LocalAccount> accounts;

    /**
     * Creates local person from given person.
     *
     * @param person person to copy
     * @param accounts accounts of the person
     */
    public LocalPerson(Person person, ConcurrentMap<String, LocalAccount> accounts) throws RemoteException {
        super(person.getFirstName(), person.getLastName(), person.getId());
        this.accounts = accounts;
    }

    @Override
    public Set<Account> getAccounts() {
        return Set.copyOf(accounts.values());
    }

    @Override
    public Account getAccount(String subId) {
        return accounts.get(id + ":" + subId);
    }

    @Override
    public void createAccount(String subId) throws RemoteException {
        accounts.put(id + ":" + subId, new LocalAccount(id + ":" + subId));
    }
}
