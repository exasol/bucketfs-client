package com.exasol.bucketfs.url;

import static com.exasol.bucketfs.url.BucketFsUrl.PATH_SEPARATOR;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class PathCompleter {

    private final Path sourceRoot;
    private final String pathInBucket;
    private final boolean isDirectory;

    public PathCompleter(final Path sourceRoot, final String pathInBucket) {
        this.sourceRoot = Objects.requireNonNull(sourceRoot, "sourceRoot");
        this.pathInBucket = Objects.requireNonNull(pathInBucket, "pathInBucket");
        this.isDirectory = BucketFsLocation.isDirectorySyntax(pathInBucket);
    }

    public String complete(final Path sourcePath) {
        final String relative = this.sourceRoot.relativize(sourcePath).toString().replace("\\", PATH_SEPARATOR);
        if (Files.isDirectory(sourcePath)) {
            return this.pathInBucket //
                    + cutFirstComponent(relative) //
                    + PATH_SEPARATOR;
        }
        return this.pathInBucket + (this.isDirectory ? relative : "");
    }

    private String cutFirstComponent(final String relative) {
        if (this.isDirectory) {
            return relative;
        }
        final int i = relative.indexOf(PATH_SEPARATOR);
        return (i < 0) ? "" : relative.substring(i + 1);
    }
}
