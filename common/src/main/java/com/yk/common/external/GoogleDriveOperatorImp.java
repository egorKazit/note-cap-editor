package com.yk.common.external;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.yk.common.service.CfEnvHelper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Qualifier("google")
public class GoogleDriveOperatorImp implements ExternalDriveOperator {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String DRIVE_FOLDER = "CapSync";

    private final CfEnvHelper cfEnvHelper;

    private final Drive service;
    private final String capSyncFolderId;

    public GoogleDriveOperatorImp(CfEnvHelper cfEnvHelper) throws IOException, GeneralSecurityException {
        this.cfEnvHelper = cfEnvHelper;


        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Credential credential = getCredentials(HTTP_TRANSPORT);

        service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(DRIVE_FOLDER).build();

        FileList capSyncFolder = service.files().list()
                .setQ(String.format("'root' in parents and trashed = false and name = '%s' and mimeType = 'application/vnd.google-apps.folder'", DRIVE_FOLDER))
                .execute();
        File capSyncFolderFile;
        if (capSyncFolder.getFiles().isEmpty()) {
            File fileMetadata = new File();
            fileMetadata.setName(DRIVE_FOLDER);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            capSyncFolderFile = service.files().create(fileMetadata).execute();
        } else {
            capSyncFolderFile = capSyncFolder.getFiles().get(0);
        }
        capSyncFolderId = capSyncFolderFile.getId();
    }

    @SneakyThrows
    public Credential getCredentials(final NetHttpTransport netHttpTransport) {

        var cred = cfEnvHelper.getCredentials("gmail-drive");
        var installedCredentials = cred.getMap();

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(GoogleDriveOperatorImp.class.getClassLoader().getResourceAsStream((String) ((Map<?, ?>) installedCredentials).get("PrivateKeyFile")), ((String) ((Map<?, ?>) installedCredentials).get("PrivateKeySecret")).toCharArray());
        PrivateKey pk = (PrivateKey) keystore.getKey("privatekey", ((String) ((Map<?, ?>) installedCredentials).get("PrivateKeySecret")).toCharArray());

        return new GoogleCredential.Builder()
                .setTransport(netHttpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId((String) ((Map<?, ?>) installedCredentials).get("AccountId"))
                .setServiceAccountScopes(SCOPES)
                .setServiceAccountPrivateKey(pk)
                .build();
    }

    @Override
    public String create(String name, String content) throws IOException {
        return create(name, "text/plain", IOUtils.toInputStream(content, StandardCharsets.UTF_8));
    }

    @Override
    public String create(String name, String mimeType, InputStream content) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setParents(Collections.singletonList(capSyncFolderId));
        AbstractInputStreamContent inputStreamContent = new InputStreamContent(mimeType, content);
        return service.files().create(fileMetadata, inputStreamContent).execute().getId();
    }

    @Override
    public InputStream read(String id) throws IOException {
        return service.files().get(id)
                .executeMediaAsInputStream();
    }

    @Override
    public void update(String fileId, String content) throws IOException {
        update(fileId, IOUtils.toInputStream(content, StandardCharsets.UTF_8));
    }

    @Override
    public void update(String fileId, InputStream content) throws IOException {
        File file = service.files().get(fileId).execute();
        File newFile = file.clone();
        newFile.setId(null);
        String mimeType = file.getMimeType();
        service.files().update(fileId, newFile, new InputStreamContent(mimeType, content))
                .execute();
    }

    @Override
    public void delete(String fileId) throws IOException {
        service.files().delete(fileId).execute();
    }

}
