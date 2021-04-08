package com.exasol.bucketfs.client;

import java.net.URI;
import java.nio.file.Path;

import com.exasol.bucketfs.*;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "cp", description = "Copy SOURCE to DEST, or multiple SOURCE(s) to DIRECTORY")
public class CopyCommand implements Runnable {
    @Parameters(index = "0", paramLabel = "SOURCE", description = "source")
    private URI source;

    @Parameters(index = "1", paramLabel = "DEST", description = "destination")
    private URI destination;

    @Override
    public void run() {
        final String host = this.source.getHost();
        final int port = this.source.getPort();
        final UnsynchronizedBucket bucket = WriteEnabledBucket.builder() //
                .ipAddress(host) //
                .httpPort(port) //
                .serviceName("bfsdefault") //
                .name("default") //
                .build();
        try {
            if (this.destination.getScheme() == null) {
                bucket.downloadFile("test.txt", Path.of(this.destination.getPath()));
            } else {
                bucket.downloadFile("test.txt", Path.of(this.destination));
            }
        } catch (final BucketAccessException exception) {
            throw new BucketFsClientException(
                    "Unable to download file from " + this.source + " Reason: " + exception.getCause().getMessage(),
                    exception);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BucketFsClientException(
                    "Got interrupted trying to download file from " + this.source + " to " + this.destination);
        }
    }
}