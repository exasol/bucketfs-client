package com.exasol.bucketfs.url;

import static com.exasol.bucketfs.Fallback.fallback;

import java.net.*;
import java.util.Objects;

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
        return (uri != null)
                && (BUCKETFS_PROTOCOL.equals(uri.getScheme()) || BUCKETFS_PROTOCOL_WITH_TLS.equals(uri.getScheme()));
    }

    public static final String BUCKETFS_PROTOCOL = "bfs";
    public static final String BUCKETFS_PROTOCOL_WITH_TLS = "bfss";
    public static final String PATH_SEPARATOR = "/";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 2580;
    public static final int UNDEFINED_PORT = -1;

    /**
     * @param uri     URI to create the URL from
     * @param profile default values for specific parts of BucketFS URLs
     * @return new BucketFS URL
     * @throws MalformedURLException if BucketFS service or bucket are missing in the path or the path is not an
     *                               absolute path
     */
    public static BucketFsUrl from(final URI uri, final Profile profile) {
        try {
            return new BucketFsUrl( //
                    uri.getScheme(), //
                    fallback(null, uri.getHost(), profile.host(), DEFAULT_HOST), //
                    fallback(UNDEFINED_PORT, uri.getPort(), profile.port(), DEFAULT_PORT), //
                    BucketFsPath.from(uri, profile.bucket()));
        } catch (final MalformedURLException exception) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-5") //
                    .message("Invalid BucketFS URL: {{url}}", uri) //
                    .message("Cause: {{cause}}.", exception.getMessage()) //
                    .mitigation("Please use URL with the following form: {{form}}.",
                            "bfs://<bucketfs-service/<bucket>/<path-in-bucket>") //
                    .toString());
        }
    }

    private final String protocol;
    private final String host;
    private final int port;
    private final BucketFsPath bucketFsPath;
    private final URI uri;

    /**
     * @param protocol
     * @param host
     * @param port
     * @param bucketName
     * @param pathInBucket
     * @throws MalformedURLException
     */
    BucketFsUrl(final String protocol, final String host, final int port, final BucketFsPath bucketFsPath)
            throws MalformedURLException {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.bucketFsPath = bucketFsPath;
        try {
            this.uri = makeUri(protocol, host, port, bucketFsPath);
        } catch (final URISyntaxException exception) {
            throw new MalformedURLException(exception.getMessage());
        }
    }

    private static URI makeUri(final String protocol, final String host, final int port,
            final BucketFsPath bucketFsPath) throws URISyntaxException {
        return new URI(protocol, null, host, port, bucketFsPath.getUriPath(), null, null);
    }

    /**
     * Get the path part of this URL.
     *
     * @return path part or this URL
     */
    public String getPath() {
        return this.uri.getPath();
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
     * Return a URI equivalent to this BucketFS URL.
     *
     * @return URI instance equivalent to this URL
     */
    public URI toURI() {
        return this.uri;
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
        return BUCKETFS_PROTOCOL_WITH_TLS.equals(getProtocol());
    }

    /**
     * Get the protocol name of this URL.
     *
     * @return protocol of this URL
     */
    public String getProtocol() {
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
        return this.uri.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uri);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BucketFsUrl)) {
            return false;
        }
        final BucketFsUrl other = (BucketFsUrl) obj;
        return Objects.equals(this.uri, other.uri);
    }
}
