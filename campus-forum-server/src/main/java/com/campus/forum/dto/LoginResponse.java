package com.campus.forum.dto;

import com.campus.forum.entity.SysUser;
import java.util.List;

/**
 * 登录响应
 */
public class LoginResponse {

    private String token;
    private SysUser user;
    private List<String> roles;
    private List<String> permissions;
    private Boolean needBind;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public SysUser getUser() {
        return user;
    }

    public void setUser(SysUser user) {
        this.user = user;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Boolean getNeedBind() {
        return needBind;
    }

    public void setNeedBind(Boolean needBind) {
        this.needBind = needBind;
    }
}
