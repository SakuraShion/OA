package com.example.emos.api.db.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色表
 *
 * @TableName tb_role
 */
@Data
public class TbRole implements Serializable {
    /**
     * 主键
     *
     */
    private Integer id;

    /**
     * 角色名称
     *
     */
    private String roleName;

    /**
     * 权限集合
     *
     */
    private String permissions;

    private String desc;

    private String defaultPermissions;

    private Boolean systemic;

    private static final long serialVersionUID = 1L;
}