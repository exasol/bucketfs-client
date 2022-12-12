package com.exasol.bucketfs.env;

import java.util.Map;

import com.exasol.bucketfs.url.BucketFsUrl;

/**
 * Defines environment variables as fallback for parts of {@link BucketFsUrl} and password.
 *
 * <p>
 * Additionally encapsulates access to these environment variables to enable mocking in unit tests.
 * </p>
 */
public class EnvironmentVariables {
    /**
     * @param env environment variables
     * @return new instance of {@link EnvironmentVariables} based on system environment variables.
     */
    public static EnvironmentVariables from(final Map<String, String> env) {
        return new EnvironmentVariables(env.get(HOST), env.get(PORT), env.get(BUCKET), env.get(PASSWORD));
    }

    /** name of environment variable for default host of BucketFS */
    public static final String HOST = "BUCKETFS_HOST";
    /** name of environment variable for default port of BucketFS */
    public static final String PORT = "BUCKETFS_PORT";
    /** name of environment variable for default bucket of BucketFS */
    public static final String BUCKET = "BUCKETFS_BUCKET";
    /** name of environment variable for default write password of BucketFS */
    public static final String PASSWORD = "BUCKETFS_PASSWORD";

    private final String host;
    private final String port;
    private final String bucket;
    private final String password;

    /**
     * Constructor for productive usage
     *
     * @param host
     * @param port
     * @param bucket
     * @param password
     */
    EnvironmentVariables(final String host, final String port, final String bucket, final String password) {
        this.host = host;
        this.port = port;
        this.bucket = bucket;
        this.password = password;
    }

    /**
     * @return host
     */
    public String host() {
        return this.host;
    }

    /**
     * @return port
     */
    public int port() {
        return this.port != null ? Integer.parseInt(this.port) : BucketFsUrl.UNDEFINED_PORT;
    }

    /**
     * @return bucket
     */
    public String bucket() {
        return this.bucket;
    }

    /**
     * @return password
     */
    public String password() {
        return this.password;
    }
}
