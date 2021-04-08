package com.exasol.bucketfs.client;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "bfs", subcommands = { CopyCommand.class }, description = "Exasol BucketFS client")
public class BucketFsClient implements Callable<Integer> {

    public static void main(final String[] arguments) {
        new CommandLine(new BucketFsClient()).execute(arguments);
    }

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}