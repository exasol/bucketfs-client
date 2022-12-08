package com.exasol.bucketfs.client;

import java.io.*;

/**
 * This class asks user for password while hiding the typed characters.
 * <p>
 * In tests the password is read from stdin.
 * </p>
 */
// [impl->dsn~sub-command-requires-hidden-password~1]
public class PasswordReader {
    public static String readPassword() {
        final Console console = System.console();
        if (console != null) {
            return new String(console.readPassword("Write password for BucketFS: "));
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            return reader.readLine();
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
