package com.exasol.bucketfs.client;

import java.io.*;

import com.exasol.bucketfs.profile.Profile;

/**
 * This class asks user for password while hiding the typed characters.
 * <p>
 * In tests the password is read from stdin.
 * </p>
 */
// [impl->dsn~sub-command-requires-hidden-password~2]
public class PasswordReader {

    static final String PROMPT = "Write password for BucketFS: ";

    private PasswordReader() {
        // only static usage
    }

    /**
     * @param profile
     * @return password for write operations to bucket
     */
    public static String readPassword(final Profile profile) {
        if (profile.password() != null) {
            return profile.password();
        }
        final Console console = System.console();
        if (console != null) {
            return new String(console.readPassword(PROMPT));
        }
        return readPasswordFromSystemIn();
    }

    /**
     * Note: interactive usage on command line will print characters typed by user
     *
     * @return password from {@link System.in}
     */
    private static String readPasswordFromSystemIn() {
        System.out.print(PROMPT); // NOSONAR
        // Prompt needs to be shown on stdout, using a logger is inappropriate here
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            return reader.readLine();
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
