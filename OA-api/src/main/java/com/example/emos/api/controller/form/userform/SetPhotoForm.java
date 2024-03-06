package com.example.emos.api.controller.form.userform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created By zf
 * 描述:
 */
@Data
@Schema(description = "设置头像")
public class SetPhotoForm {
    @NotNull(message = "url不能为空")
    @Schema(description = "头像url")
    private String url;
}
