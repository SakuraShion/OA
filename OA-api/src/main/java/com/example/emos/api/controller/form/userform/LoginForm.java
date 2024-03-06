package com.example.emos.api.controller.form.userform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Created By zf
 * 描述:   用来保存登陆操作时 HTTP 请求提交的username，password 数据信息，同时使用@valid 注解对form 对象进行规则判断，
 *        需符合对应字段约束的条件。
 */
@Data
@Schema(description = "登陆表单类")
public class LoginForm {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,20}$", message = "用户名格式错误")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "密码格式错误")
    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String code;

    @NotBlank(message = "验证码过期")
    @Schema(description = "标识")
    private String uuid;
}
