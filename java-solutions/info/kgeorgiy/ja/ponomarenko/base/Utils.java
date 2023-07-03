package info.kgeorgiy.ja.ponomarenko.base;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility class
 *
 * @author Ponomarenko Ilya
 */
public class Utils {
    /**
     * Parses integer from args[index] or returns defaultValue if there is no such argument, or it is not a number
     *
     * @param args         array of arguments
     * @param index        index of argument to parse
     * @param defaultValue default value to return if there is no such argument, or it is not a number
     * @param name         name of argument to print in error message
     * @return parsed integer or defaultValue if there is no such argument, or it is not a number
     */
    public static int parseOrDefault(String[] args, int index, int defaultValue, String name) {
        try {
            return args.length > index ? Integer.parseInt(args[index]) : defaultValue;
        } catch (NumberFormatException e) {
            System.out.println("Error: " + name + " is not a number");
        }
        return defaultValue;
    }

    /**
     * Checks if there are correct number of arguments
     *
     * @param args      array of arguments
     * @param minLength minimum number of arguments
     * @param maxLength maximum number of arguments
     * @param usage     usage message to print in error message
     * @return true if there are correct number of arguments, false otherwise
     */
    public static boolean checkArgs(String[] args, int minLength, int maxLength, String usage) {
        if (args == null || args.length > maxLength || args.length < minLength) {
            System.err.println("Error: wrong arguments");
            System.err.println("Usage: " + usage);
            return false;
        }
        return true;
    }

    /**
     * Checks if there are correct number of arguments
     *
     * @param args   array of arguments
     * @param length number of arguments
     * @param usage  usage message to print in error message
     * @return true if there are correct number of arguments, false otherwise
     */
    public static boolean checkArgs(String[] args, int length, String usage) {
        return checkArgs(args, length, length, usage);
    }

    /**
     * Checks if arguments is numbers
     *
     * @param args    array of arguments
     * @param indexes indexes of arguments to check
     * @return true if arguments is numbers, false otherwise
     */
    public static boolean checkIntegers(String[] args, int... indexes) {
        return Arrays.stream(indexes).allMatch(index -> checkIsInteger(args[index]));
    }

    /**
     * Checks if value is number
     *
     * @param value value to check
     * @return true if value is number, false otherwise
     */
    public static boolean checkIsInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Error: " + value + " is not a number");
        }
        return false;
    }

    /**
     * Shuts down executorService
     *
     * @param executorService executorService to shut down
     * @param time            time to wait for termination
     * @param timeUnit        time unit for time
     */
    public static void shutdown(ExecutorService executorService, long time, TimeUnit timeUnit) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(time, timeUnit)) {
                System.err.println("Error while waiting for termination, shutting down now");
                executorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            executorService.shutdownNow();
        }
    }
}
