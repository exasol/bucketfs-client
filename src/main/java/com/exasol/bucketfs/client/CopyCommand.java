package com.exasol.bucketfs.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import com.exasol.bucketfs.*;
import com.exasol.bucketfs.url.BucketFsUrl;

import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(name = "cp", description = "Copy SOURCE to DEST, or multiple SOURCE(s) to DIRECTORY")
public class CopyCommand implements Callable<Integer> {
    @Parameters(index = "0", paramLabel = "SOURCE", description = "source")
    private URI source;

    @Parameters(index = "1", paramLabel = "DEST", description = "destination")
    private URI destination;

    @Option(names = { "-p", "--password" }, description = "password", interactive = true)
    private String password;

    @Override
    public Integer call() {
        if (BucketFsUrl.isBucketFsUrl(this.destination)) {
            upload();
        } else {
            download();
        }
        return CommandLine.ExitCode.OK;
    }

    private void upload() {
        try {
            final BucketFsUrl url = createDestinationBucketFsUrl();
            final UnsynchronizedBucket bucket = WriteEnabledBucket.builder() //
                    .ipAddress(url.getHost()) //
                    .httpPort(url.getPort()) //
                    .serviceName(url.getServiceName()) //
                    .name(url.getBucketName()) //
                    .writePassword(this.password) //
                    .build();
            final Path sourcePath = convertSpecToPath(this.source);
            bucket.uploadFileNonBlocking(sourcePath, url.getPathInBucket());
        } catch (final BucketAccessException exception) {
            throw new BucketFsClientException(
                    "Unable to upload file to " + this.destination + "\nReason: " + exception.getCause().getMessage(),
                    exception);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BucketFsClientException(
                    "Got interrupted trying to upload file from " + this.source + " to " + this.destination);
        } catch (final TimeoutException exception) {
            throw new BucketFsClientException("Upload to " + this.destination + " timed out.");
        }
    }

    private Path convertSpecToPath(final URI pathSpec) {
        return (pathSpec.getScheme() == null) ? Path.of(pathSpec.getPath()) : Path.of(pathSpec);
    }

    private BucketFsUrl createDestinationBucketFsUrl() {
        try {
            return BucketFsUrl.create(this.destination);
        } catch (final MalformedURLException exeption) {
            throw new BucketFsClientException("Illegal BucketFS destination URL: " + this.destination);
        }
    }

    private void download() {
        try {
            final BucketFsUrl sourceUrl = createSourceBucketFsUrl();
            final ReadOnlyBucket bucket = ReadEnabledBucket.builder() //
                    .ipAddress(sourceUrl.getHost()) //
                    .httpPort(sourceUrl.getPort()) //
                    .serviceName(sourceUrl.getServiceName()) //
                    .name(sourceUrl.getBucketName()) //
                    .build();
            final Path destinationPath = convertSpecToPath(this.destination);
            bucket.downloadFile(sourceUrl.getPathInBucket(), destinationPath);
        } catch (final BucketAccessException exception) {
            throw new BucketFsClientException("Unable to download file from \"" + this.source + "\"\nReason: "
                    + exception.getCause().getMessage(), exception);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BucketFsClientException(
                    "Got interrupted trying to download file from " + this.source + " to " + this.destination);
        }
    }

    private BucketFsUrl createSourceBucketFsUrl() {
        try {
            return BucketFsUrl.create(this.source);
        } catch (final MalformedURLException exeption) {
            throw new BucketFsClientException("Illegal BucketFS source URL: " + this.source);
        }
    }
}