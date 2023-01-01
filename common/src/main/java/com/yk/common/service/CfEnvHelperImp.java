package com.yk.common.service;

import com.yk.common.utils.JsonConverter;
import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class CfEnvHelperImp implements CfEnvHelper {

    private final CfEnv cfEnv = new CfEnv();

    @Override
    public String getServiceAsString(String serviceName) {
        return JsonConverter.InstanceHolder.instance.getInstance().convertToJson(cfEnv.findServiceByName(serviceName).getCredentials().getMap());
    }

    @Override
    public CfCredentials getCredentials(String serviceName) {
        return cfEnv.findServiceByName(serviceName).getCredentials();
    }
}
