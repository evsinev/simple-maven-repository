package com.mavenrepository.maven;

import java.io.FileInputStream;
import java.io.OutputStream;

public class MavenGetFileDelegate {

    public void getFile(MavenFile aFile, OutputStream aInputStream) {

        try (FileInputStream in = new FileInputStream(aFile.getLocalFile())) {
            in.transferTo(aInputStream);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot copy " + aFile, e);
        }
    }
}
