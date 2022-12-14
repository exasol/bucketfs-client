package com.exasol.bucketfs.client;

import java.net.URI;
import java.util.concurrent.Callable;

import com.exasol.bucketfs.UnsynchronizedBucket;
import com.exasol.bucketfs.WriteEnabledBucket;
import com.exasol.bucketfs.profile.Profile;
import com.exasol.bucketfs.profile.ProfileProvider;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.bucketfs.url.UriConverter;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * This class implements listing the contents of BucketFS
 */
@Command(name = "rm", description = "Remove file PATH from BucketFS")
// [impl->dsn~delete-file~1]
public class DeleteCommand implements Callable<Integer> {

    private final ProfileProvider profileProvider;
    @Parameters(index = "0", paramLabel = "PATH", description = "path", converter = UriConverter.class)
    private URI uri;

    public DeleteCommand(final ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    @Override
    public Integer call() throws Exception {
        final Profile profile = this.profileProvider.getProfile();
        final BucketFsUrl url = BucketFsUrl.from(this.uri, profile);
        final String password = PasswordReader.readPassword(profile);
        final UnsynchronizedBucket bucket = WriteEnabledBucket.builder() //
                .ipAddress(url.getHost()) //
                .port(url.getPort()) //
                .name(url.getBucketName()) //
                .writePassword(password) //
                .build();
        bucket.deleteFileNonBlocking(url.getPathInBucket());
        return CommandLine.ExitCode.OK;
    }
}
