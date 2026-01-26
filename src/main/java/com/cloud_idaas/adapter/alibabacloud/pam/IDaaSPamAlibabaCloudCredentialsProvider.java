package com.cloud_idaas.adapter.alibabacloud.pam;

import com.aliyun.credentials.models.CredentialModel;
import com.aliyun.credentials.provider.RefreshResult;
import com.aliyun.credentials.provider.SessionCredentialsProvider;
import com.aliyun.credentials.utils.AuthConstant;
import com.aliyun.credentials.utils.ProviderName;
import com.aliyun.credentials.utils.StringUtils;
import com.cloud_idaas.adapter.domain.constants.CredentialConstants;
import com.cloud_idaas.adapter.domain.constants.RequestConstants;
import com.cloud_idaas.core.domain.constants.HttpConstants;
import com.cloud_idaas.core.exception.CredentialException;
import com.cloud_idaas.core.http.*;
import com.cloud_idaas.core.implementation.authentication.oidc.FileOidcTokenProvider;
import com.cloud_idaas.core.provider.OidcTokenProvider;
import com.cloud_idaas.core.util.RequestUtil;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IDaaSPamAlibabaCloudCredentialsProvider extends SessionCredentialsProvider {

    /**
     * Default duration for started sessions. Unit of Second
     */
    public int durationSeconds = 3600;

    /**
     * The arn of the role to be assumed.
     */
    private final String roleArn;

    private volatile String oidcToken;
    private final OidcTokenProvider oidcTokenProvider;

    /**
     * Unit of millisecond
     */
    private int connectTimeout = 10000;
    private int readTimeout = 5000;

    /**
     * The Id of the IDaas instance
     */
    private final String idaasInstanceId;
    /**
     * Endpoint of PAM DeveloperAPI
     */
    private String developerApiEndpoint;
    private String developerApiPath;

    protected IDaaSPamAlibabaCloudCredentialsProvider(BuilderImpl builder) {
        super(builder);

        this.durationSeconds = (builder.durationSeconds == null ? 3600 : builder.durationSeconds);
        if (this.durationSeconds < 900) {
            throw new IllegalArgumentException("Session duration should be in the range of 900s - max session duration.");
        }

        this.roleArn = builder.roleArn == null ? com.aliyun.credentials.utils.AuthUtils.getEnvironmentRoleArn() : builder.roleArn;
        if (StringUtils.isEmpty(this.roleArn)) {
            throw new IllegalArgumentException("RoleArn or environment variable ALIBABA_CLOUD_ROLE_ARN cannot be empty.");
        }

        this.oidcTokenProvider = builder.oidcTokenProvider == null ?
                new FileOidcTokenProvider(com.aliyun.credentials.utils.AuthUtils.getEnvironmentOIDCTokenFilePath()) : builder.oidcTokenProvider;

        this.connectTimeout = builder.connectionTimeout == null ? 5000 : builder.connectionTimeout;
        this.readTimeout = builder.readTimeout == null ? 10000 : builder.readTimeout;

        this.idaasInstanceId = builder.idaasInstanceId;
        if (StringUtils.isEmpty(this.idaasInstanceId)) {
            throw new IllegalArgumentException("idaas Instance Id cannot be empty.");
        }
        this.developerApiEndpoint = builder.developerApiEndpoint;
        this.developerApiPath = String.format(builder.developerApiPath, this.idaasInstanceId);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public RefreshResult<CredentialModel> refreshCredentials() {
        HttpClient client = HttpClientFactory.getDefaultHttpClient();
        return createCredential(client);
    }

    public RefreshResult<CredentialModel> createCredential(HttpClient client) {
        return getNewSessionCredentials(client);
    }

    public RefreshResult<CredentialModel> getNewSessionCredentials(HttpClient client) {
        String token = oidcTokenProvider.getOidcToken();
        this.oidcToken = token;
        Map<String, String> queries = new HashMap<>();
        queries.put(RequestConstants.CLOUD_ACCOUNT_ROLE_EXTERNAL_ID, this.roleArn);
        String url = RequestUtil.composeUrl(this.developerApiEndpoint, this.developerApiPath, queries, HttpConstants.HTTPS);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpConstants.AUTHORIZATION_HEADER, Collections.singletonList(HttpConstants.BEARER + HttpConstants.SPACE + HttpConstants.SPACE + token));
        HttpRequest httpRequest = new HttpRequest.Builder()
                .httpMethod(HttpMethod.GET)
                .url(url)
                .headers(headers)
                .build();
        HttpResponse httpResponse = client.send(httpRequest);
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(httpResponse.getBody(), Map.class);
        if (null == map || !map.containsKey(CredentialConstants.CLOUD_ACCOUNT_ROLE_ACCESS_CREDENTIAL)) {
            throw new CredentialException(String.format("Error retrieving credentials from PAM result: %s.", httpResponse.getBody()));
        }
        Map<String, Object> result = (Map<String, Object>)map.get(CredentialConstants.CLOUD_ACCOUNT_ROLE_ACCESS_CREDENTIAL);
        if (null == result || !result.containsKey(CredentialConstants.ALIBABA_CLOUD_STS_TOKEN)) {
            throw new CredentialException(String.format("Error retrieving credentials from PAM result: %s.", httpResponse.getBody()));
        }
        Map<String, String> stsResult = (Map<String, String>)result.get(CredentialConstants.ALIBABA_CLOUD_STS_TOKEN);
        if (!stsResult.containsKey(CredentialConstants.PAM_ACCESS_KEY_ID) || !stsResult.containsKey(CredentialConstants.PAM_ACCESS_KEY_SECRET) ||
                !stsResult.containsKey(CredentialConstants.PAM_SECURITY_TOKEN)) {
            throw new CredentialException(String.format("Error retrieving credentials from PAM result: %s.", httpResponse.getBody()));
        }
        long expiration = RequestUtil.getUTCDate(stsResult.get(CredentialConstants.PAM_EXPIRATION)).getTime();
        CredentialModel credential = CredentialModel.builder()
                .accessKeyId(stsResult.get(CredentialConstants.PAM_ACCESS_KEY_ID))
                .accessKeySecret(stsResult.get(CredentialConstants.PAM_ACCESS_KEY_SECRET))
                .securityToken(stsResult.get(CredentialConstants.PAM_SECURITY_TOKEN))
                .type(AuthConstant.OIDC_ROLE_ARN)
                .providerName(this.getProviderName())
                .expiration(expiration)
                .build();
        return RefreshResult.builder(credential)
                .staleTime(getStaleTime(expiration))
                .build();

    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getIdaasInstanceId() {
        return idaasInstanceId;
    }

    public String getRoleArn() {
        return roleArn;
    }

    public String getOIDCToken() {
        return oidcToken;
    }

    public OidcTokenProvider getOidcTokenProvider() {
        return oidcTokenProvider;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getDeveloperApiEndpoint() {
        return developerApiEndpoint;
    }

    public void setDeveloperApiEndpoint(String developerApiEndpoint) {
        this.developerApiEndpoint = developerApiEndpoint;
    }

    public String getDeveloperApiPath() {
        return developerApiPath;
    }

    public void setDeveloperApiPath(String developerApiPath) {
        this.developerApiPath = developerApiPath;
    }

    @Override
    public String getProviderName() {
        return ProviderName.OIDC_ROLE_ARN;
    }

    @Override
    public void close() {
        super.close();
    }

    public interface Builder
            extends SessionCredentialsProvider.Builder<IDaaSPamAlibabaCloudCredentialsProvider, IDaaSPamAlibabaCloudCredentialsProvider.Builder> {

        Builder durationSeconds(Integer durationSeconds);

        Builder idaasInstanceId(String idaasInstanceId);

        Builder roleArn(String roleArn);

        Builder oidcTokenProvider(OidcTokenProvider oidcTokenProvider);

        Builder connectionTimeout(Integer connectionTimeout);

        Builder readTimeout(Integer readTimeout);

        Builder developerApiEndpoint(String developerApiEndpoint);

        Builder developerApiPath(String developerApiPath);

        @Override
        IDaaSPamAlibabaCloudCredentialsProvider build();
    }

    private static final class BuilderImpl
            extends SessionCredentialsProvider.BuilderImpl<IDaaSPamAlibabaCloudCredentialsProvider, IDaaSPamAlibabaCloudCredentialsProvider.Builder>
            implements Builder {
        private Integer durationSeconds;
        private String idaasInstanceId;
        private String roleArn;
        private OidcTokenProvider oidcTokenProvider;
        private Integer connectionTimeout;
        private Integer readTimeout;
        private String developerApiEndpoint;
        private String developerApiPath;

        @Override
        public Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        @Override
        public Builder idaasInstanceId(String idaasInstanceId) {
            this.idaasInstanceId = idaasInstanceId;
            return this;
        }

        @Override
        public Builder roleArn(String roleArn) {
            this.roleArn = roleArn;
            return this;
        }

        @Override
        public Builder oidcTokenProvider(OidcTokenProvider oidcTokenProvider) {
            this.oidcTokenProvider = oidcTokenProvider;
            return this;
        }

        @Override
        public Builder connectionTimeout(Integer connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        @Override
        public Builder readTimeout(Integer readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        @Override
        public Builder developerApiEndpoint(String developerApiEndpoint) {
            this.developerApiEndpoint = developerApiEndpoint;
            return this;
        }

        @Override
        public Builder developerApiPath(String developerApiPath) {
            this.developerApiPath = developerApiPath;
            return this;
        }

        @Override
        public IDaaSPamAlibabaCloudCredentialsProvider build() {
            return new IDaaSPamAlibabaCloudCredentialsProvider(this);
        }
    }
}
