package com.exasol.bucketfs.client;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.exasol.bucketfs.env.EnvironmentVariables;

/**
 * Executes the BucketFsClient (BFSC) in integration tests.
 */
public class BFSC {

    static Map<String, String> defaultEnv(final Map<String, String> additional) {
        final Map<String, String> result = new HashMap<>();
        result.put(EnvironmentVariables.BUCKET, "");
        result.putAll(additional);
        return result;
    }

    private final String[] parameters;
    private Map<String, String> env = defaultEnv(Map.of());
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

    BFSC withEnv(final Map<String, String> env) {
        this.env = env;
        return this;
    }

    /**
     * Run the BucketFS client.
     *
     * @throws Exception
     */
    public void run() {
        final Map<String, String> envBefore = System.getenv();
        try {
            setEnv(this.env);
            overridingStdIn(catchingStdOut(() -> BucketFsClient.main(this.parameters))).run();
        } finally {
            setEnv(envBefore);
        }
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

    // https://stackoverflow.com/questions/318239
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void setEnv(final Map<String, String> newenv) {
        try {
            try {
                final Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
                final Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
                theEnvironmentField.setAccessible(true);
                final Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
                env.putAll(newenv);
                final Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                        .getDeclaredField("theCaseInsensitiveEnvironment");
                theCaseInsensitiveEnvironmentField.setAccessible(true);
                final Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
                cienv.putAll(newenv);
            } catch (final NoSuchFieldException e) {
                final Class[] classes = Collections.class.getDeclaredClasses();
                final Map<String, String> env = System.getenv();
                for (final Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        final Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        final Object obj = field.get(env);
                        final Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            }
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | SecurityException
                | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getStdOut() {
        return this.out;
    }
}