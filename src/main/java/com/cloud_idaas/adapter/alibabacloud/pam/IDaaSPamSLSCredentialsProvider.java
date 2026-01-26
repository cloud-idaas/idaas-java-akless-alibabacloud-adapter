package com.cloud_idaas.adapter.alibabacloud.pam;

import com.aliyun.credentials.models.CredentialModel;
import com.aliyun.credentials.provider.AlibabaCloudCredentialsProvider;
import com.aliyun.openservices.log.common.auth.Credentials;
import com.aliyun.openservices.log.common.auth.DefaultCredentials;
import com.cloud_idaas.core.http.*;

public class IDaaSPamSLSCredentialsProvider implements com.aliyun.openservices.log.common.auth.CredentialsProvider{

    private final AlibabaCloudCredentialsProvider credentialsProvider;

    public IDaaSPamSLSCredentialsProvider(AlibabaCloudCredentialsProvider alibabaCloudCredentialsProvider) {
        this.credentialsProvider = alibabaCloudCredentialsProvider;
    }

    @Override
    public Credentials getCredentials() {
        CredentialModel credentialModel = credentialsProvider.getCredentials();
        return new DefaultCredentials(credentialModel.getAccessKeyId(),
                credentialModel.getAccessKeySecret(),
                credentialModel.getSecurityToken());
    }
}
