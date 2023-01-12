package com.exasol.bucketfs.profile;

import java.util.Objects;

import com.exasol.bucketfs.url.BucketFsUrl;

/**
 * Defines a profile for with default values for parts of {@link BucketFsUrl} and password.
 */
public class Profile {

    public static Profile empty() {
        return new Profile(null, null, null, null, null);
    }

    private final String host;
    private final String port;
    private final String bucket;
    private final String readPassword;
    private final String writePassword;

    /**
     * Constructor for productive usage
     *
     * @param host          host name or IP address of BucketFS service
     * @param port          port HTTP or HTTPS port the BucketFS service listens on
     * @param bucket        name of the root bucket
     * @param readPassword  password for reading, required for private buckets
     * @param writePassword password for writing to the bucket
     */
    public Profile(final String host, final String port, final String bucket, final String readPassword,
            final String writePassword) {
        this.host = host;
        this.port = port;
        this.bucket = bucket;
        this.readPassword = readPassword;
        this.writePassword = writePassword;
    }

    /**
     * @return host name or IP address of BucketFS service
     */
    public String host() {
        return this.host;
    }

    /**
     * @return HTTP or HTTPS port the BucketFS service listens on
     */
    public int port() {
        return this.port != null ? Integer.parseInt(this.port) : BucketFsUrl.UNDEFINED_PORT;
    }

    /**
     * @return name of the root bucket
     */
    public String bucket() {
        return this.bucket;
    }

    /**
     * @return password for reading, required for private buckets
     */
    public String getReadPassword() {
        return this.readPassword;
    }

    /**
     * @return password for writing to the bucket
     */
    public String getWritePassword() {
        return this.writePassword;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bucket, this.host, this.port, this.readPassword, this.writePassword);
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
                && Objects.equals(this.port, other.port) && Objects.equals(this.readPassword, other.readPassword)
                && Objects.equals(this.writePassword, other.writePassword);
    }
}
