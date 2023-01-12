package com.exasol.bucketfs;

/**
 * Evaluates a list of parameters returning the first "defined" value.
 */
public class Fallback {

    private Fallback() {
        // only static usage
    }

    /**
     * @param <T>        type of value
     * @param undefined  ignore candidates with this value
     * @param candidates list of candidates
     * @return first candidate with value not undefined
     */
    @SafeVarargs
    public static <T> T of(final T undefined, final T... candidates) {
        for (final T c : candidates) {
            if (c != undefined) {
                return c;
            }
        }
        return undefined;
    }
}
