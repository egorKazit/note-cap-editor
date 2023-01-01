package com.yk.common.service;

import io.pivotal.cfenv.core.CfCredentials;

public interface CfEnvHelper {

    String getServiceAsString(String serviceName);

    CfCredentials getCredentials(String serviceName);

}
