package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.roleform.*;
import com.example.emos.api.db.pojo.TbRole;
import com.example.emos.api.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/role")
@Tag(name = "RoleController", description = "角色Web接口")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping("/searchAllRole")
    @Operation(summary = "查询所有角色")
    public R searchAllRole() {
        ArrayList<HashMap> list = roleService.searchAllRole();
        return R.ok().put("list", list);
    }

    @PostMapping("/searchById")
    @Operation(summary = "根据ID查询角色")
    @SaCheckPermission(value = {"ROOT", "ROLE:SELECT"}, mode = SaMode.OR)
    public R searchById(@Valid @RequestBody SearchRoleByIdForm form) {
        HashMap map = roleService.searchById(form.getId());
        return R.ok(map);
    }

    @PostMapping("/searchRoleByPage")
    @Operation(summary = "查询角色分页数据")
    @SaCheckPermission(value = {"ROOT", "ROLE:SELECT"}, mode = SaMode.OR)
    public R searchRoleByPage(@Valid @RequestBody SearchRoleByPageForm form) {
        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);

        param.put("start", start);
        PageUtils pageUtils = roleService.searchRoleByPage(param);

        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/insert")
    @Operation(summary = "新增角色")
    @SaCheckPermission(value = {"ROOT", "ROLE:INSERT"}, mode = SaMode.OR)
    public R insert(@Valid @RequestBody InsertRoleForm form) {
        TbRole tbRole = new TbRole();
        tbRole.setRoleName(form.getRoleName());
        tbRole.setPermissions(JSONUtil.parseArray(form.getPermissions()).toString());
        tbRole.setDesc(form.getDesc());

        int rows = roleService.insert(tbRole);

        return R.ok().put("rows", rows);
    }

    @PostMapping("/update")
    @Operation(summary = "修改角色信息")
    @SaCheckPermission(value = {"ROOT", "ROLE:UPDATE"}, mode = SaMode.OR)
    public R update(@Valid @RequestBody UpdateRoleForm form) {
        TbRole role = new TbRole();
        role.setRoleName(form.getRoleName());
        role.setDesc(form.getDesc());
        role.setPermissions(JSONUtil.parseArray(form.getPermissions()).toString());
        role.setId(form.getId());

        int rows = roleService.update(role);
        // 如果角色修改成功，且角色对应权限发生了改变
        if (rows > 0 && form.getChanged()) {
            // 把角色关联的用户踢下线
            ArrayList<Integer> ids = roleService.searchUserIdByRoleId(form.getId());
            for (Integer id : ids) {
                StpUtil.logoutByLoginId(id);
            }

            return R.ok().put("rows", rows);
        }

        return R.error("修改角色信息失败");
    }

    @PostMapping("/delete")
    @Operation(summary = "删除非系统角色")
    @SaCheckPermission(value = {"ROOT", "ROLE:DELETE"}, mode = SaMode.OR)
    public R delete(@Valid @RequestBody DeleteRoleByIdsForm form) {
        int rows = roleService.deleteRoleByIds(form.getIds());
        if (rows > 0) {
            return R.ok().put("rows", rows);
        }

        return R.error("修改角色失败");
    }

}
