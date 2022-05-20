package com.example.emos.api.controller.form.userform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Created By zf
 * 描述:
 */
@Schema(description = "修改密码表单")
@Data
public class UpdatePasswordForm {

    @NotBlank(message = "password不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$",message = "password格式错误")
    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "newPassword不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$",message = "newPassword格式错误")
    @Schema(description = "密码")
    private String newPassword;
}
