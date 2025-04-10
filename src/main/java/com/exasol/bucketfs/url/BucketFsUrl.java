package com.exasol.bucketfs.url;

import static java.util.Objects.requireNonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;
import java.util.Set;

import com.exasol.bucketfs.Fallback;
import com.exasol.bucketfs.client.BucketFsClientException;
import com.exasol.bucketfs.profile.Profile;
import com.exasol.errorreporting.ExaError;

/**
 * BucketFS-specific URL.
 *
 * For compatibility this class is modeled after {@link java.net.URL}.
 */
// [impl->dsn~bucket-fs-url~1]
public final class BucketFsUrl {

    /**
     * @param uri URI to check whether it refers to BucketFS or not
     * @return {@code true} if URI refers to BucketFS
     */
    public static boolean isBucketFsUrl(final URI uri) {
        return (uri != null) && (BUCKETFS_PROTOCOL.equals(uri.getScheme()) //
                || BUCKETFS_PROTOCOL_WITH_TLS.equals(uri.getScheme()));
    }

    public static final String PATH_SEPARATOR = "/";
    public static final int UNDEFINED_PORT = -1;

    static final String BUCKETFS_PROTOCOL = "bfs";
    static final String BUCKETFS_PROTOCOL_WITH_TLS = "bfss";
    private static final Set<String> ALLOWED_PROTOCOLS = Set.of(BUCKETFS_PROTOCOL, BUCKETFS_PROTOCOL_WITH_TLS);

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 2580;

    /**
     * @param uri     URI to create the URL from
     * @param profile default values for specific parts of BucketFS URLs
     * @return new instance of {@link BucketFsUrl}
     * @throws MalformedURLException if BucketFS service or bucket are missing in the path or the path is not an
     *                               absolute path
     */
    public static BucketFsUrl from(final URI uri, final Profile profile) {
        try {
            final BucketFsPath path = BucketFsPath.from(uri, profile.bucket());
            final String scheme = uri.getScheme();
            final BucketFsProtocol protocol = scheme != null ? BucketFsProtocol.forName(scheme) : BucketFsProtocol.BFS;
            return from(protocol, uri.getHost(), uri.getPort(), path, profile);
        } catch (final MalformedURLException exception) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-5") //
                    .message("Invalid BucketFS URL: {{url}}.", uri) //
                    .message(" Cause: {{cause}}.", exception.getMessage()) //
                    .mitigation("Please use URL with the following form: {{form}}.",
                            "bfs://<bucketfs-service/<bucket>/<path-in-bucket>") //
                    .toString());
        }
    }

    /**
     * @param profile {@link Profile} with default values for specific parts of BucketFS URLs
     * @return new instance of {@link BucketFsUrl} potentially with invalid {@link BucketFsPath}
     */
    public static BucketFsUrl from(final Profile profile) {
        final BucketFsPath bucketFsPath = new BucketFsPath(profile.bucket(), "");
        return from(profile.protocol(), null, UNDEFINED_PORT, bucketFsPath, profile);
    }

    private static BucketFsUrl from(final BucketFsProtocol protocol, final String host, final int port,
            final BucketFsPath bucketFsPath, final Profile profile) {
        return new BucketFsUrl( //
                protocol, //
                Fallback.of(null, host, profile.host(), DEFAULT_HOST), //
                Fallback.of(UNDEFINED_PORT, port, profile.port(), DEFAULT_PORT), //
                bucketFsPath);
    }

    private final BucketFsProtocol protocol;
    private final String host;
    private final int port;
    private final BucketFsPath bucketFsPath;

    /**
     * Create a new instance.
     * 
     * @param protocol     protocol of this URL, one of
     *                     <ul>
     *                     <li>{@link #BUCKETFS_PROTOCOL}</li>
     *                     <li>{@link #BUCKETFS_PROTOCOL_WITH_TLS}</li>
     *                     </ul>
     * @param host         host of this URL
     * @param port         port number, or -1 if the port is not set
     * @param bucketFsPath name of the bucket and remaining path inside the bucket
     */
    BucketFsUrl(final BucketFsProtocol protocol, final String host, final int port, final BucketFsPath bucketFsPath) {
        this.protocol = requireNonNull(protocol, "protocol");
        this.host = requireNonNull(host, "host");
        this.port = requireNonNull(port, "port");
        this.bucketFsPath = requireNonNull(bucketFsPath, "bucketFsPath");
    }

    /**
     * Get the name of the bucket from the URL.
     *
     * @return name of the bucket
     */
    public String getBucketName() {
        return this.bucketFsPath.getBucketName();
    }

    /**
     * Get the part the URL path that represents the path inside the bucket.
     *
     * @return path inside the bucket.
     */
    public String getPathInBucket() {
        return this.bucketFsPath.getPathInBucket();
    }

    /**
     * Get the port number of this URL.
     *
     * @return port number, or -1 if the port is not set
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Get the default port number of the protocol associated with this URL. If the URL scheme or the URLStreamHandler
     * for the URL do not define a default port number, then -1 is returned.
     *
     * @return port number
     */
    public int getDefaultPort() {
        return isTlsEnabled() ? UNDEFINED_PORT : DEFAULT_PORT;
    }

    /**
     * Check whether this BucketFS URL uses TLS.
     *
     * @return {@code true} if the URL is points to a resource accessed via a TLS-secured connection.
     */
    public boolean isTlsEnabled() {
        return protocol.isTlsEnabled();
    }

    /**
     * Get the protocol name of this URL.
     *
     * @return protocol of this URL
     */
    public BucketFsProtocol getProtocol() {
        return this.protocol;
    }

    /**
     * Get the host name of this URL.
     *
     * @return host name of this URL
     */
    public String getHost() {
        return this.host;
    }

    @Override
    public String toString() {
        return this.protocol.getName() + "://" + this.host + ":" + this.port + this.bucketFsPath.getUriPath();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bucketFsPath, this.host, this.port, this.protocol);
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
        final BucketFsUrl other = (BucketFsUrl) obj;
        return Objects.equals(this.bucketFsPath, other.bucketFsPath) && Objects.equals(this.host, other.host)
                && (this.port == other.port) && Objects.equals(this.protocol, other.protocol);
    }
}
