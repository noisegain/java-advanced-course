package info.kgeorgiy.ja.ponomarenko.walk;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public class Walk {
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Should be 2 args provided");
            return;
        }
        if (args[0] == null || args[1] == null) {
            System.err.println("Args should not be null");
            return;
        }
        createDirsForOutputFile(args[1]);

        try (
                BufferedWriter writer = Files.newBufferedWriter(Path.of(args[1]))
        ) {
            try (
                    Stream<String> lines = Files.lines(Path.of(args[0]))
            ) {
                lines.forEach(line -> {
                    String res = calcHash(new File(line));
                    try {
                        writer.write(res + " " + line);
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println("Error while writing in file: " + e.getMessage());
                    }
                });
            } catch (IOException | UncheckedIOException e) {
                System.err.println("Error while reading file: " + args[0]);
                System.err.println(e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Error while reading file: " + args[0]);
            System.err.println(e.getMessage());
        } catch (InvalidPathException e) {
            System.err.println("Invalid path: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security exception: " + e.getMessage());
        }
    }

    private static void createDirsForOutputFile(String arg) {
        try {
            Path pathToFile = Path.of(arg);
            Path parent = pathToFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            System.err.println("Error while creating dirs for: " + arg);
            System.err.println(e.getMessage());
        } catch (InvalidPathException e) {
            System.err.println("Invalid path: " + arg);
            System.err.println(e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security exception: " + e.getMessage());
        }
    }

    private static String calcHash(File file) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[1 << 16];
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (int count; (count = bis.read(buffer)) >= 0; ) {
                digest.update(buffer, 0, count);
            }
            return String.format("%064x", new BigInteger(1, digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No such algorithm: SHA-256");
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + file.getName());
        } catch (IOException e) {
            System.err.println("Error while reading file: " + file.getName());
        } catch (SecurityException e) {
            System.err.println("Security exception: " + e.getMessage());
        }
        return "0".repeat(64);
    }
}
