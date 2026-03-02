package com.fy.erp.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fy.erp.security.UserContext;
import com.fy.erp.security.UserPrincipal;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "deleted", Integer.class, 0);

        UserPrincipal user = UserContext.get();
        if (user != null) {
            strictInsertFill(metaObject, "createBy", Long.class, user.getUserId());
            strictInsertFill(metaObject, "updateBy", Long.class, user.getUserId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        UserPrincipal user = UserContext.get();
        if (user != null) {
            strictUpdateFill(metaObject, "updateBy", Long.class, user.getUserId());
        }
    }
}
