package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.message.DeleteMessageRefByIdForm;
import com.example.emos.api.controller.form.message.SearchMessageById;
import com.example.emos.api.controller.form.message.SearchMessageByPageForm;
import com.example.emos.api.controller.form.message.UpdateUnreadMessageForm;
import com.example.emos.api.service.MessageService;
import com.example.emos.api.task.MessageTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

/**
 * Created By zf
 * 描述:
 */
@RestController
@RequestMapping("/message")
@Tag(name="MessageController",description = "消息通知接口")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageTask messageTask;

    @PostMapping("/searchMessageByPage")
    @Operation(summary = "获取分页消息列表")
    @SaCheckLogin
    public R searchMessageByPage(@Valid @RequestBody SearchMessageByPageForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        List<HashMap> list = messageService.searchMessageByPage(userId, start, length);
        long count = messageService.searchMessageCount(userId);

        PageUtils pageUtil = new PageUtils(list, count, start, length);

        return R.ok().put("page", pageUtil);
    }


    @PostMapping("/searchMessageById")
    @Operation(summary = "根据Id查询消息")
    @SaCheckLogin
    public R searchMessageById(@Valid @RequestBody SearchMessageById form) {
        HashMap map = messageService.searchMessageById(form.getId());

        return R.ok().put("result", map);
    }

    @PostMapping("/updateUnreadMessage")
    @Operation(summary = "更新消息为已读")
    @SaCheckLogin
    public R updateUnreadMessage(@Valid @RequestBody UpdateUnreadMessageForm form) {
        long rows = messageService.updateUnreadMessage(form.getId());

        return R.ok().put("rows", rows == 1);
    }

    @PostMapping("/deleteMessageRefById")
    @Operation(summary = "删除消息")
    @SaCheckLogin
    public R deleteMessageRefById(@Valid @RequestBody DeleteMessageRefByIdForm form) {
        for(String id : form.getIds()) {
            messageService.deleteMessageRefById(id);
        }

        return R.ok().put("rows", 1);
    }

    @GetMapping("/refreshMessage")
    @Operation(summary = "刷新消息")
    @SaCheckLogin
    public R refreshMessage() {
        int userId = StpUtil.getLoginIdAsInt();
        messageTask.receiveAsync(userId + "");
        long count = messageService.searchUnreadCount(userId);

        return R.ok().put("UnReadCount", count);
    }

}
