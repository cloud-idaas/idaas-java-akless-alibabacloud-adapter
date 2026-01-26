package com.cloud_idaas.adapter.domain;

public enum DeveloperApiTypeEnum {

    OBTAIN_ACCESS_CREDENTIAL("obtainAccessCredential", "/v2/%s/cloudAccountRoles/_/actions/obtainAccessCredential");

    private final String code;
    private final String path;

    DeveloperApiTypeEnum(String code, String path){
        this.code = code;
        this.path = path;
    }

    public String getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }
}