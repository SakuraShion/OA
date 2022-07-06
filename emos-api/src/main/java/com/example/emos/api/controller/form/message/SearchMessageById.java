package com.example.emos.api.controller.form.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created By zf
 * 描述:
 */
@Data
@Schema(description = "根据Id查询消息")
public class SearchMessageById {

    @NotNull(message = "id 不能为空")
    private String id;
}
