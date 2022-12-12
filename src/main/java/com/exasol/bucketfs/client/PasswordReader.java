package com.exasol.bucketfs.client;

import java.io.*;

import com.exasol.bucketfs.env.EnvironmentVariables;

/**
 * This class asks user for password while hiding the typed characters.
 * <p>
 * In tests the password is read from stdin.
 * </p>
 */
// [impl->dsn~sub-command-requires-hidden-password~2]
public class PasswordReader {

    static final String PROMPT = "Write password for BucketFS: ";

    /**
     * @param env
     * @return password for write operations to bucket
     */
    public static String readPassword(final EnvironmentVariables env) {
        if (env.password() != null) {
            return env.password();
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
        System.out.print(PROMPT);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            return reader.readLine();
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
