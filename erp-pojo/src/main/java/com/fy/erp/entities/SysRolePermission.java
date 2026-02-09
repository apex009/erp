package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_perm")
public class SysRolePermission extends BaseEntity {
    private Long roleId;
    private Long permId;
}
