package com.exasol.bucketfs.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.concurrent.Callable;

import com.exasol.bucketfs.http.HttpClientBuilder;
import com.exasol.bucketfs.list.BucketContentListing;
import com.exasol.bucketfs.list.BucketListing;
import com.exasol.bucketfs.profile.ProfileProvider;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.bucketfs.url.UriConverter;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * This class implements listing the contents of BucketFS
 */
@Command(name = "ls", description = "List contents of PATH")
// [impl->dsn~list-contents~1]
// [impl->dsn~list-files-and-directories~1]
// [impl->dsn~highlight-type-of-entries~1]
public class ListCommand implements Callable<Integer> {

    private final ProfileProvider profileProvider;
    @Parameters(index = "0", paramLabel = "PATH", description = "path", converter = UriConverter.class)
    private URI uri;

    public ListCommand(final ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    @Override
    public Integer call() throws Exception {
        final BucketFsUrl url = BucketFsUrl.from(this.uri, this.profileProvider.getProfile());
        final HttpClient client = new HttpClientBuilder().build();

        final String bucketName = url.getBucketName();
        final String protocol = url.isTlsEnabled() ? "https" : "http";
        if (bucketName == null) {
            new BucketListing(client, protocol, url.getHost(), url.getPort()).retrieve().forEach(this::print);
        } else {
            new BucketContentListing(client, protocol, url.getHost(), url.getPort(), bucketName).retrieve(url.getPathInBucket())
                    .forEach(this::print);
        }
        return CommandLine.ExitCode.OK;
    }

    // Suppress sonar warning S106 since the purpose of the list command is to print the listing on stdout.
    @SuppressWarnings("java:S106")
    private void print(final String string) {
        System.out.println(string);
    }
}
