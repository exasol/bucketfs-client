package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.url.BucketFsLocation.asDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.ReadOnlyBucket;
import com.exasol.bucketfs.client.UseCaseDetector.RecurseOption;
import com.exasol.bucketfs.client.UseCaseDetector.UseCase;
import com.exasol.bucketfs.url.BucketFsLocation;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.errorreporting.ExaError;

public class Downloader {

    private static final Logger LOGGER = Logger.getLogger(Downloader.class.getName());

    private final BucketFsClient parent;
    private final ReadOnlyBucket bucket;
    private final BucketFsUrl source;
    private final Path destination;

    public Downloader(final BucketFsClient parent, final ReadOnlyBucket bucket, final BucketFsUrl source,
            final Path destination) {
        this.parent = parent;
        this.bucket = bucket;
        this.source = source;
        this.destination = destination;
    }

    public void download() throws BucketAccessException, IOException {
        final BucketFsLocation location = BucketFsLocation.from(this.bucket, this.source.getPathInBucket());
        final RecurseOption recurseOption = RecurseOption.from(this.parent::recursiveOption, this.parent::isRecursive);
        final UseCase usecase = new UseCaseDetector(this.source, this.destination, recurseOption).detect(location);
        final Path dest = destinationWithFilename(location);
        if (usecase == UseCase.DIRECTORY) {
            final String prefix = asDirectory(this.source.getPathInBucket());
            for (final String spec : getFilesForDownload()) {
                final Path file = dest.resolve(spec);
                Files.createDirectories(file.getParent());
                LOGGER.finer(() -> "Downloading " + this.source + spec + " to " + file);
                this.bucket.downloadFile(prefix + spec, file);
            }
        } else {
            this.bucket.downloadFile(this.source.getPathInBucket(), dest);
        }
    }

    /**
     * Potentially append filename from path in bucket to destination path.
     *
     * @param location
     * @return destination incl. filename
     */
    private Path destinationWithFilename(final BucketFsLocation location) {
        return Files.isDirectory(this.destination) //
                ? this.destination.resolve(location.getLastComponent())
                : this.destination;
    }

    // [impl->dsn~copy-ambigue-entrie-on-lower-level~1]
    private List<String> getFilesForDownload() throws BucketAccessException {
        final List<String> list = this.bucket.listContentsRecursively(this.source.getPathInBucket());
        final List<String> result = new ArrayList<>();
        final int n = list.size();
        for (int i = 0; i < n; i++) {
            final String current = list.get(i);
            final String next = (i < (n - 1)) ? list.get(i + 1) : null;
            if ((next != null) && next.startsWith(current)) {
                this.parent.printWarning(ExaError.messageBuilder("W-BFSC-14") //
                        .message("Skipping ambigue file {{file}} as BucketFS contains directory with identical name.", //
                                asDirectory(this.source.getPathInBucket()) + current) //
                        .toString());
            } else {
                result.add(current);
            }
        }
        return result;
    }
}
