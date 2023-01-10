package com.exasol.bucketfs.url;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.ReadOnlyBucket;

class BucketFsLocationTest {

    enum Type {
        DIR(true, false), //
        FILE(false, true), //
        BOTH(true, true), //
        NONE(false, false);

        final boolean dir;
        final boolean file;

        private Type(final boolean dir, final boolean file) {
            this.dir = dir;
            this.file = file;
        }

        public boolean exists() {
            return this.dir || this.file;
        }
    }

    static ReadOnlyBucket BUCKET;

    @BeforeAll
    static void beforeAll() throws BucketAccessException {
        BUCKET = mock(ReadOnlyBucket.class);
        doReturn(List.of("both", "both/", "folder/", "a.txt")).when(BUCKET).listContents("");
        doReturn(List.of("aa.txt", "sub/", "fboth", "fboth/")).when(BUCKET).listContents("folder");
    }

    @ParameterizedTest
    @MethodSource("com.exasol.bucketfs.url.BucketFsLocationTest#provideParameters")
    void testAll1(final String path, final Type expected) throws Exception {
        final BucketFsLocation testee = BucketFsLocation.from(BUCKET, path);
        assertThat(testee.exists(), is(expected.exists()));
        assertThat(testee.isRegularFile(), is(expected.file));
        assertThat(testee.isDirectory(), is(expected.dir));
    }

    static Stream<Arguments> provideParameters() {
        return Stream.concat(stream(""), stream("/"));
    }

    private static Stream<Arguments> stream(final String prefix) {
        return Stream.of( //
                Arguments.of(prefix + "non-existing-file", Type.NONE), //
                Arguments.of(prefix + "ne-folder/", Type.NONE), //
                Arguments.of(prefix + "folder/ne-file", Type.NONE), //
                Arguments.of(prefix + "folder/", Type.DIR), //
                Arguments.of(prefix + "folder", Type.DIR), //
                Arguments.of(prefix + "folder/sub/", Type.DIR), //
                Arguments.of(prefix + "folder/sub", Type.DIR), //
                Arguments.of(prefix + "a.txt/", Type.NONE), //
                Arguments.of(prefix + "a.txt", Type.FILE), //
                Arguments.of(prefix + "folder/aa.txt/", Type.NONE), //
                Arguments.of(prefix + "folder/aa.txt", Type.FILE), //
                Arguments.of(prefix + "both/", Type.DIR), //
                Arguments.of(prefix + "both", Type.BOTH), //
                Arguments.of(prefix + "folder/fboth/", Type.DIR), //
                Arguments.of(prefix + "folder/fboth", Type.BOTH) //
        );
    }
}
