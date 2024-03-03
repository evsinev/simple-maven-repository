package com.mavenrepository.maven;

import java.io.FileOutputStream;
import java.io.InputStream;

public class MavenPutFileDelegate {

    public void putFile(MavenFile aFile, InputStream aInputStream) {
        aFile.createDir();
        try (FileOutputStream out = new FileOutputStream(aFile.getLocalFile())) {
            aInputStream.transferTo(out);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot copy " + aFile, e);
        }
    }
}
