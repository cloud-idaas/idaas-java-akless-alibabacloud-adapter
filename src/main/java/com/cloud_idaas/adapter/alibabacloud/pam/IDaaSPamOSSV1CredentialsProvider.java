package com.cloud_idaas.adapter.alibabacloud.pam;

import com.aliyun.credentials.models.CredentialModel;
import com.aliyun.credentials.provider.AlibabaCloudCredentialsProvider;
import com.aliyun.oss.common.auth.BasicCredentials;
import com.aliyun.oss.common.auth.Credentials;
import com.cloud_idaas.core.http.*;

public class IDaaSPamOSSV1CredentialsProvider implements com.aliyun.oss.common.auth.CredentialsProvider {

    private final AlibabaCloudCredentialsProvider credentialsProvider;

    public IDaaSPamOSSV1CredentialsProvider(AlibabaCloudCredentialsProvider alibabaCloudCredentialsProvider){
        this.credentialsProvider = alibabaCloudCredentialsProvider;
    }

    @Override
    public void setCredentials(Credentials credentials) {
    }

    @Override
    public Credentials getCredentials() {
        CredentialModel credentialModel = credentialsProvider.getCredentials();
        return new BasicCredentials(credentialModel.getAccessKeyId(),
                credentialModel.getAccessKeySecret(),
                credentialModel.getSecurityToken(),
                credentialModel.getExpiration());
    }
}