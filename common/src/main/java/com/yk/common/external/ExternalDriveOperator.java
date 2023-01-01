package com.yk.common.external;

import java.io.IOException;
import java.io.InputStream;

public interface ExternalDriveOperator {

    String create(String name, String content) throws IOException;

    String create(String name, String mimeType, InputStream content) throws IOException;

    InputStream read(String id) throws IOException;

    void update(String id, String content) throws IOException;

    void update(String id, InputStream content) throws IOException;

    void delete(String fileId) throws IOException;

}
