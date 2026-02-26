package com.fy.erp.config;

import com.fy.erp.entities.SysDept;
import com.fy.erp.entities.SysRole;
import com.fy.erp.entities.SysUser;
import com.fy.erp.entities.SysUserRole;
import com.fy.erp.service.SysDeptService;
import com.fy.erp.service.SysRoleService;
import com.fy.erp.service.SysUserRoleService;
import com.fy.erp.service.SysUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysDeptService deptService;
    private final SysUserRoleService userRoleService;

    public DataInitializer(SysUserService userService,
                           SysRoleService roleService,
                           SysDeptService deptService,
                           SysUserRoleService userRoleService) {
        this.userService = userService;
        this.roleService = roleService;
        this.deptService = deptService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void run(String... args) {
        SysDept dept = deptService.lambdaQuery()
                .in(SysDept::getDeptName, "系统管理", "Admin")
                .orderByAsc(SysDept::getId)
                .list()
                .stream()
                .findFirst()
                .orElse(null);
        if (dept == null) {
            dept = new SysDept();
            dept.setDeptName("系统管理");
            dept.setLeader("系统");
            dept.setStatus(1);
            deptService.save(dept);
        } else if (!"系统管理".equals(dept.getDeptName())) {
            dept.setDeptName("系统管理");
            dept.setLeader("系统");
            deptService.updateById(dept);
        }

        SysRole role = roleService.lambdaQuery()
                .eq(SysRole::getRoleCode, "ADMIN")
                .orderByAsc(SysRole::getId)
                .list()
                .stream()
                .findFirst()
                .orElse(null);
        if (role == null) {
            role = new SysRole();
            role.setRoleCode("ADMIN");
            role.setRoleName("管理员");
            role.setStatus(1);
            roleService.save(role);
        } else if (!"管理员".equals(role.getRoleName())) {
            role.setRoleName("管理员");
            roleService.updateById(role);
        }

        SysUser user = userService.lambdaQuery()
                .eq(SysUser::getUsername, "admin")
                .orderByAsc(SysUser::getId)
                .list()
                .stream()
                .findFirst()
                .orElse(null);
        if (user == null) {
            user = new SysUser();
            user.setDeptId(dept.getId());
            user.setUsername("admin");
            user.setNickname("管理员");
            user.setStatus(1);
            userService.setPassword(user, "123456");
            userService.save(user);
        }

        boolean hasUserRole = userRoleService.lambdaQuery()
                .eq(SysUserRole::getUserId, user.getId())
                .eq(SysUserRole::getRoleId, role.getId())
                .exists();
        if (!hasUserRole) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRoleService.save(userRole);
        }
    }
}
