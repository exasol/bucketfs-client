package com.exasol.bucketfs.profile;

import java.util.Objects;

import com.exasol.bucketfs.url.BucketFsUrl;

/**
 * Defines a profile for with default values for parts of {@link BucketFsUrl} and password.
 */
public class Profile {

    public static Profile empty() {
        return new Profile(null, null, null, null);
    }

    private final String host;
    private final String port;
    private final String bucket;
    private final String password;

    /**
     * Constructor for productive usage
     *
     * @param host
     * @param object
     * @param bucket
     * @param password
     */
    public Profile(final String host, final String object, final String bucket, final String password) {
        this.host = host;
        this.port = object;
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

    @Override
    public int hashCode() {
        return Objects.hash(this.bucket, this.host, this.password, this.port);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Profile other = (Profile) obj;
        return Objects.equals(this.bucket, other.bucket) && Objects.equals(this.host, other.host)
                && Objects.equals(this.password, other.password) && Objects.equals(this.port, other.port);
    }
}
