package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.profile.ProfileReader.CONFIG_FILE_PROPERTY;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Executes the BucketFsClient (BFSC) in integration tests.
 */
public class BFSC {

    private final String[] parameters;
    private Path configFile = Path.of("/non/existing/file");
    private String in = null;
    private String out = null;

    /**
     * Create the wrapper with the given parameters.
     *
     * @param parameters command line parameters
     * @return wrapper object
     */
    static BFSC create(final String... parameters) {
        return new BFSC(parameters);
    }

    private BFSC(final String[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Feed STDIN with a string.
     *
     * @param in string to be fed to STDIN
     * @return {@code this} for fluent programming
     */
    public BFSC feedStdIn(final String in) {
        this.in = in;
        return this;
    }

    /**
     * Catch STDOUT into string for verification in test
     *
     * @return {@code this} for fluent programming
     */
    public BFSC catchStdout() {
        this.out = "";
        return this;
    }

    BFSC withConfigFile(final Path path) {
        this.configFile = path;
        return this;
    }

    /**
     * Run the BucketFS client.
     *
     * @throws Exception
     */
    public void run() {
        System.setProperty(CONFIG_FILE_PROPERTY, this.configFile.toString());
        overridingStdIn(catchingStdOut(() -> BucketFsClient.main(this.parameters))).run();
    }

    private Runnable catchingStdOut(final Runnable runnable) {
        if (this.out != null) {
            return () -> catchStdOut(runnable);
        } else {
            return runnable;
        }
    }

    private Runnable overridingStdIn(final Runnable runnable) {
        if (this.in != null) {
            return () -> overrideStdIn(runnable);
        } else {
            return runnable;
        }
    }

    private void overrideStdIn(final Runnable runnable) {
        final InputStream previousStdIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(this.in.getBytes()));
            runnable.run();
        } finally {
            System.setIn(previousStdIn);
        }
    }

    private void catchStdOut(final Runnable runnable) {
        final PrintStream previous = System.out;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(out));
            runnable.run();
        } finally {
            System.setOut(previous);
            this.out = out.toString(StandardCharsets.UTF_8);
        }
    }

    public String getStdOut() {
        return this.out;
    }
}
