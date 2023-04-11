package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.url.BucketFsLocation.asDirectory;

import java.net.URI;
import java.util.concurrent.Callable;

import com.exasol.bucketfs.UnsynchronizedBucket;
import com.exasol.bucketfs.WriteEnabledBucket;
import com.exasol.bucketfs.profile.Profile;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.bucketfs.url.UriConverter;

import picocli.CommandLine;
import picocli.CommandLine.*;

/**
 * This class implements listing the contents of BucketFS
 */
@Command(name = "rm", description = "Remove file PATH from BucketFS")
// [impl->dsn~delete-file~1]
// [impl->dsn~no-error-when-deleting-a-non-existing-file~1]
public class DeleteCommand implements Callable<Integer> {

    @ParentCommand
    BucketFsClient parent;

    @Parameters(index = "0", paramLabel = "PATH", description = "path", converter = UriConverter.class)
    private URI uri;

    // [impl->dsn~delete-ambigue-entry-recursively~1]
    // [impl->dsn~delete-ambigue-entry-non-recursively~1]
    @Override
    public Integer call() throws Exception {
        final Profile profile = this.parent.getProfile();
        final BucketFsUrl url = BucketFsUrl.from(this.uri, profile);
        final UnsynchronizedBucket bucket = WriteEnabledBucket.builder() //
                .host(url.getHost()) //
                .port(url.getPort()) //
                .name(url.getBucketName()) //
                .writePassword(this.parent.writePassword()) //
                .build();
        final String path = url.getPathInBucket();
        bucket.deleteFileNonBlocking(path);
        if (this.parent.isRecursive()) {
            for (final String child : bucket.listContentsRecursively(path)) {
                bucket.deleteFileNonBlocking(asDirectory(path) + child);
            }
        }
        return CommandLine.ExitCode.OK;
    }
}
