package com.exasol.bucketfs.url;

import static com.exasol.bucketfs.url.BucketFsUrl.PATH_SEPARATOR;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.exasol.bucketfs.Fallback;
import com.exasol.errorreporting.ExaError;

class BucketFsPath {

    private static final Pattern PATH_PATTERN = Pattern.compile("/(\\w{1,128})(/.*)");

    // java doc for standard java class URI:
    // The path of a hierarchical URI that is either absolute or specifies an authority is always absolute.
    static BucketFsPath from(final URI uri, final String defaultBucket) throws MalformedURLException {
        final String bucketName = Fallback.of(null, defaultBucket);
        final String path = Optional.ofNullable(uri.getPath()).orElse("");
        if (bucketName != null) {
            return new BucketFsPath(bucketName, path);
        }

        final Matcher matcher = PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return new BucketFsPath(matcher.group(1), matcher.group(2));
        }

        throw new MalformedURLException(ExaError.messageBuilder("E-BFSC-6") //
                .message("URI contains illegal path in bucket: {{uri}}.", uri) //
                .toString());
    }

    private final String bucketName;
    private final String pathInBucket;

    /**
     * @param bucketName   name of the bucket
     * @param pathInBucket remaining path inside the bucket
     */
    BucketFsPath(final String bucketName, final String pathInBucket) {
        this.bucketName = bucketName;
        this.pathInBucket = pathInBucket;
    }

    String getPathInBucket() {
        return this.pathInBucket;
    }

    String getBucketName() {
        return this.bucketName;
    }

    String getUriPath() {
        return PATH_SEPARATOR + this.bucketName + this.pathInBucket;
    }
}