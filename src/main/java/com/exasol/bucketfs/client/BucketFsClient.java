package com.exasol.bucketfs.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.*;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.exasol.bucketfs.*;
import com.exasol.bucketfs.WriteEnabledBucket.Builder;
import com.exasol.bucketfs.client.PasswordReader.ConsoleReader;
import com.exasol.bucketfs.http.HttpClientBuilder;
import com.exasol.bucketfs.list.ListingRetriever;
import com.exasol.bucketfs.profile.Profile;
import com.exasol.bucketfs.profile.ProfileReader;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.errorreporting.ExaError;

import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

/**
 * This class implements the BucketFS client.
 */
// [impl->dsn~command-line-parsing~1]
@Command( //
        name = "bfsc", //
        mixinStandardHelpOptions = true, //
        description = "Exasol BucketFS client", //
        versionProvider = VersionFromManifest.class, //
        subcommands = { CopyCommand.class, ListCommand.class, DeleteCommand.class })
public class BucketFsClient implements Callable<Integer> {
    @Spec
    private CommandSpec spec;
    @Option(names = { "-r", "--recursive" }, description = "recursive", scope = ScopeType.INHERIT)
    private boolean recursive;
    @Option(names = { "-p", "--profile" }, description = "name of the profile to use", scope = ScopeType.INHERIT)
    private String profileName;
    @Option(names = { "-pw", "--require-read-password" }, //
            description = "whether BFSC should ask for a read password", scope = ScopeType.INHERIT)
    private boolean requireReadPassword;
    @Option(names = { "-c", "--certificate" }, required = false, //
            description = "local path to the server's TLS certificate in case the certificate is not contained in the Java keystore", scope = ScopeType.INHERIT)
    private Path tlsCertificatePath;

    private final ConsoleReader consoleReader;
    private Profile profile;

    public static void main(final String[] arguments) {
        mainWithConsoleReader(PasswordReader.defaultConsoleReader(), arguments);
    }

    static void mainWithConsoleReader(final ConsoleReader consoleReader, final String[] arguments) {
        final CommandLine commandLineClient = new CommandLine(new BucketFsClient(consoleReader)) //
                .setExecutionExceptionHandler(new PrintExceptionMessageHandler());
        final int exitCode = commandLineClient.execute(arguments);
        System.exit(exitCode);
    }

    public BucketFsClient(final ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
    }

    String recursiveOption() {
        return String.join(" or ", this.spec.findOption("-r").names());
    }

    void printWarning(final String message) {
        final CommandLine cl = this.spec.commandLine();
        cl.getErr().println(cl.getColorScheme().errorText(message));
    }

    void print(final String message) {
        final CommandLine cl = this.spec.commandLine();
        cl.getOut().println(message);
    }

    boolean isRecursive() {
        return this.recursive;
    }

    public Profile getProfile() {
        if (this.profile == null) {
            this.profile = new ProfileReader().getProfile(this.profileName);
        }
        return this.profile;
    }

    String readPassword() {
        return PasswordReader.forReading(this.requireReadPassword, this.consoleReader) //
                .readPassword(getProfile());
    }

    String writePassword() {
        return PasswordReader.forWriting(this.consoleReader).readPassword(getProfile());
    }

    UnsynchronizedBucket buildWriteEnabledBucket(final URI destination) {
        final Profile profile = this.getProfile();
        final BucketFsUrl url = BucketFsUrl.from(destination, profile);
        final Builder<? extends Builder<?>> builder = WriteEnabledBucket.builder()
                .useTls(url.isTlsEnabled())
                .host(url.getHost())
                .port(url.getPort())
                .name(url.getBucketName())
                .readPassword(this.readPassword())
                .writePassword(this.writePassword());
        certificate().ifPresent(cert -> builder.certificate(cert)
                .allowAlternativeHostName(url.getHost())
                .allowAlternativeIpAddress(url.getHost()));
        return builder.build();
    }

    ReadOnlyBucket buildReadOnlyBucket(final URI source) {
        final Profile profile = this.getProfile();
        final BucketFsUrl url = BucketFsUrl.from(source, profile);
        final var builder = ReadEnabledBucket.builder()
                .useTls(url.isTlsEnabled())
                .host(url.getHost())
                .port(url.getPort())
                .name(url.getBucketName())
                .readPassword(this.readPassword());
        certificate().ifPresent(cert -> builder.certificate(cert)
                .allowAlternativeHostName(url.getHost())
                .allowAlternativeIpAddress(url.getHost()));
        return builder.build();
    }

    ListingRetriever createListingRetriever(final BucketFsUrl bucketFsUrl) {
        final HttpClientBuilder httpClientBuilder = new HttpClientBuilder();
        certificate().ifPresent(cert -> httpClientBuilder.certificate(cert)
                .allowAlternativeHostName(bucketFsUrl.getHost())
                .allowAlternativeIPAddress(bucketFsUrl.getHost()));
        final HttpClient client = httpClientBuilder.build();
        return new ListingRetriever(client);
    }

    private Optional<X509Certificate> certificate() {
        if (this.tlsCertificatePath == null) {
            return Optional.empty();
        }
        final Path absolutePath = this.tlsCertificatePath.toAbsolutePath();
        if (!Files.exists(absolutePath)) {
            throw new IllegalStateException(ExaError.messageBuilder("E-BFSC-15")
                    .message("TLS certificate does not exist at configured path {{certificate path}}", absolutePath)
                    .toString());
        }
        return Optional.of(readCertificate(absolutePath));
    }

    private X509Certificate readCertificate(final Path path) {
        try (InputStream fis = Files.newInputStream(path)) {
            final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(fis);
        } catch (final IOException | CertificateException exception) {
            throw new IllegalStateException(ExaError.messageBuilder("E-BFSC-16")
                    .message("Error reading TLS certificate from file {{certificate path}}: {{error message}}", path,
                            exception.getMessage())
                    .toString(), exception);
        }
    }

    @Override
    public Integer call() {
        throw new ParameterException(this.spec.commandLine(), "Missing required subcommand");
    }
}
