package com.yk.common.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;

@Qualifier("password")
@Service
public class PasswordRetrieverImp implements SecretRetriever {

    private static final String ENCRYPTION_FLAG = "encryption";
    private static final String SAP_CP_CRED_STORE_NAMESPACE = "sapcp-credstore-namespace";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String RSA = "RSA";
    private static final String CLIENT_PRIVATE_KEY = "client_private_key";

    private final CfCredentials cfCredentials;

    public PasswordRetrieverImp() {
        cfCredentials = new CfEnv().findServiceByName("capper-creds").getCredentials();
    }

    @Override
    public String receive(String namespace, String name) throws IOException, InterruptedException, NoSuchAlgorithmException,
            InvalidKeySpecException, ParseException, JOSEException {
        @SuppressWarnings("unchecked")
        Map<String, Object> encryption = (Map<String, Object>) cfCredentials.getMap().get(ENCRYPTION_FLAG);

        String url = StringUtils.removeEnd((String) cfCredentials.getMap().get("url"), "/");
        String passwordName = String.format("password?name=%s", name);
        String finalUrl = String.join("/", url, passwordName);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(finalUrl))
                .header(SAP_CP_CRED_STORE_NAMESPACE, namespace)
                .header(AUTHORIZATION_HEADER, "Basic " + Base64.getEncoder()
                        .encodeToString((cfCredentials.getUsername() + ":" + cfCredentials.getPassword()).getBytes(StandardCharsets.UTF_8)))
                .build();
        HttpResponse<String> httpResponse = HttpClient.newBuilder().build().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        byte[] binaryKey = Base64.getDecoder().decode(((String) encryption.get(CLIENT_PRIVATE_KEY)).getBytes(StandardCharsets.UTF_8));
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(binaryKey));

        JWEObject jweObject = JWEObject.parse(httpResponse.body());
        jweObject.decrypt(new RSADecrypter(rsaPrivateKey));

        return (String) jweObject.getPayload().toJSONObject().get("value");
    }
}
