package com.exasol.bucketfs.url;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PathCompleterTest {

    @TempDir
    static Path TEMP;
    static Path FILE;
    static Path FOLDER;

    @BeforeAll
    static void beforeAll() throws IOException {
        FILE = Files.writeString(TEMP.resolve("file.txt"), "content");
        FOLDER = Files.createDirectory(TEMP.resolve("folder"));
    }

    @ParameterizedTest
    @CsvSource(value = { //
            "file.txt, path/,          path/file.txt", //
            "file.txt, path/other.txt, path/other.txt", //
            "file.txt, path,           path", //
    })
    void testCompletePathForFile(final String sourcePath, final String targetPath, final String expected)
            throws IOException {
        final PathCompleter testee = new PathCompleter(TEMP, targetPath);
        final String actual = testee.complete(TEMP.resolve(sourcePath));
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource(value = { //
            "folder, path/,          path/folder/", //
            "folder, path/other.txt, path/other.txt/", //
            "folder, path/other/,    path/other/folder/", //
            "folder, path,           path/", //
    })
    void testCompletePathForFolder(final String sourcePath, final String targetPath, final String expected)
            throws IOException {
        final PathCompleter testee = new PathCompleter(TEMP, targetPath);
        final String actual = testee.complete(TEMP.resolve(sourcePath));
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @ValueSource(strings = { "file.txt", "file.txt/" })
    void testRedundantTrailingSlash(final String filename) throws IOException {
        final Path path = TEMP.resolve(filename);
        assertThat(Files.isDirectory(path), is(false));
        assertThat(Files.exists(path), is(true));
    }

    @Test
    void walkFileTreeSingleFile() throws IOException {
        final SampleFileVisitor visitor = new SampleFileVisitor();
        Files.walkFileTree(FILE, visitor);
        assertThat(visitor.list, contains(FILE));
    }

    @Test
    void walkFileTreeVisitsOnlyFiles() throws IOException {
        final SampleFileVisitor visitor = new SampleFileVisitor();
        Files.walkFileTree(TEMP, visitor);
        assertThat(visitor.list, equalTo(List.of(FILE)));
    }

    static class SampleFileVisitor extends SimpleFileVisitor<Path> {
        List<Path> list = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            this.list.add(file);
            return FileVisitResult.CONTINUE;
        }
    }
}
