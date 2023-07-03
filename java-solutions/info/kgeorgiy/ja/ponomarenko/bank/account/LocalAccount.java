package info.kgeorgiy.ja.ponomarenko.bank.account;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Local account implementation.
 *
 * @author Ilya Ponomarenko
 */
public class LocalAccount extends BaseAccount implements Serializable {
    /**
     * Creates local account from given account.
     *
     * @param account account to copy
     * @throws RemoteException if remote error occurs
     */
    public LocalAccount(Account account) throws RemoteException {
        super(account.getId(), account.getAmount());
    }

    /**
     * Creates local account with specified identifier and amount 0.
     *
     * @param id account identifier
     */
    public LocalAccount(final String id) {
        super(id, 0);
    }
}
