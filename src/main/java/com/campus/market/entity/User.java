package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户
 */
@Data
@TableName("user")
public class User implements Serializable {

    /** 角色：普通用户 */
    public static final int ROLE_USER = 0;
    /** 角色：管理员 */
    public static final int ROLE_ADMIN = 1;

    /** 状态：正常 */
    public static final int STATUS_NORMAL = 1;
    /** 状态：禁用 */
    public static final int STATUS_DISABLED = 0;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 密码不对外返回，只允许写入 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    private String studentNo;

    private String campus;

    /** 0普通用户 1管理员 */
    private Integer role;

    /** 1正常 0禁用 */
    private Integer status;

    @TableLogic
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
