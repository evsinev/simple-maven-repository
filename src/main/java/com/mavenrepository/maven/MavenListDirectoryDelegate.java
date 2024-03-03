package com.mavenrepository.maven;

import java.io.File;

public class MavenListDirectoryDelegate {

    public String listDirectory(MavenFile aFile) {

        StringBuilder sb = new StringBuilder();
        sb.append("""
                <!doctype html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <script src="https://cdn.tailwindcss.com"></script>
                </head>
                <body><pre class='m-8'>
                <a href="../">../</a>
                """
        );
        File[] files = safe(aFile.getLocalFile().listFiles());

        for (File file : files) {
            String link = file.isDirectory() ? file.getName() + "/" : file.getName();
            sb.append(String.format("<a href=\"%s\">%s</a>\n", link, link));
        }
        sb.append("</pre></body></html>");
        return sb.toString();

    }

    private File[] safe(File[] files) {
        return files == null ? new File[0] : files;
    }
}
