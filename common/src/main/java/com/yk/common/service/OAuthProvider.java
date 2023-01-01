package com.yk.common.service;

import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.io.IOException;

public interface OAuthProvider {
    OAuth2AccessToken fetchToken() throws IOException, InterruptedException;
}
