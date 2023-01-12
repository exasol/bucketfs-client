package com.exasol.bucketfs;

public class Lines {
    public static String lines(final String... lines) {
        return String.join(System.lineSeparator(), lines);
    }
}
