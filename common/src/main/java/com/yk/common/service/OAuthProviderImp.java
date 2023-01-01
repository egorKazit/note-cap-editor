package com.yk.common.service;

import com.nimbusds.jose.JOSEException;
import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

@Service
@Log4j2
public class OAuthProviderImp implements OAuthProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final SecretRetriever secretRetriever;
    private final CfCredentials cfCredentials;
    private OAuth2AccessToken oAuth2AccessToken;

    public OAuthProviderImp(SecretRetriever secretRetriever) {
        this.secretRetriever = secretRetriever;
        cfCredentials = new CfEnv().findServiceByName("capper-xsuaa").getCredentials();
    }

    @Override
    public OAuth2AccessToken fetchToken() throws IOException, InterruptedException {
        return getOrRequestNew();
    }

    private OAuth2AccessToken getOrRequestNew() throws IOException, InterruptedException {
        if (oAuth2AccessToken == null || !Objects.requireNonNull(oAuth2AccessToken.getExpiresAt()).isAfter(Instant.now())) {
            String targetUrl;
            try {
                targetUrl = String.format("%s/oauth/token?grant_type=password" +
                                "&username=egorprostoy@gmail.com&password=%s", (String) cfCredentials.getMap().get("url"),
                        secretRetriever.receive("capper", "capper-password"));
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | ParseException |
                     JOSEException e) {
                throw new InterruptedException();
            }

            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(URI.create(targetUrl));
            Charset charset = StandardCharsets.UTF_8;
            String credentialsString = cfCredentials.getMap().get("clientid") + ":" + cfCredentials.getMap().get("clientsecret");
            byte[] encodedBytes = Base64.getEncoder().encode(credentialsString.getBytes(charset));
            var authorizationHeader = new String(encodedBytes, charset);
            httpRequestBuilder.header(AUTHORIZATION_HEADER, "Basic " + authorizationHeader);

            HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                throw new InterruptedException("Request can not be processed");
            }
            JSONObject jsonObject = new JSONObject(httpResponse.body());
            oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, jsonObject.getString("access_token"), Instant.now(),
                    Instant.now().plusSeconds(Integer.toUnsignedLong(jsonObject.getInt("expires_in"))));
        }
        return oAuth2AccessToken;
    }

}
