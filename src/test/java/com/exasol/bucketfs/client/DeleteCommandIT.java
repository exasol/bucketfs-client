package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.url.BucketFsUrl.PATH_SEPARATOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.itsallcode.junit.sysextensions.AssertExit.assertExitWithStatus;
import static picocli.CommandLine.ExitCode.OK;

import java.util.*;

import org.itsallcode.junit.sysextensions.ExitGuard;
import org.itsallcode.junit.sysextensions.SystemErrGuard;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.exasol.bucketfs.BucketAccessException;

@ExtendWith(ExitGuard.class)
@ExtendWith(SystemErrGuard.class)
// [itest->dsn~delete-file~1]
class DeleteCommandIT {

    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();

    @AfterAll
    static void afterAll() {
        SETUP.close();
    }

    @Test
    void testFileInRoot() throws Exception {
        SETUP.createRemoteFiles("delete.txt");
        verifyDelete("delete.txt");
    }

    @Test
    void testFileInDirectory() throws Exception {
        // "aa.txt" in same folder ensures folder to still be observable after
        // file "delete.txt" has been deleted. Otherwise folder would no longer be observable
        // as BucketFS only manages a list of flat file paths but not a hierarchy of folders and files.
        SETUP.createRemoteFiles("folder/delete.txt", "folder/aa.txt");
        verifyDelete("folder/delete.txt");
    }

    @Test
    void testNonRecursiveSkipsDirectory() throws Exception {
        SETUP.createRemoteFiles("folder2/any.txt");
        assertExitWithStatus(OK, () -> createClient("rm", "folder2").run());
        final List<String> actual = SETUP.getDefaultBucket().listContents("");
        assertThat(actual, hasItem("folder2/"));
    }

    // [itest->dsn~delete-ambigue-entry-non-recursively~1]
    @Test
    void testNonRecursiveAmbigue() throws Exception {
        SETUP.createRemoteFiles("ambigue/any.txt", "ambigue");
        assertExitWithStatus(OK, () -> createClient("rm", "ambigue").run());
        final List<String> actual = SETUP.getDefaultBucket().listContents("");
        assertThat(actual, hasItem("ambigue/"));
        assertThat(actual, not(hasItem("ambigue")));
    }

    @Test
    void testRecursive() throws Exception {
        SETUP.createRemoteFiles("delete/d1.txt", "delete/d2.txt");
        verifyDelete(createClient("rm", "-r", "delete"), "", "delete/");
    }

    // [itest->dsn~delete-ambigue-entry-recursively~1]
    @Test
    void testRecursiveAmbigue() throws Exception {
        SETUP.createRemoteFiles("ambigue-delete/ad.txt", "ambigue-delete");
        assertExitWithStatus(OK, () -> createClient("rm", "-r", "ambigue-delete").run());
        final List<String> actual = SETUP.getDefaultBucket().listContents("");
        assertThat(actual, not(hasItem("ambigue-delete/")));
        assertThat(actual, not(hasItem("ambigue-delete")));
    }

    @Test
    // [itest->dsn~no-error-when-deleting-a-non-existing-file~1]
    void testDeleteNonExistingFile() throws BucketAccessException {
        verifyDelete("non-existing-file.txt");
    }

    private void verifyDelete(final String pathInBucket) throws BucketAccessException {
        String folder = "";
        String filename = pathInBucket;
        final int i = pathInBucket.lastIndexOf(PATH_SEPARATOR);
        if (i >= 0) {
            filename = pathInBucket.substring(i + 1);
            folder = pathInBucket.substring(0, i);
        }
        verifyDelete(createClient("rm", pathInBucket), folder, filename);
    }

    private void verifyDelete(final BFSC client, final String list, final String missing) throws BucketAccessException {
        assertExitWithStatus(OK, client::run);
        final List<String> actual = SETUP.getDefaultBucket().listContents(list);
        assertThat(actual, not(hasItem(missing)));
    }

    private BFSC createClient(final String... args) {
        final ArrayList<String> argList = new ArrayList<>(args.length + 2);
        Arrays.stream(args).forEach(argList::add);
        final String path = args[args.length - 1];
        argList.set(args.length - 1, SETUP.getDefaultBucketUriToFile(path));
        SETUP.getTlsCertificatePath().ifPresent(certPath -> {
            argList.add("--certificate");
            argList.add(certPath.toString());
        });
        System.out.println(argList);
        return BFSC.create(argList.toArray(new String[0])).feedStdIn(SETUP.getDefaultBucket().getWritePassword());
    }
}
