package info.kgeorgiy.ja.ponomarenko.bank.bank;

import info.kgeorgiy.ja.ponomarenko.bank.account.Account;
import info.kgeorgiy.ja.ponomarenko.bank.person.Person;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     *
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Creates a new {@link Person} with specified parameters.
     *
     * @param firstName person's first name
     * @param lastName person's last name
     * @param id person's id
     * @return created person
     */
    Person createPerson(String firstName, String lastName, String id) throws RemoteException;

    /**
     * Returns person by identifier.
     * @param id person's id
     * @param type person's type
     * @return person with specified identifier or {@code null} if such person does not exist.
     */
    Person getPerson(String id, Person.Type type) throws RemoteException;

    /**
     * Returns all accounts of specified person.
     *
     * @param person person
     * @return all accounts of specified person
     */
    Set<Account> getAccounts(Person person) throws RemoteException;
}
