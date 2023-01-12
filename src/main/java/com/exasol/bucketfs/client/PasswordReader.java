package com.exasol.bucketfs.client;

import java.util.function.Function;

import com.exasol.bucketfs.profile.Profile;

/**
 * This class asks user for a password while hiding the typed characters.
 * <p>
 * In tests the password is read from stdin.
 * </p>
 */
// [impl->dsn~sub-command-requires-hidden-password~2]
public class PasswordReader {

    interface ConsoleReader {
        String readPassword(final String prompt);
    }

    static ConsoleReader defaultConsoleReader() {
        return new ConsoleReaderWithFallbackToStdIn();
    }

    /**
     * @param isRequired    {@code true} if the read password is required
     * @param consoleReader used to read password from interactive console, can be mocked in tests
     * @return a new instance of {@see PasswordReader} for read operations
     */
    public static PasswordReader forReading(final boolean isRequired, final ConsoleReader consoleReader) {
        return new PasswordReader("reading from", Profile::getReadPassword, isRequired, consoleReader);
    }

    /**
     * @param consoleReader used to read password from interactive console, can be mocked in tests
     * @return a new instance of {@see PasswordReader} for write operations
     */
    public static PasswordReader forWriting(final ConsoleReader consoleReader) {
        return new PasswordReader("writing to", Profile::getWritePassword, true, consoleReader);
    }

    private final String qualifier;
    private final Function<Profile, String> getter;
    private final boolean readInteractively;
    private final ConsoleReader consoleReader;

    PasswordReader(final String qualifier, final Function<Profile, String> getter, final boolean readInteractively) {
        this(qualifier, getter, readInteractively, defaultConsoleReader());
    }

    PasswordReader(final String qualifier, final Function<Profile, String> getter, final boolean readInteractively,
            final ConsoleReader consoleReader) {
        this.qualifier = qualifier;
        this.getter = getter;
        this.readInteractively = readInteractively;
        this.consoleReader = consoleReader;
    }

    /**
     * Get the password for read or write operations either from profile or read it interactively hiding the typed
     * characters.
     *
     * @param profile
     * @return password for read or write operations to bucket
     */
    public String readPassword(final Profile profile) {
        final String passwordFromProfile = this.getter.apply(profile);
        if (passwordFromProfile != null) {
            return passwordFromProfile;
        }
        return this.readInteractively //
                ? this.consoleReader.readPassword(prompt(this.qualifier))
                : "";
    }

    static String prompt(final String qualifier) {
        return "Password for " + qualifier + " BucketFS: ";
    }
}
