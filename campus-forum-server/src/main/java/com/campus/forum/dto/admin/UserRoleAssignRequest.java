package com.campus.forum.dto.admin;

import java.util.List;

public class UserRoleAssignRequest {

    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
