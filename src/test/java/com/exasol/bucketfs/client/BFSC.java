package com.exasol.bucketfs.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Wrapper for executing the BucketFsClient (BFSC) in integration tests.
 */
public class BFSC {
    private final String[] parameters;
    private String in = null;

    /**
     * Create the wrapper with the given parameters.
     *
     * @param parameters command line parameters
     * @return wrapper object
     */
    static BFSC create(final String... parameters) {
        return new BFSC(parameters);
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

    private BFSC(final String[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Run the BucketFS client.
     */
    public void run() {
        if (isStdInOverridden()) {
            runWithOverriddenStdIn();
        } else {
            BucketFsClient.main(this.parameters);
        }
    }

    private boolean isStdInOverridden() {
        return this.in != null;
    }

    private void runWithOverriddenStdIn() {
        final InputStream previousStdIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream(this.in.getBytes()));
            BucketFsClient.main(this.parameters);
        } finally {
            System.setIn(previousStdIn);
        }
    }
}