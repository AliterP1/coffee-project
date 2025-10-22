package com.example.mycoffeedemo.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOwner {
    /**
     * 要校验的资源类型，比如 "user" / "order"
     */
    String resource();

    /**
     * 方法参数里用来标识资源 ID 的参数名，比如 "id" 或 "orderId"
     */
    String idArg();

    /**
     * 允许的角色，如果当前用户角色在其中则直接放行
     * 示例：{"admin","merchant"}
     */
    String[] allowRoles() default {};
}
