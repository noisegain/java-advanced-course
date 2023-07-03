package info.kgeorgiy.ja.ponomarenko.bank.account;

/**
 * Remote account implementation.
 *
 * @author Ilya Ponomarenko
 */
public class RemoteAccount extends BaseAccount {
    /**
     * Creates remote account with specified identifier and amount 0.
     *
     * @param id account identifier
     */
    public RemoteAccount(final String id) {
        super(id, 0);
    }
}
