package com.cloud_idaas.adapter.alibabacloud.pam;

import com.aliyun.credentials.models.CredentialModel;
import com.aliyun.credentials.provider.AlibabaCloudCredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.Credentials;

public class IDaaSPamOSSV2CredentialsProvider implements com.aliyun.sdk.service.oss2.credentials.CredentialsProvider{

    private final AlibabaCloudCredentialsProvider credentialsProvider;

    public IDaaSPamOSSV2CredentialsProvider(AlibabaCloudCredentialsProvider alibabaCloudCredentialsProvider){
        this.credentialsProvider = alibabaCloudCredentialsProvider;
    }

    @Override
    public Credentials getCredentials() {
        CredentialModel credentialModel = credentialsProvider.getCredentials();
        return new Credentials(credentialModel.getAccessKeyId(),
                credentialModel.getAccessKeySecret(),
                credentialModel.getSecurityToken());
    }
}
