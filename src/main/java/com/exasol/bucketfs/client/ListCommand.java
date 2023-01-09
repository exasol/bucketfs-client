package com.exasol.bucketfs.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.concurrent.Callable;

import com.exasol.bucketfs.http.HttpClientBuilder;
import com.exasol.bucketfs.list.*;
import com.exasol.bucketfs.profile.Profile;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.bucketfs.url.UriConverter;

import picocli.CommandLine;
import picocli.CommandLine.*;

/**
 * This class implements listing the contents of BucketFS
 */
@Command(name = "ls", description = "List contents of PATH")
// [impl->dsn~list-contents~1]
// [impl->dsn~list-files-and-directories~1]
// [impl->dsn~highlight-type-of-entries~1]
public class ListCommand implements Callable<Integer> {

    @ParentCommand
    BucketFsClient parent;

    @Parameters(index = "0", paramLabel = "PATH", description = "path", converter = UriConverter.class)
    private URI uri;

    @Override
    public Integer call() throws Exception {
        final Profile profile = this.parent.getProfile();
        final BucketFsUrl bucketFsurl = BucketFsUrl.from(this.uri, profile);
        final HttpClient client = new HttpClientBuilder().build();
        final String protocol = bucketFsurl.isTlsEnabled() ? "https" : "http";
        final String bucketName = bucketFsurl.getBucketName();
        final ListingRetriever contentLister = new ListingRetriever(client);

        if (bucketName == null) {
            new BucketService(publicReadUri(protocol, bucketFsurl, ""), contentLister) //
                    .retrieve() //
                    .forEach(this.parent::print);
        } else {
            final String path = bucketFsurl.getPathInBucket();
            // final String password =
            // PasswordReader.forReading(this.parent.requireReadPassword()).readPassword(profile);
            final String password = this.parent.readPassword();
            new BucketContentLister(publicReadUri(protocol, bucketFsurl, bucketName), contentLister, password) //
                    .retrieve(path, this.parent.isRecursive()) //
                    .forEach(this.parent::print);
        }
        return CommandLine.ExitCode.OK;
    }

    private URI publicReadUri(final String protocol, final BucketFsUrl url, final String suffix) {
        return ListingRetriever.publicReadUri(protocol, url.getHost(), url.getPort(), suffix);
    }
}
