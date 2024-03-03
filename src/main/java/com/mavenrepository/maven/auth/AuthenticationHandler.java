package com.mavenrepository.maven.auth;

import com.mavenrepository.maven.config.MavenConfig;
import com.mavenrepository.maven.config.MavenRepository;
import com.mavenrepository.maven.config.MavenUser;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.http.RoutedPath;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AuthenticationHandler implements Handler {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationHandler.class);

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private final RepositoryAccess access;
    private final MavenConfig      mavenConfig;
    private final Handler          handler;

    public AuthenticationHandler(RepositoryAccess access, MavenConfig mavenConfig, Handler handler) {
        this.access      = access;
        this.mavenConfig = mavenConfig;
        this.handler     = handler;
    }

    @Override
    public void handle(ServerRequest aRequest, ServerResponse aResponse) throws Exception {
        try {
            MavenUser mavenUser = checkUser(aRequest);
            checkRepositoryAccess(mavenUser, aRequest.path());
        } catch (AuthException e) {
            LOG.warn("Authenticated {}", e.getMessage());
            aResponse.status(e.getStatus());
            e.getHeader().ifPresent(aResponse::header);
            aResponse.send(e.getBody());
            return;
        }

        handler.handle(aRequest, aResponse);
    }

    private MavenUser checkUser(ServerRequest aRequest) throws AuthException {
        if (!isAuthenticated(aRequest)) {
            throw new AuthException(
                    Status.UNAUTHORIZED_401.code()
                    , "No Authorization header"
                    , HeaderValues.create("WWW-Authenticate", "Basic")
            );
        }

        String authHeader = aRequest.headers().get(HeaderNames.AUTHORIZATION).get();

        if (!authHeader.startsWith("Basic ")) {
            throw new AuthException(471, "No Authorization header. Authorization header should be Basic");
        }

        MavenUser           actualUserPassword = parseUsernameAndPassword(authHeader.substring("Basic ".length()));
        Optional<MavenUser> configUserOpt      = findUser(actualUserPassword.getUsername());

        if (configUserOpt.isEmpty()) {
            throw new AuthException(472, "User " + actualUserPassword.getUsername() + " not found");
        }

        MavenUser mavenUser = configUserOpt.get();

        if (!mavenUser.getPassword().equals(actualUserPassword.getPassword())) {
            LOG.debug("Expected password '{}' but was '{}' for user '{}'", mavenUser.getPassword(), actualUserPassword.getPassword(), actualUserPassword.getUsername());
            throw new AuthException(473, "Bad password for user " + actualUserPassword.getUsername());
        }

        LOG.debug("With user {}", actualUserPassword.getUsername());

        return mavenUser;
    }

    private void checkRepositoryAccess(MavenUser aUser, RoutedPath aPath) {
        String repositoryName = new StringTokenizer(aPath.path(), "/").nextToken();
        MavenRepository repository = mavenConfig.getRepositories()
                .stream()
                .filter(it -> repositoryName.equals(it.getName()))
                .findAny()
                .orElseThrow(() -> new AuthException(481, "Repository '" + repositoryName + "' not found"));

        List<String> users = access == RepositoryAccess.READ ? repository.getRead() : repository.getFull();

        if (users == null) {
            throw new AuthException(482, "No " + access + " access config for repository " + repositoryName);
        }

        for (String user : users) {
            if(user.equals(aUser.getUsername())) {
                return;
            }
        }
        throw new AuthException(Status.FORBIDDEN_403.code(), "User has no " + access + " access to repo " + repositoryName);
    }

    private Optional<MavenUser> findUser(String aUsername) {
        for (MavenUser user : mavenConfig.getUsers()) {
            if (user.getUsername().equals(aUsername)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    private MavenUser parseUsernameAndPassword(String aBase64) {
        String          text     = new String(BASE64_DECODER.decode(aBase64), UTF_8);
        StringTokenizer st       = new StringTokenizer(text, ":");
        String          username = st.nextToken();
        String          password = st.nextToken();
        return MavenUser.builder()
                .username(username)
                .password(password)
                .build();
    }

    private boolean isAuthenticated(ServerRequest aRequest) {
        return aRequest.headers().contains(HeaderNames.AUTHORIZATION);
    }

}
