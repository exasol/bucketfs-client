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
    private static final Pattern PATH_PATTERN = Pattern.compile("/(\\w{1,128})/(\\w{1,128})/(.*)");
    private static final int DEFAULT_PORT = 2580;
    private static final int UNDEFINED_PORT = -1;
    private final String host;
    private final int port;
    private final String serviceName;
    private final String bucketName;
    private final String pathInBucket;
    private final boolean useTls;

    /**
     * Create a BucketFS URL.
     *
     * @param host         host on which the BucketFS service runs
     * @param port         port on which the BucketFS service listens
     * @param serviceName  name of the BucketFS service
     * @param bucketName   name of the bucket
     * @param pathInBucket path inside the bucket
     * @param useTls       set to {@code true} if TLS should be used
     */
    public BucketFsUrl(final String host, final int port, final String serviceName, final String bucketName,
            final String pathInBucket, final boolean useTls) {
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.bucketName = bucketName;
        this.pathInBucket = pathInBucket;
        this.useTls = useTls;
    }

    /**
     * Creates a URL object from the String representation.
     *
     * @param spec String to parse as a URL
     *
     * @throws MalformedURLException if the port is negative or if either BucketFS service or bucket are missing in the
     *                               path or the path is not an absolute path
     */
    public BucketFsUrl(final String spec) throws MalformedURLException {
        try {
            final URI uri = new URI(spec);
            final String path = uri.getPath();
            final Matcher matcher = PATH_PATTERN.matcher(path);
            if (matcher.matches()) {
                this.serviceName = matcher.group(1);
                this.bucketName = matcher.group(2);
                this.pathInBucket = matcher.group(3);
                this.host = uri.getHost();
                this.port = uri.getPort();
                this.useTls = BUCKETFS_PROTOCOL_WITH_TLS.equals(uri.getScheme());
            } else {
                throw new MalformedURLException("Illegal path in bucket: '" + path
                        + "'. Path must have the following form: '/<bucketfs-service/<bucket>/<path-in-bucket>");
            }
        } catch (final URISyntaxException exception) {
            throw new MalformedURLException(exception.getMessage());
        }
    }

    /**
     * Get the path part of this URL.
     *
     * @return path part or this URL
     */
    public String getPath() {
        return PATH_SEPARATOR + this.serviceName + PATH_SEPARATOR + this.bucketName + PATH_SEPARATOR
                + this.pathInBucket;
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
     *
     * @throws MalformedURLException if the port is negative
     */
    public URI toURI() throws MalformedURLException {
        try {
            return new URI(getProtocol(), null, this.host, this.port, getPath(), null, null);
        } catch (final URISyntaxException exception) {
            throw new MalformedURLException(exception.getMessage());
        }
    }

    /**
     * Get the default port number of the protocol associated with this URL. If the URL scheme or the URLStreamHandler
     * for the URL do not define a default port number, then -1 is returned.
     *
     * @return port number
     */
    public int getDefaultPort() {
        return this.useTls ? UNDEFINED_PORT : DEFAULT_PORT;
    }

    /**
     * Gets the protocol name of this URL.
     *
     * @return protocol of this URL
     */
    public String getProtocol() {
        return this.useTls ? BUCKETFS_PROTOCOL_WITH_TLS : BUCKETFS_PROTOCOL;
    }

    /**
     * Gets the host name of this URL.
     *
     * @return host name of this URL
     */
    public String getHost() {
        return this.host;
    }

    @Override
    public String toString() {
        return getProtocol() + "://" + this.host + ":" + this.port + PATH_SEPARATOR + this.serviceName + PATH_SEPARATOR
                + this.bucketName + PATH_SEPARATOR + this.pathInBucket;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bucketName, this.host, this.pathInBucket, this.port, this.serviceName, this.useTls);
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
        return Objects.equals(this.bucketName, other.bucketName) && Objects.equals(this.host, other.host)
                && Objects.equals(this.pathInBucket, other.pathInBucket) && (this.port == other.port)
                && Objects.equals(this.serviceName, other.serviceName) && (this.useTls == other.useTls);
    }
}