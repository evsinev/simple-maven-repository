package com.mavenrepository.maven.config;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Builder
public class MavenConfig {

    File            baseDir;

    List<MavenUser> users;

    List<MavenRepository> repositories;

}
