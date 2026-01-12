package com.wang.wangaicodemother.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        for (UserRoleEnum item : UserRoleEnum.values()) {

            if (ObjectUtil.isEmpty(value)) {
                return null;
            }
            if (item.value.equals(value)) {
                return item;
            }
        }
        return null;
    }
}
