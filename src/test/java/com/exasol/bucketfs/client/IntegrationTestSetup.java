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

import org.testcontainers.containers.Container;
import org.testcontainers.containers.ExecConfig;
import org.testcontainers.containers.ExecConfig.ExecConfigBuilder;

import com.exasol.bucketfs.*;
import com.exasol.bucketfs.http.HttpClientBuilder;
import com.exasol.bucketfs.list.BucketContentLister;
import com.exasol.bucketfs.list.ListingRetriever;
import com.exasol.config.BucketConfiguration;
import com.exasol.containers.ExasolContainer;
import com.exasol.containers.ExasolService;

public class IntegrationTestSetup {

    private static final int BucketFsTlsPort = 2581;
    private static final Logger LOGGER = Logger.getLogger(IntegrationTestSetup.class.getName());

    private final ExasolContainer<? extends ExasolContainer<?>> exasol = new ExasolContainer<>()//
            .withExposedPorts(BucketFsTlsPort, 20000, 20001, 20002, 20003, 2580, 8563) //
            .withRequiredServices(ExasolService.BUCKETFS)//
            .withReuse(true); //

    public void stop() {
        this.exasol.stop();
    }

    public IntegrationTestSetup() {
        this.exasol.start();

        setBucketFsPortsAndEnableBucketFsTls();

    }

    private void setBucketFsPortsAndEnableBucketFsTls() {
        try {

            stopDatabase();
            alterBucketFsConfig();
            startDatabase();
            //String stdout;
            //int exitCode;

//            lsResult = exasol.execInContainer("/bin/bash", "-c",
//                    "confd_client -c bucketfs_modify -a '{\"bucketfs_name\": \"bfsdefault\", \"http_port\": 2580, \"https_port\": 2581}'");
//            stdout = lsResult.getStdout();
//            exitCode = lsResult.getExitCode();
//            lsResult = exasol.execInContainer("/bin/bash", "-c",
//                    "confd_client -c db_start -A '{ \"db_name\": \"DB1\"}'");
//            stdout = lsResult.getStdout();
//            exitCode = lsResult.getExitCode();
//            lsResult = exasol.execInContainer("/bin/bash",  "-c", "printenv");
//            stdout = lsResult.getStdout();
//            exitCode = lsResult.getExitCode();
//            lsResult = exasol.execInContainer("/bin/bash",  "-c", "which confd_client");
//            stdout = lsResult.getStdout();
//            exitCode = lsResult.getExitCode();
            // Container.ExecResult lsResult = exasol.execInContainer("/bin/bash", "-c","confd_client", "-c", "db_stop",
            // "-A", "'{ \"db_name\": \"DB1\"}'");
            // String stdout = lsResult.getStdout();
            // int exitCode = lsResult.getExitCode();
            // lsResult = exasol.execInContainer("/bin/bash", "-c", "confd_client", "-c","bucketfs_modify", "-a",
            // "db_start", "-A", "'{\"bucketfs_name\": \"bfsdefault\", \"http_port\": 2580, \"https_port\": 2581}'");
            // stdout = lsResult.getStdout();
            // exitCode = lsResult.getExitCode();
            // lsResult = exasol.execInContainer("/bin/bash", "-c", "confd_client", "-c", "db_start", "-A", "'{
            // \"db_name\": \"DB1\"}'");
            // stdout = lsResult.getStdout();
            // exitCode = lsResult.getExitCode();

        } catch (final IOException e) {
            throw new RuntimeException(e);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopDatabase() throws IOException, InterruptedException {
        String cmd = "confd_client -c db_stop -A '{ \"db_name\": \"DB1\"}'";
        executeCommandInExasolDockerDb(cmd);
    }
    private void alterBucketFsConfig() throws IOException, InterruptedException {
        //String cmd = "confd_client -c bucketfs_modify -a '{\"bucketfs_name\": \"bfsdefault\", \"http_port\": 2580, \"https_port\": 2581}'";
        String cmd = "confd_client -c bucketfs_modify -a '{\"bucketfs_name\": \"bfsdefault\", \"https_port\": 2581}'";
        executeCommandInExasolDockerDb(cmd);
    }
    private void startDatabase() throws IOException, InterruptedException {
        String cmd = "confd_client -c db_start -A '{ \"db_name\": \"DB1\"}'";
        executeCommandInExasolDockerDb(cmd);
    }

    private void executeCommandInExasolDockerDb(String cmd) throws IOException, InterruptedException {
        final ExecConfigBuilder ecb = ExecConfig.builder() //
                //.user("root") //
                //.envVars(Map.of("COS_DIRECTORY","/usr/opt/EXASuite-7/EXAClusterOS-7.1.25"))
                //.workDir("")
                .command(new String[] { "/bin/bash", "-c", "source /root/.bashrc ; " + cmd}); //

        Container.ExecResult cmdExecResult = exasol.execInContainer(ecb.build());

        String stdout = cmdExecResult.getStdout();
        int exitCode = cmdExecResult.getExitCode();
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

    public Integer getMappedBucketFsTlsPort() {
        return this.exasol.getMappedPort(2581);
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

    public String getDefaultBucketTlsUriToFile(final String filename) {
        return getBucketTlsFsUri(DEFAULT_BUCKETFS, DEFAULT_BUCKET, filename);
    }

    public String getBucketFsUri(final String serviceName, final String bucketName, final String pathInBucket) {
        return "bfs://" + getHost() + ":" + getMappedBucketFsPort() + "/" + bucketName + "/" + pathInBucket;
    }

    public String getBucketTlsFsUri(final String serviceName, final String bucketName, final String pathInBucket) {
        return "bfss://" + getHost() + ":" + getMappedBucketFsTlsPort() + "/" + bucketName + "/" + pathInBucket;
        // return "bfss://" + "127.0.0.1:36551" + "/" + bucketName + "/" + pathInBucket;
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
