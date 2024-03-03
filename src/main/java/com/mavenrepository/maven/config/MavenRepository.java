package com.mavenrepository.maven.config;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Data
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Builder
public class MavenRepository {

    String       name;
    List<String> read;
    List<String> full;

}
