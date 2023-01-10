package com.exasol.bucketfs.profile;

import java.util.Base64;
import java.util.Objects;

import com.exasol.bucketfs.url.BucketFsUrl;

/**
 * Defines a profile for with default values for parts of {@link BucketFsUrl} and password.
 */
public class Profile {

    public static Profile empty(final boolean decodePasswords) {
        return new Profile(null, null, null, null, null, decodePasswords);
    }

    private final String host;
    private final String port;
    private final String bucket;
    private final String readPassword;
    private final String writePassword;
    private final boolean decodePasswords;

    /**
     * Constructor for productive usage
     *
     * @param host            host name or IP address of BucketFS service
     * @param port            port HTTP or HTTPS port the BucketFS service listens on
     * @param bucket          name of the root bucket
     * @param readPassword    password for reading, required for private buckets
     * @param writePassword   password for writing to the bucket
     * @param decodePasswords {@code true} if BFSC should apply base-64 decoding to passwords from profile or from
     *                        interactive prompt
     */
    public Profile(final String host, final String port, final String bucket, final String readPassword,
            final String writePassword, final boolean decodePasswords) {
        this.host = host;
        this.port = port;
        this.bucket = bucket;
        this.readPassword = readPassword;
        this.writePassword = writePassword;
        this.decodePasswords = decodePasswords;
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
    public String readPassword() {
        return decodePassword(this.readPassword);
    }

    /**
     * @return password for writing to the bucket
     */
    public String writePassword() {
        return decodePassword(this.writePassword);
    }

    public boolean decodePasswords() {
        return this.decodePasswords;
    }

    /**
     * if {@link #decodePasswords()} is {@code true} then return the Base64-decoded value of the argument otherwise
     * return the input string.
     *
     * @param raw raw argument, potentially Base64 encoded
     * @return decoded argument
     */
    public String decodePassword(final String raw) {
        return (this.decodePasswords && (raw != null))//
                ? new String(Base64.getDecoder().decode(raw))
                : raw;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bucket, this.decodePasswords, this.host, this.port, this.readPassword,
                this.writePassword);
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
        return Objects.equals(this.bucket, other.bucket) && (this.decodePasswords == other.decodePasswords)
                && Objects.equals(this.host, other.host) && Objects.equals(this.port, other.port)
                && Objects.equals(this.readPassword, other.readPassword)
                && Objects.equals(this.writePassword, other.writePassword);
    }
}
