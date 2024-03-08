package com.mika.bi.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户登录请求
 *

 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户账户
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;


}
