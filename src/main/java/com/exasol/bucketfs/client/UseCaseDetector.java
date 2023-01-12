package com.exasol.bucketfs.client;

import static com.exasol.bucketfs.url.BucketFsUrl.PATH_SEPARATOR;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.exasol.bucketfs.BucketAccessException;
import com.exasol.bucketfs.url.BucketFsLocation;
import com.exasol.bucketfs.url.BucketFsUrl;
import com.exasol.errorreporting.ExaError;

class UseCaseDetector {

    enum UseCase {
        FILE, DIRECTORY;
    }

    interface RecurseOption {
        static RecurseOption from(final Supplier<String> nameGetter, final BooleanSupplier valueGetter) {
            return new RecurseOption() {
                @Override
                public String name() {
                    return nameGetter.get();
                }

                @Override
                public boolean value() {
                    return valueGetter.getAsBoolean();
                }
            };
        }

        String name();

        boolean value();
    }

    private final BucketFsUrl source;
    private final Path destination;
    private final RecurseOption recurseOption;

    UseCaseDetector(final BucketFsUrl source, final Path destination, final RecurseOption recurseOption) {
        this.source = source;
        this.destination = destination;
        this.recurseOption = recurseOption;
    }

    // [impl->dsn~download-ambigue-file~1]
    // [impl->dsn~download-ambigue-directory-recursively~1]
    // [impl->dsn~download-ambigue-entry-recursively~1]
    UseCase detect(final BucketFsLocation location) throws BucketAccessException {
        if (!location.exists()) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-10") //
                    .message("Cannot download {{source|q}}: No such file or directory.", this.source) //
                    .toString());
        }
        if (location.isDirectory() && location.isRegularFile()) {
            if (!this.recurseOption.value()) {
                // assume user wants to download regular file
                // ignore directory with identical name
                return UseCase.FILE;
            }
            if (!location.hasDirectorySyntax()) {
                throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-11") //
                        .message("Cannot download regular file and directory with identical name.") //
                        .mitigation("Append trailing path separator {{separator}}" //
                                + " to {{source|q}} to download directory recursively.", //
                                PATH_SEPARATOR, this.source) //
                        .mitigation("Remove option {{option|uq}} to download the regular file.", //
                                this.recurseOption.name()) //
                        .toString());
            }
        }
        if (!location.isDirectory()) {
            return UseCase.FILE; // ignore potential flag -r
        }
        if (!this.recurseOption.value()) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-12") //
                    .message("Cannot download directory {{source|q}}.", this.source) //
                    .mitigation("Specify option {{option|uq}} to download directories.", //
                            this.recurseOption.name()) //
                    .toString());
        }
        if (Files.isRegularFile(this.destination)) {
            throw new BucketFsClientException(ExaError.messageBuilder("E-BFSC-13") //
                    .message("Cannot overwrite local non-directory {{destination}}" //
                            + " with download from directory {{source|q}}.", //
                            this.destination, this.source) //
                    .toString());
        }
        return UseCase.DIRECTORY;
    }
}
