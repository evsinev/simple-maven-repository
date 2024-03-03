package com.mavenrepository;

import com.mavenrepository.maven.MavenFile;
import com.mavenrepository.maven.MavenGetFileDelegate;
import com.mavenrepository.maven.MavenPutFileDelegate;
import com.mavenrepository.maven.config.MavenConfig;
import com.mavenrepository.maven.MavenListDirectoryDelegate;
import com.mavenrepository.maven.auth.AuthenticationHandler;
import com.mavenrepository.maven.auth.RepositoryAccess;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class MavenController implements HttpService {

    private static final Logger LOG = LoggerFactory.getLogger( MavenController.class );

    private final MavenConfig config;
    private final File        baseDir;

    private final MavenPutFileDelegate       putFileDelegate       = new MavenPutFileDelegate();
    private final MavenGetFileDelegate       getFileDelegate       = new MavenGetFileDelegate();
    private final MavenListDirectoryDelegate listDirectoryDelegate = new MavenListDirectoryDelegate();

    public MavenController(MavenConfig config) {
        this.config = config;
        baseDir = config.getBaseDir();
    }

    @Override
    public void routing(HttpRules aRules) {
        aRules
            .put("/*", new AuthenticationHandler(RepositoryAccess.WRITE, config, this::putFile))
            .get("/*", new AuthenticationHandler(RepositoryAccess.READ, config, this::getFile))
        ;
    }

    private void getFile(ServerRequest aRequest, ServerResponse aResponse) {
        LOG.debug("Get Path {}", aRequest.path().path());
        MavenFile file = getMavenFile(aRequest);
        if(!file.getLocalFile().exists()) {
            aResponse.status(Status.NOT_FOUND_404)
                    .send();
            return;
        }

        if(file.getLocalFile().isDirectory()) {
            aResponse
                    .header(HeaderNames.CONTENT_TYPE, "text/html")
                    .send(listDirectoryDelegate.listDirectory(file));
            return;
        }

        try(OutputStream out = aResponse.outputStream()) {
            getFileDelegate.getFile(file, out);
        } catch (Exception e) {
            LOG.error("Cannot process PUT {}", file, e);
            aResponse.status(500).send("Error " + e.getMessage());
        }
    }

    private void putFile(ServerRequest aRequest, ServerResponse aResponse) {
        MavenFile file = getMavenFile(aRequest);
        LOG.debug("Put file {}", file);
        try(InputStream in = aRequest.content().inputStream()) {
            putFileDelegate.putFile(file, in);
            aResponse.send();
        } catch (Exception e) {
            LOG.error("Cannot process PUT {}", file, e);
            aResponse.status(500).send("Error " + e.getMessage());
        }
    }

    private MavenFile getMavenFile(ServerRequest aRequest) {
        String path = aRequest.path().path();
        LOG.debug("Path {}", path);
        return MavenFile.createMavenFile(baseDir, path, aRequest.path().segments());
    }
}
