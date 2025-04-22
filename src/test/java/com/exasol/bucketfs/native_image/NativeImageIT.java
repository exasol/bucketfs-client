package com.exasol.bucketfs.native_image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.exasol.bucketfs.ProcessExecutor;
import com.exasol.bucketfs.url.OsCheck;
import com.exasol.bucketfs.url.OsCheck.OSType;

@Tag("native-image")
class NativeImageIT {
    @Test
    void testNativeImage() throws IOException, InterruptedException {
        final Path executable = Path.of("target").resolve(getBinaryName()).toAbsolutePath();
        assertTrue(Files.exists(executable),
                "Executable %s does not exist, build it with 'mvn package'".formatted(executable));
        final ProcessExecutor executor = new ProcessExecutor(executable.toString()).run("--help");
        executor.assertProcessFinishes();
        assertAll(() -> assertThat("std out", executor.getStdOut(), startsWith("Usage: ")),
                () -> assertThat("std err", executor.getStdErr(), emptyString()),
                () -> assertThat("exit code", executor.getExitCode(), equalTo(0)));
    }

    private String getBinaryName() {
        final OSType osType = new OsCheck().getOperatingSystemType();
        switch (osType) {
            case WINDOWS:
                return "bfsc-windows_x86_64.exe";
            case LINUX:
                return "bfsc-linux_x86_64";
            case MACOS:
                return "bfsc-osx_x86_64";
            default:
                throw new IllegalStateException("Unsupported OS type %s".formatted(osType));
        }
    }
}
