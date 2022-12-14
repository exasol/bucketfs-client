package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKET;
import static com.exasol.bucketfs.BucketConstants.DEFAULT_BUCKETFS;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.exasol.bucketfs.*;
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
                .ipAddress(getHost()) //
                .port(getMappedBucketFsPort()) //
                .serviceName("bfsdefault") //
                .name("default") //
                .writePassword(bucketConfiguration.getWritePassword()) //
                .build();
    }

    public String getDefaultBucketUriToFile(final String filename) {
        return getBucketFsUri(DEFAULT_BUCKETFS, DEFAULT_BUCKET, filename);
    }

    public String getBucketFsUri(final String serviceName, final String bucketName, final String pathInBucket) {
        return "bfs://" + getHost() + ":" + getMappedBucketFsPort() + "/" + bucketName + "/" + pathInBucket;
    }

    public void createFiles(final String... paths) throws BucketAccessException {
        final UnsynchronizedBucket bucket = getDefaultBucket();
        Arrays.stream(paths).forEach(path -> createFile(bucket, path));
        System.out.println("Actual content: " + bucket.listContents());
    }

    private static void createFile(final UnsynchronizedBucket bucket, final String path) {
        try {
            bucket.uploadStringContentNonBlocking(path, path);
        } catch (BucketAccessException | TimeoutException exception) {
            throw new IllegalStateException(exception);
        }
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
