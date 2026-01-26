package com.cloud_idaas.adapter.alibabacloud.pam;

import com.aliyun.credentials.provider.AlibabaCloudCredentialsProvider;
import com.cloud_idaas.adapter.domain.DeveloperApiTypeEnum;
import com.cloud_idaas.core.factory.IDaaSCredentialProviderFactory;

public class IDaaSPamAklessCredentialFactory {

    public static com.aliyun.oss.common.auth.CredentialsProvider getOSSV1CredentialProvider(String roleArn, DeveloperApiTypeEnum developerApiTypeEnum) {
        AlibabaCloudCredentialsProvider credentialsProvider = IDaaSPamAlibabaCloudCredentialsProvider.builder()
                .oidcTokenProvider(IDaaSCredentialProviderFactory.getIDaaSCredentialProvider())
                .developerApiEndpoint(IDaaSCredentialProviderFactory.getDeveloperApiEndpoint())
                .developerApiPath(developerApiTypeEnum.getPath())
                .idaasInstanceId(IDaaSCredentialProviderFactory.getIDaasInstanceId())
                .roleArn(roleArn)
                .build();
        return new IDaaSPamOSSV1CredentialsProvider(credentialsProvider);
    }

    public static com.aliyun.sdk.service.oss2.credentials.CredentialsProvider getOSSV2CredentialProvider(String roleArn, DeveloperApiTypeEnum developerApiTypeEnum) {
        AlibabaCloudCredentialsProvider credentialsProvider = IDaaSPamAlibabaCloudCredentialsProvider.builder()
                .oidcTokenProvider(IDaaSCredentialProviderFactory.getIDaaSCredentialProvider())
                .developerApiEndpoint(IDaaSCredentialProviderFactory.getDeveloperApiEndpoint())
                .developerApiPath(developerApiTypeEnum.getPath())
                .idaasInstanceId(IDaaSCredentialProviderFactory.getIDaasInstanceId())
                .roleArn(roleArn)
                .build();
        return new IDaaSPamOSSV2CredentialsProvider(credentialsProvider);
    }

    public static com.aliyun.openservices.log.common.auth.CredentialsProvider getSLSCredentialProvider(String roleArn, DeveloperApiTypeEnum developerApiTypeEnum) {
        AlibabaCloudCredentialsProvider credentialsProvider = IDaaSPamAlibabaCloudCredentialsProvider.builder()
                .oidcTokenProvider(IDaaSCredentialProviderFactory.getIDaaSCredentialProvider())
                .developerApiEndpoint(IDaaSCredentialProviderFactory.getDeveloperApiEndpoint())
                .developerApiPath(developerApiTypeEnum.getPath())
                .idaasInstanceId(IDaaSCredentialProviderFactory.getIDaasInstanceId())
                .roleArn(roleArn)
                .build();
        return new IDaaSPamSLSCredentialsProvider(credentialsProvider);
    }

    public static AlibabaCloudCredentialsProvider getAlibabaCloudCredentialsProvider(String roleArn, DeveloperApiTypeEnum developerApiTypeEnum) {
        return IDaaSPamAlibabaCloudCredentialsProvider.builder()
                .oidcTokenProvider(IDaaSCredentialProviderFactory.getIDaaSCredentialProvider())
                .developerApiEndpoint(IDaaSCredentialProviderFactory.getDeveloperApiEndpoint())
                .developerApiPath(developerApiTypeEnum.getPath())
                .idaasInstanceId(IDaaSCredentialProviderFactory.getIDaasInstanceId())
                .roleArn(roleArn)
                .build();
    }
}