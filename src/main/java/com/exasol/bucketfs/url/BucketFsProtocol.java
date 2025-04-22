package com.exasol.bucketfs.url;

import java.util.Arrays;

import com.exasol.errorreporting.ExaError;

/**
 * BucketFS protocol.
 */
public enum BucketFsProtocol {
    /** Unencrypted protocol via HTTP */
    BFS("bfs"),
    /** Encrypted protocol via HTTPS */
    BFSS("bfss");

    private final String name;

    BucketFsProtocol(final String name) {
        this.name = name;
    }

    /** @return protocol name ({@code bfs} or {@code bfss}) */
    public String getName() {
        return name;
    }

    public boolean isTlsEnabled() {
        return this == BFSS;
    }

    public static BucketFsProtocol forName(final String name) {
        return Arrays.stream(values()).filter(p -> p.getName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ExaError.messageBuilder("E-BFSC-17")
                        .message("Unsupported BucketFS protocol {{protocol name}}.", name)
                        .mitigation("Use one of {{bfs}} or {{bfss}}.", BFS.getName(), BFSS.getName()).toString()));
    }
}
