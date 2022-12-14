package com.exasol.bucketfs.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.itsallcode.junit.sysextensions.AssertExit.assertExitWithStatus;
import static picocli.CommandLine.ExitCode.OK;

import java.util.List;

import org.itsallcode.junit.sysextensions.ExitGuard;
import org.itsallcode.junit.sysextensions.SystemErrGuard;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.exasol.bucketfs.BucketAccessException;

@ExtendWith(ExitGuard.class)
@ExtendWith(SystemErrGuard.class)
//[itest->dsn~delete-file~1]
class DeleteCommandIT {

    private static final IntegrationTestSetup SETUP = new IntegrationTestSetup();

    @AfterAll
    static void afterAll() {
        SETUP.stop();
    }

    @Test
    void fileInRoot() throws BucketAccessException {
        SETUP.createFiles("delete.txt");
        verifyDelete("delete.txt");
    }

    @Test
    void fileInFolder() throws BucketAccessException {
        SETUP.createFiles("folder/delete.txt");
        verifyDelete("folder/delete.txt");
    }

    // which results do we expect here?
    // should BFSC inspect the listing before deleting and report an error if file cannot be found?
    /**
     * Unfortunately BucketFS does only support non-blocking delete operation.
     * <p>
     * The only option for BFSC to detect whether delete was successful would be to retrieve the listing of the bucket
     * contents and to check whether it contains the file to be deleted before and after the delete operation.
     * </p>
     * <p>
     * Additionally BFSC does not know how long to wait after deleting the file before it can expect the listing to not
     * contain the file anymore.
     * </p>
     * <p>
     * In summary inspecting the listing requires additional effort, additional HTTP communication, and results are
     * unreliable.
     * </p>
     * <p>
     * So In summary inspecting the listing requires additional effort, additional HTTP communication, and results are
     * unreliable.
     * </p>
     *
     *
     * @throws BucketAccessException in case of problems during deleting the file
     */
    @Test
    void deleteNonExistingFile() throws BucketAccessException {
        verifyDelete("non-existing-file.txt");
    }

    private void verifyDelete(final String pathInBucket) throws BucketAccessException {
        final String path = SETUP.getDefaultBucketUriToFile(pathInBucket);
        final String password = SETUP.getDefaultBucket().getWritePassword();
        final BFSC client = BFSC.create("rm", path).feedStdIn(password).catchStdout();
        assertExitWithStatus(OK, () -> client.run());
        final int i = pathInBucket.lastIndexOf("/");
        String folder = "";
        String filename = pathInBucket;
        if (i >= 0) {
            filename = pathInBucket.substring(i + 1);
            folder = pathInBucket.substring(0, i);
        }
        final List<String> actual = SETUP.getDefaultBucket().listContents(folder);
        assertThat(actual.contains(filename), is(false));
    }
}
