package com.exasol.bucketfs.url;

import static com.exasol.bucketfs.url.BucketFsUrl.PATH_SEPARATOR;

import java.util.List;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.ReadOnlyBucket;
import com.exasol.bucketfs.list.ListingRetriever;

public class BucketFsLocation {

    public static boolean isDirectorySyntax(final String pathInBucket) {
        return pathInBucket.endsWith(PATH_SEPARATOR);
    }

    public static String asDirectory(final String path) {
        return path.endsWith(PATH_SEPARATOR) ? path : path + PATH_SEPARATOR;
    }

    public static BucketFsLocation from(final ReadOnlyBucket bucket, final String pathInBucket) {
        final String p = ListingRetriever.removeLeadingSeparator(pathInBucket);
        final int n = p.length() - (p.endsWith(PATH_SEPARATOR) ? 2 : 1);
        final int i = p.lastIndexOf(PATH_SEPARATOR, n);
        return (i >= 0) //
                ? new BucketFsLocation(bucket, p.substring(0, i), p.substring(i + 1))
                : new BucketFsLocation(bucket, "", p);
    }

    private final ReadOnlyBucket bucket;
    private final String prefix;
    private final String last;
    private Status status;

    BucketFsLocation(final ReadOnlyBucket bucket, final String prefix, final String last) {
        this.bucket = bucket;
        this.prefix = prefix;
        this.last = last;
    }

    public boolean isDirectory() throws BucketAccessException {
        return getStatus().isDirectory;
    }

    public boolean isRegularFile() throws BucketAccessException {
        return getStatus().isRegularFile;
    }

    public boolean isOnlyDirectory() throws BucketAccessException {
        return getStatus().isOnlyDirectory();
    }

    public boolean exists() throws BucketAccessException {
        return getStatus().exists();
    }

    public boolean hasDirectorySyntax() {
        return this.last.endsWith(PATH_SEPARATOR);
    }

    private Status getStatus() throws BucketAccessException {
        if (this.status == null) {
            final List<String> listing = this.bucket.listContents(this.prefix);
            this.status = new Status( //
                    listing.contains(asDirectory(this.last)), //
                    !hasDirectorySyntax() && listing.contains(this.last));
        }
        return this.status;
    }

    public String getLastComponent() {
        return this.last;
    }

    static class Status {
        boolean isDirectory;
        boolean isRegularFile;

        Status(final boolean isDirectory, final boolean isRegularFile) {
            this.isDirectory = isDirectory;
            this.isRegularFile = isRegularFile;
        }

        boolean isOnlyDirectory() {
            return this.isDirectory || !this.isRegularFile;
        }

        boolean exists() {
            return this.isDirectory || this.isRegularFile;
        }
    }
}