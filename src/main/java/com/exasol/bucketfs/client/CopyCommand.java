package com.exasol.bucketfs.client;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import com.exasol.bucketfs.*;
import com.exasol.bucketfs.profile.Profile;
import com.exasol.bucketfs.profile.ProfileProvider;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.bucketfs.url.UriConverter;
import com.exasol.errorreporting.ExaError;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * This class implements copy operations to and from BucketFS
 */
//[impl->dsn~command-line-parsing~1]
@Command(name = "cp", description = "Copy SOURCE to DEST, or multiple SOURCE(s) to DIRECTORY")
public class CopyCommand implements Callable<Integer> {

    private final ProfileProvider profileProvider;
    @Parameters(index = "0", paramLabel = "SOURCE", description = "source", converter = UriConverter.class)
    private URI source;
    @Parameters(index = "1", paramLabel = "DEST", description = "destination", converter = UriConverter.class)
    private URI destination;

    public CopyCommand(final ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

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
        try {
            final BucketFsUrl url = createDestinationBucketFsUrl();
            final String password = PasswordReader.readPassword(this.profileProvider.getProfile());
            final UnsynchronizedBucket bucket = WriteEnabledBucket.builder() //
                    .host(url.getHost()) //
                    .port(url.getPort()) //
                    .name(url.getBucketName()) //
                    .writePassword(password) //
                    .build();
            bucket.uploadFileNonBlocking(sourcePath, url.getPathInBucket());
        } catch (final BucketAccessException exception) {
            throw new BucketFsClientException(exception);
        } catch (final TimeoutException exception) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-1")
                    .message("Upload to {{destination}} timed out.", this.destination).toString());
        } catch (final FileNotFoundException exception) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-2")
                    .message("Unable to upload. No such file or directory: {{source-path}}", sourcePath).toString());
        }
    }

    private Path convertSpecToPath(final URI pathSpec) {
        return (pathSpec.getScheme() == null) ? Path.of(pathSpec.getPath()) : Path.of(pathSpec);
    }

    // [impl->dsn~copy-command-copies-file-from-bucket~1]
    private void download() {
        try {
            final BucketFsUrl url = BucketFsUrl.from(this.source, this.profileProvider.getProfile());
            final ReadOnlyBucket bucket = ReadEnabledBucket.builder() //
                    .host(url.getHost()) //
                    .port(url.getPort()) //
                    .name(url.getBucketName()) //
                    .build();
            final Path destinationPath = convertSpecToPath(this.destination);
            bucket.downloadFile(url.getPathInBucket(), destinationPath);
        } catch (final BucketAccessException exception) {
            throw new BucketFsClientException(exception);
        }
    }
}
