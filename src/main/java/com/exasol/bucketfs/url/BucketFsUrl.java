package com.exasol.bucketfs.url;

import java.net.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BucketFS-specific URL.
 *
 * For compatibility this class is modeled after {@link java.net.URL}.
 */
public final class BucketFsUrl {
    public static final String BUCKETFS_PROTOCOL = "bfs";
    public static final String BUCKETFS_PROTOCOL_WITH_TLS = "bfss";
    public static final String PATH_SEPARATOR = "/";
    private static final Pattern PATH_PATTERN = Pattern.compile("/(\\w{1,128})/(\\w{1,128})(/.*)");
    private static final int DEFAULT_PORT = 2580;
    private static final int UNDEFINED_PORT = -1;
    private final URI uri;
    private String cachedServiceName;
    private String cachedBucketName;
    private String cachedPathInBucket;

    /**
     * Create a URL object from the String representation.
     *
     * @param spec String to parse as a URL
     * @return new BucketFS URL
     *
     * @throws MalformedURLException if the port is negative or if either BucketFS service or bucket are missing in the
     *                               path or the path is not an absolute path
     */
    public static BucketFsUrl create(final String spec) throws MalformedURLException {
        try {
            final URI uri = new URI(spec);
            return new BucketFsUrl(uri);
        } catch (final URISyntaxException exception) {
            throw new MalformedURLException(exception.getMessage());
        }
    }

    /**
     * Create a URL object from a URI.
     *
     * @param uri URI to create the URL from
     * @return new BucketFS URL
     *
     * @throws MalformedURLException if BucketFS service or bucket are missing in the path or the path is not an
     *                               absolute path
     */
    public static BucketFsUrl create(final URI uri) throws MalformedURLException {
        return new BucketFsUrl(uri);
    }

    public static boolean isBucketFsUrl(final URI inputUri) {
        return (inputUri != null) && (BUCKETFS_PROTOCOL.equals(inputUri.getScheme())
                || BUCKETFS_PROTOCOL_WITH_TLS.equals(inputUri.getScheme()));
    }

    /**
     * Create a BucketFS URL.
     *
     * @param host         host on which the BucketFS service runs
     * @param port         port on which the BucketFS service listens
     * @param serviceName  name of the BucketFS service
     * @param bucketName   name of the bucket
     * @param pathInBucket path inside the bucket
     * @param useTls       set to {@code true} if TLS should be used
     *
     * @throws MalformedURLException if the port is negative or if either BucketFS service or bucket are missing in the
     *                               path or the path is not an absolute path
     */
    public BucketFsUrl(final String host, final int port, final String serviceName, final String bucketName,
            final String pathInBucket, final boolean useTls) throws MalformedURLException {
        try {
            final String path = calculatePath(serviceName, bucketName, pathInBucket);
            final String protocol = calculateProtocol(useTls);
            this.uri = new URI(protocol, null, host, port, path, null, null);
            this.cachedServiceName = serviceName;
            this.cachedBucketName = bucketName;
            this.cachedPathInBucket = pathInBucket;
        } catch (final URISyntaxException exception) {
            throw new MalformedURLException(exception.getMessage());
        }
    }

    private String calculatePath(final String serviceName, final String bucketName, final String pathInBucket) {
        return PATH_SEPARATOR + serviceName + PATH_SEPARATOR + bucketName + PATH_SEPARATOR + pathInBucket;
    }

    private String calculateProtocol(final boolean useTls) {
        return useTls ? BUCKETFS_PROTOCOL_WITH_TLS : BUCKETFS_PROTOCOL;
    }

    private void cacheUriPathElements(final String path) throws MalformedURLException {
        final Matcher matcher = PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            this.cachedServiceName = matcher.group(1);
            this.cachedBucketName = matcher.group(2);
            this.cachedPathInBucket = matcher.group(3);
        } else {
            throw new MalformedURLException("Illegal path in bucket: '" + path
                    + "'. Path must have the following form: '/<bucketfs-service/<bucket>/<path-in-bucket>");
        }
    }

    private BucketFsUrl(final URI uri) throws MalformedURLException {
        this.uri = uri;
        cacheUriPathElements(uri.getPath());
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
     * Get the name of the BucketFS service from the URL.
     *
     * @return name of the BucketFS service
     */
    public String getServiceName() {
        return this.cachedServiceName;
    }

    /**
     * Get the name of the bucket from the URL.
     *
     * @return name of the bucket
     */
    public String getBucketName() {
        return this.cachedBucketName;
    }

    /**
     * Get the part the URL path that represents the path inside the bucket.
     *
     * @return path inside the bucket.
     */
    public String getPathInBucket() {
        return this.cachedPathInBucket;
    }

    /**
     * Get the port number of this URL.
     *
     * @return port number, or -1 if the port is not set
     */
    public int getPort() {
        return this.uri.getPort();
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
        return this.uri.getScheme();
    }

    /**
     * Get the host name of this URL.
     *
     * @return host name of this URL
     */
    public String getHost() {
        return this.uri.getHost();
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