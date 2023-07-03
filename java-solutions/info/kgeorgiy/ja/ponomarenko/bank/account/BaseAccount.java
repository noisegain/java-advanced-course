package info.kgeorgiy.ja.ponomarenko.bank.account;

/**
 * Abstract account implementation.
 *
 * @author Ilya Ponomarenko
 */
abstract class BaseAccount implements Account {
    private final String id;
    private int amount;

    /**
     * Creates account with specified identifier and amount of money.
     * @param id account identifier
     * @param amount amount of money in the account
     */
    public BaseAccount(final String id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
