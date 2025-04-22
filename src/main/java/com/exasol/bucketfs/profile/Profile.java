package com.exasol.bucketfs.profile;

import java.nio.file.Path;

import com.exasol.bucketfs.url.BucketFsProtocol;
import com.exasol.bucketfs.url.BucketFsUrl;

/**
 * Defines a profile for with default values for parts of {@link BucketFsUrl} and password.
 * 
 * @param protocol       BucketFS protocol of the BucketFS service
 * @param host           host name or IP address of BucketFS service
 * @param port           port HTTP or HTTPS port the BucketFS service listens on
 * @param bucket         name of the root bucket
 * @param readPassword   password for reading, required for private buckets
 * @param writePassword  password for writing to the bucket
 * @param tlsCertificate path to the TLS certificate file of the BucketFS service
 */
public record Profile(BucketFsProtocol protocol, String host, int port, String bucket, String readPassword,
        String writePassword, Path tlsCertificate) {

    public static Profile empty() {
        return new Profile(BucketFsProtocol.BFS, null, BucketFsUrl.UNDEFINED_PORT, null, null, null, null);
    }

    /**
     * @return new {@link Builder} instance {@link Profile} record.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for the {@link Profile} record.
     */
    public static class Builder {
        private BucketFsProtocol protocol = BucketFsProtocol.BFS;
        private String host;
        private int port = BucketFsUrl.UNDEFINED_PORT;
        private String bucket;
        private String readPassword;
        private String writePassword;
        private Path tlsCertificate;

        private Builder() {
        }

        /**
         * @param protocol protocol of the BucketFS service, default: {@link BucketFsProtocol#BFS}.
         * @return self
         */
        public Builder protocol(final BucketFsProtocol protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * @param host host name or IP address of the BucketFS service
         * @return self
         */
        public Builder host(final String host) {
            this.host = host;
            return this;
        }

        /**
         * @param port HTTP or HTTPS port the BucketFS service listens on
         * @return self
         */
        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        /**
         * @param port HTTP or HTTPS port the BucketFS service listens on
         * @return self
         */
        public Builder port(final String port) {
            return this.port(port != null ? Integer.parseInt(port) : BucketFsUrl.UNDEFINED_PORT);
        }

        /**
         * @param bucket name of the root bucket
         * @return self
         */
        public Builder bucket(final String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * @param readPassword password for reading, required for private buckets
         * @return self
         */
        public Builder readPassword(final String readPassword) {
            this.readPassword = readPassword;
            return this;
        }

        /**
         * @param writePassword password for writing to the bucket
         * @return self
         */
        public Builder writePassword(final String writePassword) {
            this.writePassword = writePassword;
            return this;
        }

        /**
         * @param tlsCertificate path to the TLS certificate file of the BucketFS service
         * @return self
         */
        public Builder tlsCertificate(final Path tlsCertificate) {
            this.tlsCertificate = tlsCertificate;
            return this;
        }

        /**
         * Builds a {@link Profile} instance using the accumulated values.
         * 
         * @return new {@link Profile} instance
         */
        public Profile build() {
            return new Profile(this.protocol, this.host, this.port, this.bucket, this.readPassword, this.writePassword,
                    this.tlsCertificate);
        }
    }
}
