package com.exasol.bucketfs.url;

import static com.exasol.bucketfs.url.BucketFsUrl.PATH_SEPARATOR;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.exasol.bucketfs.Fallback;
import com.exasol.errorreporting.ExaError;

public class BucketFsPath {

    private static final Pattern PATH_PATTERN = Pattern.compile("/(\\w{1,128})(/.*)");

    // java doc for standard java class URI:
    // The path of a hierarchical URI that is either absolute or specifies an authority is always absolute.
    public static BucketFsPath from(final URI uri, final String defaultBucket) throws MalformedURLException {
        final String bucket = Fallback.fallback(null, defaultBucket);
        final String path = Optional.ofNullable(uri.getPath()).orElse("");
        if (bucket != null) {
            return new BucketFsPath(bucket, path);
        }

        final Matcher matcher = PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return new BucketFsPath(matcher.group(1), matcher.group(2));
        }

        throw new MalformedURLException(ExaError.messageBuilder("E-BFSC-6") //
                .message("URI contains illegal path in bucket: {{uri}}.", uri) //
                .mitigation("Please use URI with the following form: {{form}}",
                        "bfs://<bucketfs-service/<bucket>/<path-in-bucket>") //
                .toString());
    }

    private final String bucket;
    private final String pathInBucket;

    /**
     * @param bucket       name of the bucket
     * @param pathInBucket remaining path inside the bucket
     */
    BucketFsPath(final String bucket, final String pathInBucket) {
        this.bucket = bucket;
        this.pathInBucket = pathInBucket;
    }

    public String getPathInBucket() {
        return this.pathInBucket;
    }

    public String getBucketName() {
        return this.bucket;
    }

    public String getUriPath() {
        return PATH_SEPARATOR + this.bucket + this.pathInBucket;
    }
}
