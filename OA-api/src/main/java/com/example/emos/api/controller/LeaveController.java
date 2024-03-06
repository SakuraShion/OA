package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.leaveform.DeleteLeaveByIdForm;
import com.example.emos.api.controller.form.leaveform.InsertLeaveForm;
import com.example.emos.api.controller.form.leaveform.SearchLeaveByIdForm;
import com.example.emos.api.controller.form.leaveform.SearchLeaveByPageForm;
import com.example.emos.api.db.dao.TbLeaveDao;
import com.example.emos.api.db.pojo.TbLeave;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.JarURLConnection;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
@RestController
@RequestMapping("/leave")
@Tag(name = "LeaveController", description = "员工请假Web接口")
public class LeaveController {
    @Autowired
    private LeaveService leaveService;


    @PostMapping("/searchLeaveByPage")
    @Operation(summary = "查询请假分页数据")
    @SaCheckLogin
    public R searchLeaveByPage(@Valid @RequestBody SearchLeaveByPageForm form) {
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("start", start);
        param.put("myId", StpUtil.getLoginIdAsInt());

        if ( !(StpUtil.hasPermission("LEAVE:SELECT") || StpUtil.hasPermission("ROOT")) ) {
            param.put("userId", StpUtil.getLoginIdAsInt());
        }

        PageUtils pageUtils = leaveService.searchLeaveByPage(param);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/insert")
    @Operation(summary = "添加请假记录")
    @SaCheckLogin
    @SuppressWarnings("all")
    public R insert(@Valid @RequestBody InsertLeaveForm form) {
        // 验证请假开始时间是否大于结束时间
        DateTime date_1 = DateUtil.parse(form.getStart());
        DateTime date_2 = DateUtil.parse(form.getEnd());
        if (date_1.isAfterOrEquals(date_2)) {
            throw new EmosException("请假开始时间不得晚于结束时间");
        }

        HashMap param = new HashMap(){{
           put("userId", StpUtil.getLoginIdAsInt());
           put("start", form.getStart());
           put("end", form.getEnd());
        }};

        if (leaveService.searchContradiction(param)) {
            return R.error("当前请假申请与已有请假申请日期上存在交集覆盖");
        }

        long hours = date_1.between(date_2, DateUnit.HOUR);
        String days = new BigDecimal(hours).divide(new BigDecimal(24), 1, RoundingMode.CEILING).toString();
        // 如果字符串结尾是 .0 结尾，将其抹除
        if (days.contains(".0")) {
            days = days.replace(".0", "");
        }

        // 如果 days 值为 0，将其设置为 0.1，例如请假只请了半小时，而 hours 是 long 类型的，会算作 0
        if (days.equals("0")) {
            days = "0.1";
        }

        TbLeave leave = JSONUtil.parse(form).toBean(TbLeave.class);
        leave.setDays(days);
        leave.setUserId(StpUtil.getLoginIdAsInt());

        int rows = leaveService.insert(leave);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/deleteLeaveById")
    @Operation(summary = "删除请假记录")
    @SaCheckLogin
    public R deleteLeaveById(@Valid @RequestBody DeleteLeaveByIdForm form) {
        HashMap param = new HashMap() {{
           put("id", form.getId());
           put("userId", StpUtil.getLoginIdAsInt());
        }};
        int rows = leaveService.deleteLeaveById(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/searchLeaveById")
    @Operation(summary = "根据id查询请假数据")
    public R searchLeaveById(@Valid @RequestBody SearchLeaveByIdForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        if ( !(StpUtil.hasPermission("LEAVE:SELECT") || StpUtil.hasPermission("ROOT")) ) {
            param.put("userId", StpUtil.getLoginIdAsInt());
        }

        HashMap map = leaveService.searchLeaveById(param);
        return R.ok(map);
    }
}
