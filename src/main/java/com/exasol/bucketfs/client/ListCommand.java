package com.exasol.bucketfs.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.Callable;

import com.exasol.bucketfs.ReadEnabledBucket;
import com.exasol.bucketfs.ReadOnlyBucket;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.bucketfs.url.UriConverter;
import com.exasol.errorreporting.ExaError;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "ls", description = "List contents of PATH")
public class ListCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "PATH", description = "path", converter = UriConverter.class)
    private URI path;

    @Override
    public Integer call() throws Exception {
        final BucketFsUrl url = bucketFsUrl();
        final ReadOnlyBucket bucket = ReadEnabledBucket.builder() //
                .ipAddress(url.getHost()) //
                .port(url.getPort()) //
                .name(url.getBucketName()) //
                .build();
        bucket.listContents(url.getPathInBucket()).stream() //
                .forEach(e -> System.out.println(e));
        return CommandLine.ExitCode.OK;
    }

    private BucketFsUrl bucketFsUrl() {
        try {
            return BucketFsUrl.create(this.path);
        } catch (final MalformedURLException exception) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-5") //
                    .message("Invalid BucketFS URL: {{url}}", this.path).toString());
        }
    }
}
