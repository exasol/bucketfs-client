package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKET;
import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKETFS;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.exasol.bucketfs.*;
import com.exasol.bucketfs.http.HttpClientBuilder;
import com.exasol.bucketfs.list.BucketContentLister;
import com.exasol.bucketfs.list.ListingRetriever;
import com.exasol.config.BucketConfiguration;
import com.exasol.containers.ExasolContainer;
import com.exasol.containers.ExasolService;

public class IntegrationTestSetup {
    private static final Logger LOGGER = Logger.getLogger(IntegrationTestSetup.class.getName());

    private final ExasolContainer<? extends ExasolContainer<?>> exasol = new ExasolContainer<>()//
            .withRequiredServices(ExasolService.BUCKETFS).withReuse(true);

    public void stop() {
        this.exasol.stop();
    }

    public IntegrationTestSetup() {
        this.exasol.start();
    }

    public void cleanBucketFS() throws BucketAccessException {
        final UnsynchronizedBucket bucket = getDefaultBucket();
        bucket.listContents().stream() //
                .filter(Predicate.not("EXAClusterOS/"::equals)) //
                .forEach(path -> delete(bucket, path));
    }

    private void delete(final UnsynchronizedBucket bucket, final String path) {
        try {
            LOGGER.fine("deleting " + path);
            bucket.deleteFileNonBlocking(path);
        } catch (final BucketAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public Integer getMappedBucketFsPort() {
        return this.exasol.getMappedPort(this.exasol.getDefaultInternalBucketfsPort());
    }

    public String getHost() {
        return this.exasol.getHost();
    }

    public UnsynchronizedBucket getDefaultBucket() {
        final BucketConfiguration bucketConfiguration = this.exasol.getClusterConfiguration() //
                .getBucketFsServiceConfiguration(DEFAULT_BUCKETFS) //
                .getBucketConfiguration(DEFAULT_BUCKET);
        return WriteEnabledBucket.builder() //
                .host(getHost()) //
                .port(getMappedBucketFsPort()) //
                .serviceName("bfsdefault") //
                .name("default") //
                .writePassword(bucketConfiguration.getWritePassword()) //
                .readPassword(bucketConfiguration.getReadPassword()) //
                .build();
    }

    public String getDefaultBucketUriToFile(final String filename) {
        return getBucketFsUri(DEFAULT_BUCKETFS, DEFAULT_BUCKET, filename);
    }

    public String getBucketFsUri(final String serviceName, final String bucketName, final String pathInBucket) {
        return "bfs://" + getHost() + ":" + getMappedBucketFsPort() + "/" + bucketName + "/" + pathInBucket;
    }

    public static List<Path> createLocalFiles(final Path folder, final String... filenames) {
        return Arrays.stream(filenames) //
                .map(name -> createLocalFile(folder.resolve(name), content(name))) //
                .collect(Collectors.toList());
    }

    public static Path createLocalFile(final Path file, final String content) {
        try {
            return Files.writeString(file, content);
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    public void createRemoteFiles(final String... paths) throws BucketAccessException, InterruptedException {
        final UnsynchronizedBucket bucket = getDefaultBucket();
        final List<String> list = listAll(bucket);
        for (final String path : paths) {
            if (!list.contains(path)) {
                createRemoteFile(bucket, path);
                waitUntilObjectSynchronized();
            }
        }
        LOGGER.fine("IntegrationTestSetup.createFiles(): Actual content: " + listAll(bucket));
    }

    private void createRemoteFile(final UnsynchronizedBucket bucket, final String remote) {
        try {
            bucket.uploadStringContentNonBlocking(content(remote), remote);
        } catch (BucketAccessException | TimeoutException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static String content(final String remote) {
        return "content of file " + remote;
    }

    public List<String> listAll(final UnsynchronizedBucket bucket) throws BucketAccessException {
        final HttpClient client = new HttpClientBuilder().build();
        final ListingRetriever contentLister = new ListingRetriever(client);
        final URI uri = ListingRetriever.publicReadUri("http", getHost(), getMappedBucketFsPort(),
                bucket.getBucketName());
        return new BucketContentLister(uri, contentLister, bucket.getReadPassword()).retrieve("", true);
    }

    public void uploadStringContent(final String content, final String pathInBucket)
            throws InterruptedException, BucketAccessException, TimeoutException {
        getDefaultBucket().uploadStringContentNonBlocking(content, pathInBucket);
        waitUntilObjectSynchronized();
    }

    @SuppressWarnings("java:S2925")
    public void waitUntilObjectSynchronized() throws InterruptedException {
        Thread.sleep(500);
    }
}
