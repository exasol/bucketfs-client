package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.list.ListingRetriever.removeLeadingSeparator;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.ReadOnlyBucket;
import com.exasol.bucketfs.http.HttpClientBuilder;
import com.exasol.bucketfs.list.BucketContentLister;
import com.exasol.bucketfs.list.ListingRetriever;
import com.exasol.bucketfs.url.BucketFsUrl;

public class ListingProvider {

    private final ReadOnlyBucket bucket;
    private final BucketFsUrl source;

    public ListingProvider(final ReadOnlyBucket bucket, final BucketFsUrl source) {
        this.bucket = bucket;
        this.source = source;
    }

    // proposal: enhance interface of bucketfs-java:ReadEnableBucket and move this method there
    List<String> list(final String path, final boolean recursive) throws BucketAccessException {
        final HttpClient client = new HttpClientBuilder().build();
        final ListingRetriever contentLister = new ListingRetriever(client);
        final String protocol = this.source.isTlsEnabled() ? "https" : "http";
        final URI uri = ListingRetriever.publicReadUri(protocol, this.source.getHost(), this.source.getPort(),
                this.source.getBucketName());
        return new BucketContentLister(uri, contentLister, this.bucket.getReadPassword()) //
                .retrieve(removeLeadingSeparator(path), recursive);
    }
}
