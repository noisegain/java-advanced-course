package info.kgeorgiy.ja.ponomarenko.bank.person;

import java.rmi.RemoteException;

/**
 * Abstract person implementation.
 *
 * @author Ilya Ponomarenko
 */
abstract class AbstractPerson implements Person {
    protected final String firstName;
    protected final String lastName;
    protected final String id;

    protected AbstractPerson(String firstName, String lastName, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }

    @Override
    public String getFirstName() throws RemoteException {
        return firstName;
    }

    @Override
    public String getLastName() throws RemoteException {
        return lastName;
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }
}
