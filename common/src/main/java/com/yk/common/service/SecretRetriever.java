package com.yk.common.service;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.nimbusds.jose.JOSEException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;

public interface SecretRetriever {
    String receive(String namespace, String name) throws IOException, InterruptedException,
            NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JsonEOFException, JOSEException;
}
