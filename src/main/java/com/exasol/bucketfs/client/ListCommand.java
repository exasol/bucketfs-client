package com.exasol.bucketfs.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.Callable;

import com.exasol.bucketfs.ReadEnabledBucket;
import com.exasol.bucketfs.ReadOnlyBucket;
import com.exasol.bucketfs.env.EnvironmentVariables;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.bucketfs.url.UriConverter;
import com.exasol.errorreporting.ExaError;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * This class implements listing the contents of BucketFS
 */
@Command(name = "ls", description = "List contents of PATH")
public class ListCommand implements Callable<Integer> {

    private final EnvironmentVariables env;
    @Parameters(index = "0", paramLabel = "PATH", description = "path", converter = UriConverter.class)
    private URI uri;

    public ListCommand(final EnvironmentVariables env) {
        this.env = env;
    }

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
            return BucketFsUrl.from(this.uri, this.env);
        } catch (final MalformedURLException exception) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-5") //
                    .message("Invalid BucketFS URL: {{url}}", this.uri).toString());
        }
    }
}
