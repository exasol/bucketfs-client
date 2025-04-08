package com.exasol.bucketfs.native_image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.exasol.bucketfs.ProcessExecutor;
import com.exasol.bucketfs.url.OsCheck;
import com.exasol.bucketfs.url.OsCheck.OSType;

class NativeImageIT {
    @Test
    void testNativeImage() throws IOException, InterruptedException {
        final String suffix = new OsCheck().getOperatingSystemType() == OSType.WINDOWS ? ".exe" : "";
        final Path executable = Path.of("target/bfsc" + suffix).toAbsolutePath();
        assertTrue(Files.exists(executable),
                "Executable %s does not exist, build it with 'mvn package'".formatted(executable));
        final ProcessExecutor executor = new ProcessExecutor(executable.toString()).run("--help");
        executor.assertProcessFinishes();
        assertAll(() -> assertThat("std out", executor.getStdOut(), startsWith("Usage: ")),
                () -> assertThat("std err", executor.getStdErr(), emptyString()),
                () -> assertThat("exit code", executor.getExitCode(), equalTo(0)));
    }
}
