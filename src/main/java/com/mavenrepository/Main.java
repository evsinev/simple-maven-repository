
package com.mavenrepository;


import com.mavenrepository.maven.config.MavenConfig;
import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    public static void main(String[] args) {

        LogConfig.configureRuntime();

        Config config = Config.create();
        Config.global(config);

        MavenConfig mavenConfig = config.get("maven").as(MavenConfig.class).get();
        LOG.debug("Maven config {}", mavenConfig);

        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .routing(builder -> builder
                        .register("/maven", new MavenController(mavenConfig))
                        .get("/health", (req, res) -> res.send("ok"))
                )
                .build()
                .start();


        LOG.debug("Server started");

    }

}