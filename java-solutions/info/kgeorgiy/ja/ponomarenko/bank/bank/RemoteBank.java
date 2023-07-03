package info.kgeorgiy.ja.ponomarenko.bank.bank;

import info.kgeorgiy.ja.ponomarenko.bank.account.Account;
import info.kgeorgiy.ja.ponomarenko.bank.account.LocalAccount;
import info.kgeorgiy.ja.ponomarenko.bank.account.RemoteAccount;
import info.kgeorgiy.ja.ponomarenko.bank.person.LocalPerson;
import info.kgeorgiy.ja.ponomarenko.bank.person.Person;
import info.kgeorgiy.ja.ponomarenko.bank.person.RemotePerson;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Remote bank implementation.
 *
 * @author Ilya Ponomarenko
 */
public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Set<Account>> personAccounts = new ConcurrentHashMap<>();

    /**
     * Creates a new {@link RemoteBank} instance.
     *
     * @param port port to export remote objects
     */
    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        System.out.println("Creating account " + id);
        final Account account = new RemoteAccount(id);
        final String[] split = id.split(":");
        if (split.length == 2) {
            final String personId = split[0];
            personAccounts.computeIfAbsent(personId, key -> ConcurrentHashMap.newKeySet()).add(account);
        }
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            System.out.println("Account " + id + " already exists");
            return getAccount(id);
        }
    }

    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    @Override
    public Person createPerson(String firstName, String lastName, String id) throws RemoteException {
        final Person person = new RemotePerson(firstName, lastName, id, this);
        if (persons.putIfAbsent(id, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            System.out.println("Person " + id + " already exists");
            return getPerson(id, Person.Type.REMOTE);
        }
    }

    @Override
    public Person getPerson(String id, Person.Type type) throws RemoteException {
        System.out.println("Retrieving person " + id);
        final Person person = persons.get(id);
        if (type == Person.Type.REMOTE || person == null) {
            return person;
        }
        final ConcurrentMap<String, LocalAccount> accounts = new ConcurrentHashMap<>();
        for (Account account : getAccounts(person)) {
            accounts.put(account.getId(), new LocalAccount(account));
        }
        return new LocalPerson(person, accounts);
    }

    @Override
    public Set<Account> getAccounts(Person person) throws RemoteException {
        System.out.println("Retrieving accounts for person " + person.getId());
        return personAccounts.get(person.getId());
    }
}
