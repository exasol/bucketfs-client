package com.exasol.bucketfs.client;

import java.io.*;

import com.exasol.bucketfs.client.PasswordReader.ConsoleReader;

public class ConsoleReaderWithFallbackToStdIn implements ConsoleReader {

    @Override
    public String readPassword(final String prompt) {
        final Console console = System.console();
        return console != null //
                ? new String(console.readPassword(prompt))
                : readPasswordFromSystemIn(prompt);
    }

    /**
     * Note: interactive usage on command line will print characters typed by user
     *
     * @return password from {@link System.in}
     */
    private String readPasswordFromSystemIn(final String prompt) {
        showPrompt(prompt);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            return reader.readLine();
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    // Suppress sonar warning S106 since prompt needs to be shown on stdout and using a logger is inappropriate here.
    @SuppressWarnings("java:S106")
    private void showPrompt(final String prompt) {
        System.out.print(prompt);
    }
}
