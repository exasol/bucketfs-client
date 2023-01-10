package com.exasol.bucketfs.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.UnsynchronizedBucket;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.bucketfs.url.PathCompleter;
import com.exasol.errorreporting.ExaError;

public class Uploader extends SimpleFileVisitor<Path> {

    public static Uploader from(final Path sourcePath, final UnsynchronizedBucket bucket, final BucketFsUrl url) {
        return new Uploader(bucket, new PathCompleter(sourcePath, url.getPathInBucket()));
    }

    private static final Logger LOGGER = Logger.getLogger(Uploader.class.getName());
    private final UnsynchronizedBucket bucket;
    private final PathCompleter pathCompleter;

    public Uploader(final UnsynchronizedBucket bucket, final PathCompleter pathCompleter) {
        this.bucket = bucket;
        this.pathCompleter = pathCompleter;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        final String pathInBucket = this.pathCompleter.complete(file);
        LOGGER.finest(() -> "uploading " + file + " to " + pathInBucket);
        try {
            this.bucket.uploadFileNonBlocking(file, pathInBucket);
            return FileVisitResult.CONTINUE;
        } catch (final BucketAccessException exception) {
            throw new BucketFsClientException(exception);
        } catch (final TimeoutException exception) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-1")
                    .message("Upload to {{destination}} timed out.", pathInBucket).toString(), exception);
        } catch (final FileNotFoundException exception) {
            throw createExceptionForFileNotFound(file);
        }
    }

    public static BucketFsClientException createExceptionForFileNotFound(final Path file) {
        return new BucketFsClientException(ExaError.messageBuilder("E-BFSC-2") //
                .message("Unable to upload. No such file or directory: {{source-path}}", file) //
                .toString());
    }
}
