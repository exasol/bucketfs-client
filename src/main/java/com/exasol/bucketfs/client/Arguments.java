package com.exasol.bucketfs.client;

import java.net.URL;

import picocli.CommandLine.Parameters;

class Arguments {
    @Parameters(paramLabel = "SOURCE", description = "source from which files are copied")
    URL source;

    @Parameters(paramLabel = "DEST", description = "")
    URL destination;
}