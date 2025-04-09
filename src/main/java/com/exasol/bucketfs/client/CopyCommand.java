package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.BucketOperation.DOWNLOAD;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.exasol.bucketfs.*;
import com.exasol.bucketfs.profile.Profile;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.bucketfs.url.UriConverter;
import com.exasol.errorreporting.ExaError;

import picocli.CommandLine;
import picocli.CommandLine.*;

/**
 * This class implements copy operations to and from BucketFS
 */
// [impl->dsn~command-line-parsing~1]
@Command(name = "cp", description = "Copy SOURCE to DEST, or multiple SOURCE(s) to DIRECTORY")
public class CopyCommand implements Callable<Integer> {

    @ParentCommand
    BucketFsClient parent;

    @Parameters(index = "0", paramLabel = "SOURCE", description = "source", converter = UriConverter.class)
    private URI source;
    @Parameters(index = "1", paramLabel = "DEST", description = "destination", converter = UriConverter.class)
    private URI destination;

    @Override
    public Integer call() {
        if (BucketFsUrl.isBucketFsUrl(this.destination)) {
            upload();
        } else {
            download();
        }
        return CommandLine.ExitCode.OK;
    }

    // [impl->dsn~copy-command-copies-file-to-bucket~1]
    private void upload() {
        final Path sourcePath = convertSpecToPath(this.source);
        if (Files.isDirectory(sourcePath) && !this.parent.isRecursive()) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-8") //
                    .message("Cannot upload directory {{source}}.", this.source) //
                    .mitigation("Specify option {{option|uq}} to upload directories.", //
                            this.parent.recursiveOption()) //
                    .toString());
        }
        try {
            final Profile profile = this.parent.getProfile();
            final BucketFsUrl url = BucketFsUrl.from(this.destination, profile);
            final UnsynchronizedBucket bucket = this.parent.buildWriteEnabledBucket(destination);
            if (!Files.exists(sourcePath)) {
                throw Uploader.createExceptionForFileNotFound(sourcePath);
            }
            final Path sourceParent = sourcePath.getParent() == null ? Path.of(".") : sourcePath.getParent();
            Files.walkFileTree(sourcePath, Uploader.from(sourceParent, bucket, url));
        } catch (final IOException exception) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-9") //
                    .message("Failed to upload {{file}}", sourcePath)
                    .message("Cause: {{cause}}", exception.getMessage()) //
                    .toString(), exception);
        }
    }

    private Path convertSpecToPath(final URI pathSpec) {
        return (pathSpec.getScheme() == null) ? Path.of(pathSpec.getPath()) : Path.of(pathSpec);
    }

    // [impl->dsn~copy-command-copies-file-from-bucket~1]
    private void download() {
        try {
            final Profile profile = this.parent.getProfile();
            final BucketFsUrl url = BucketFsUrl.from(this.source, profile);
            final ReadOnlyBucket bucket = this.parent.buildReadOnlyBucket(this.source);
            final Path destinationPath = convertSpecToPath(this.destination);
            new Downloader(this.parent, bucket, url, destinationPath).download();
        } catch (final BucketAccessException exception) {
            throw new BucketFsClientException(exception);
        } catch (final IOException exception) {
            throw new BucketFsClientException(
                    BucketAccessException.downloadIoException(this.source, DOWNLOAD, exception));
        }
    }
}
