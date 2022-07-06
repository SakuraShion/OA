package com.example.emos.api.controller.form.message;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created By zf
 * 描述:
 */
@Data
public class UpdateUnreadMessageForm {

    @NotNull(message = "id 不能为空")
    private String id;
}
