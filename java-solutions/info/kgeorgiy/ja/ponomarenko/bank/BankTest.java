package info.kgeorgiy.ja.ponomarenko.bank;

import info.kgeorgiy.ja.ponomarenko.bank.account.Account;
import info.kgeorgiy.ja.ponomarenko.bank.bank.Bank;
import info.kgeorgiy.ja.ponomarenko.bank.bank.RemoteBank;
import info.kgeorgiy.ja.ponomarenko.bank.person.LocalPerson;
import info.kgeorgiy.ja.ponomarenko.bank.person.Person;
import org.junit.jupiter.api.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.stream.IntStream;

class BankTest {

    public static final int PORT = 8448;
    private static Bank bank;

    @BeforeAll
    static void setUp() throws RemoteException, NotBoundException {
        final Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        registry.rebind("//localhost/bank", new RemoteBank(PORT));
        bank = (Bank) registry.lookup("//localhost/bank");
        UnicastRemoteObject.exportObject(bank, Registry.REGISTRY_PORT);
    }

    @AfterAll
    static void tearDown() throws RemoteException {
        UnicastRemoteObject.unexportObject(bank, true);
    }

    @Test
    @DisplayName("get incorrect account")
    void getIncorrectAccount() throws RemoteException {
        Assertions.assertNull(bank.getAccount("incorrect1"));
        Assertions.assertNull(bank.getAccount("incorrect2"));
    }

    @Test
    @DisplayName("get incorrect person")
    void getIncorrectPerson() throws RemoteException {
        Assertions.assertNull(bank.getPerson("incorrect1", Person.Type.LOCAL));
        Assertions.assertNull(bank.getPerson("incorrect2", Person.Type.REMOTE));
    }

    @Test
    @DisplayName("create accounts")
    void createAccounts() throws RemoteException {
        final String[] ACCOUNT_IDS = IntStream.rangeClosed(1, 100).mapToObj(Integer::toString).toArray(String[]::new);
        // create accounts
        for (String id : ACCOUNT_IDS) {
            Account account = bank.createAccount(id);
            Assertions.assertEquals(id, account.getId());
        }
        // check accounts
        for (String id : ACCOUNT_IDS) {
            Assertions.assertEquals(id, bank.getAccount(id).getId());
        }
    }

    @Test
    @DisplayName("create person")
    void createPerson() throws RemoteException {
        String firstName = "Georgiy";
        String lastName = "Korneev";
        String id = "228";
        Person person = bank.createPerson(firstName, lastName, id);
        Assertions.assertAll(
                () -> Assertions.assertEquals(firstName, person.getFirstName()),
                () -> Assertions.assertEquals(lastName, person.getLastName()),
                () -> Assertions.assertEquals(id, person.getId())
        );
    }

    @Test
    @DisplayName("create multiple persons with same id")
    void sameId() throws RemoteException {
        final String[] FIRST_NAMES = {"Ilya", "Maxim", "Taras", "Alexey", "Denis"};
        final String[] LAST_NAMES = {"Ponomarenko", "Trofimov", "Leontev", "Myasnikov", "Zhimoedov"};
        final String ID = "123123123";
        for (int i = 0; i < FIRST_NAMES.length; ++i) {
            bank.createPerson(FIRST_NAMES[i], LAST_NAMES[i], ID);
        }
        Person person = bank.getPerson(ID, Person.Type.REMOTE);
        Assertions.assertAll(
                () -> Assertions.assertEquals(FIRST_NAMES[0], person.getFirstName()),
                () -> Assertions.assertEquals(LAST_NAMES[0], person.getLastName()),
                () -> Assertions.assertEquals(ID, person.getId())
        );
    }

    @Test
    @DisplayName("create multiple persons with many accounts")
    void create() throws RemoteException {
        final String[] FIRST_NAMES = {"Ilya", "Maxim", "Taras", "Alexey", "Denis"};
        final String[] LAST_NAMES = {"Ponomarenko", "Trofimov", "Leontev", "Myasnikov", "Zhimoedov"};
        final String[] IDS = {"1234", "4321", "1111", "2222", "3333"};
        final String[] ACCOUNT_IDS = IntStream.rangeClosed(1, 100).mapToObj(Integer::toString).toArray(String[]::new);

        for (int i = 0; i < IDS.length; ++i) {
            bank.createPerson(FIRST_NAMES[i], LAST_NAMES[i], IDS[i]);
        }
        for (String id : IDS) {
            Person person = bank.getPerson(id, Person.Type.REMOTE);
            for (String accountId : ACCOUNT_IDS) {
                Account account = bank.createAccount(person.getId() + ":" + accountId);
                Assertions.assertEquals(person.getId() + ":" + accountId, account.getId());
            }
        }
    }

    @Test
    @DisplayName("create person and check balances with local and remote")
    void verifyLocalAndRemote() throws RemoteException {
        final String[] IDS = {"1313", "228", "1337"};
        final String FIRST_NAME = "Ilya";
        final String LAST_NAME = "Ponomarenko";
        final String ACCOUNT_ID = "13132281337";
        Person person = bank.createPerson(FIRST_NAME, LAST_NAME, ACCOUNT_ID);
        for (String id : IDS) {
            person.createAccount(id);
        }
        Assertions.assertEquals(person.getAccounts().size(), IDS.length);
        for (Account account : person.getAccounts()) {
            Assertions.assertEquals(0, account.getAmount());
        }
        LocalPerson local = (LocalPerson) bank.getPerson(ACCOUNT_ID, Person.Type.LOCAL);
        // set local balances
        for (String id : IDS) {
            Assertions.assertEquals(0, local.getAccount(id).getAmount());
            local.getAccount(id).setAmount(100);
            Assertions.assertEquals(100, local.getAccount(id).getAmount());
        }
        // check remote balances unchanged and set them
        for (String id : IDS) {
            Assertions.assertEquals(0, person.getAccount(id).getAmount());
            person.getAccount(id).setAmount(300);
            Assertions.assertEquals(300, person.getAccount(id).getAmount());
        }
        local = (LocalPerson) bank.getPerson(ACCOUNT_ID, Person.Type.LOCAL);
        // check new local has new balances
        for (String id : IDS) {
            Assertions.assertEquals(300, local.getAccount(id).getAmount());
        }
        Person remote = bank.getPerson(ACCOUNT_ID, Person.Type.REMOTE);
        // check remote has new balances and set new
        for (String id : IDS) {
            Assertions.assertEquals(300, remote.getAccount(id).getAmount());
            remote.getAccount(id).setAmount(500);
        }
        // verify old remote person has new balances
        for (String id : IDS) {
            Assertions.assertEquals(500, person.getAccount(id).getAmount());
        }
    }
}