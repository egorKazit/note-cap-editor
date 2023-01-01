package com.yk.common.service;

import com.yk.common.configuration.PropertiesConfiguration;
import com.yk.common.utils.JsonConverter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Scope("prototype")
public class HttpOperatorServiceImp implements HttpOperatorService {

    private final PropertiesConfiguration propertiesConfiguration;

    public final OAuthProvider oAuthProvider;

    public HttpOperatorServiceImp(PropertiesConfiguration propertiesConfiguration, OAuthProvider oAuthProvider) {
        this.propertiesConfiguration = propertiesConfiguration;
        this.oAuthProvider = oAuthProvider;
    }

    @Override
    public <T> T send(String path, String method, HttpRequest.BodyPublisher bodyPublisher, Class<T> classOfT, int expectedStatus,
                      String contentType, String resultPath, Object... args)
            throws IOException, InterruptedException, CommonServiceException, URISyntaxException {
        HttpRequest httpRequest = HttpRequest.newBuilder(buildURI(path, args))
                .method(method, bodyPublisher != null ? bodyPublisher : HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", contentType)
                .header("Authorization", "Bearer " + oAuthProvider.fetchToken().getTokenValue()).build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != expectedStatus) {
            try {
                throw new CommonServiceException(new JSONObject(response.body()).getJSONObject("error").getString("message"));
            } catch (JSONException e) {
                throw new CommonServiceException("Unexpected response " + response.body() + " with response code " + response.statusCode());
            }
        }
        String result = response.body();
        if (resultPath != null) {
            result = new JSONObject(result).get(resultPath).toString();
        }
        return JsonConverter.InstanceHolder.instance.getInstance().convertFromJson(result, classOfT);
    }

    @Override
    public InputStream send(String path, String method, HttpRequest.BodyPublisher bodyPublisher, int expectedStatus, String contentType, Object... args)
            throws IOException, InterruptedException, CommonServiceException, URISyntaxException {
        HttpRequest httpRequest = HttpRequest.newBuilder(buildURI(path, args))
                .method(method, bodyPublisher != null ? bodyPublisher : HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", contentType)
                .header("Authorization", "Bearer " + oAuthProvider.fetchToken().getTokenValue()).build();
        HttpResponse<InputStream> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != expectedStatus) {
            throw new CommonServiceException(new JSONObject(response.body()).getJSONObject("error").getString("message"));
        }
        return response.body();
    }

    @Override
    public int send(String path, String method, HttpRequest.BodyPublisher bodyPublisher, String... args) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest httpRequest = HttpRequest.newBuilder(buildURI(path, args))
                .method(method, bodyPublisher != null ? bodyPublisher : HttpRequest.BodyPublishers.noBody())
                .header("Authorization", "Bearer " + oAuthProvider.fetchToken().getTokenValue()).build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    private URI buildURI(String path, Object[] args) throws URISyntaxException {
        String finalURIString = propertiesConfiguration.getService() + "/" + String.format(StringUtils.removeStart(path, "/"), args);
        return new URI(finalURIString.replace(" ", "%20"));
    }

}
