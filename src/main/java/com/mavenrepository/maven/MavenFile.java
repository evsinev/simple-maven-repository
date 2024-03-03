package com.mavenrepository.maven;

import io.helidon.common.uri.UriPathSegment;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Builder
public class MavenFile {
    String httpPath;
    File   localFile;
    File   localDir;

    public static MavenFile createMavenFile(File aBaseDir, String aHttpPath, List<UriPathSegment> aSegments) {
        File file = aBaseDir;
        for (UriPathSegment segment : aSegments) {
            file = new File(file, segment.value());
        }

        return MavenFile.builder()
                .httpPath(aHttpPath)
                .localFile(file)
                .localDir(file.getParentFile())
                .build();
    }

    public void createDir() {
        try {
            Files.createDirectories(localDir.toPath());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create dir " + localDir.getAbsolutePath(), e);
        }
    }
}
