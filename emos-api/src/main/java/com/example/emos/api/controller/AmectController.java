package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.amectform.*;
import com.example.emos.api.db.pojo.TbAmect;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.AmectService;
import com.example.emos.api.websocket.WebSocketService;
import com.example.emos.api.wxpay.WXPayUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created By zf
 * 描述:
 */
@RestController
@RequestMapping("/amect")
@Tag(name = "AmectController", description = "罚款Web接口")
@Slf4j
public class AmectController {

    // 微信支付的密钥
    @Value("${wx.key}")
    private String key;

    @Autowired
    private AmectService amectService;

    @PostMapping("/searchAmectByPage")
    @Operation(summary = "查询罚款分页数据")
    @SaCheckLogin
    public R searchAmectByPage(@Valid @RequestBody SearchAmectByPageForm form) {
        if ((form.getStartDate() != null && form.getEndDate() == null) || (form.getStartDate() == null && form.getEndDate() != null)) {
            return R.error("开始时间和结束时间必须同时为空或同时不为空");
        }

        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("start", start);
        param.put("currentUserId", StpUtil.getLoginIdAsInt());

        if ( !( StpUtil.hasPermission("AMECT:SELECT") || StpUtil.hasPermission("ROOT"))) {
            param.put("userId", StpUtil.getLoginIdAsInt());
        }

        PageUtils pageUtils = amectService.searchAmectByPage(param);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/insert")
    @Operation(summary = "新增罚款记录")
    @SaCheckPermission(value = {"ROOT","AMECT:INSERT"}, mode = SaMode.OR)
    public R insert(@Valid @RequestBody InsertAmectForm form) {
        ArrayList<TbAmect> list = new ArrayList<>();
        for (Integer userId : form.getUserId()) {
            TbAmect amect = new TbAmect();
            amect.setAmount(new BigDecimal(form.getAmount()));
            amect.setTypeId(form.getTypeId());
            amect.setReason(form.getReason());
            amect.setUserId(userId);
            amect.setUuid(IdUtil.simpleUUID());
            list.add(amect);
        }
        int rows = amectService.insert(list);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/searchById")
    @Operation(summary = "根据id查找罚款记录")
    @SaCheckPermission({"ROOT","AMECT:SELECT"})
    public R searchById(@Valid @RequestBody SearchAmectByIdForm form) {
        HashMap map = amectService.searchById(form.getId());
        return R.ok(map);
    }

    @PostMapping("/update")
    @Operation(summary = "修改罚款记录")
    @SaCheckPermission({"ROOT","AMECT:UPDATE"})
    public R update(@Valid @RequestBody UpdateAmectForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        int rows = amectService.update(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除罚款记录")
    @SaCheckPermission({"ROOT","AMECT:DELETE"})
    public R delete(@Valid @RequestBody DeleteAmectByIdsForm form) {
        int rows = amectService.delete(form.getIds());
        return R.ok().put("rows", rows);
    }

    @PostMapping("/createNativeAmectPayOrder")
    @Operation(summary = "创建Native罚款支付订单")
    @SaCheckLogin
    public R createNativeAmectPayOrder(@Valid @RequestBody CreateNativeAmectPayOrderForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        int amectId = form.getAmectId();
        HashMap param = new HashMap(){{
            put("userId", userId);
            put("amectId", amectId);
        }};
        String qrCodeBase64 = amectService.createNativeAmectPayOrder(param);
        return R.ok().put("qrCodeBase64", qrCodeBase64);
    }

    @RequestMapping("/recieveMessage")
    @Operation(summary = "接收支付回调消息")
    public void recieveMessage(HttpServletResponse response, HttpServletRequest request) throws Exception {
        request.setCharacterEncoding("utf-8");
        // 使用 IO 流读取 request 中的数据，获取每行数据，使用 StringBuffer 进行拼接，转换成字符串
        Reader reader = request.getReader();
        BufferedReader buffer = new BufferedReader(reader);
        String line = buffer.readLine();
        StringBuffer temp = new StringBuffer();
        while (line != null) {
            temp.append(line);
            line = buffer.readLine();
        }
        // 关闭流
        buffer.close();
        reader.close();
        String xml = temp.toString();
        // 进行数字签名的验证，防止仿造回调信息
        if (WXPayUtil.isSignatureValid(xml, key)) {
            // 将微信平台传递过来的 xml 字符串转换成 map 对象
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            String returnCode = map.get("return_code");
            String resultCode = map.get("result_code");
            if ("SUCCESS".equals(resultCode) && "SUCCESS".equals(returnCode)) {
                // 商品订单id
                String outTradeNo = map.get("out_trade_no");

                HashMap param = new HashMap(){{
                   put("status", 2);
                   put("uuid", outTradeNo);
                }};

                int rows = amectService.updateStatus(param);
                if (rows == 0) {
                    log.error("重复回调");
                    throw new EmosException("重复回调");
                }

                // 向前端推送支付结果
                // 根据罚款单获取 userId
                int userId = amectService.searchUserByUUID(outTradeNo);
                // 向用户推送消息
                WebSocketService.sendInfo("收款成功", userId + "");

                // 向微信平台返回响应
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/xml");
                Writer writer = response.getWriter();
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");

                bufferedWriter.close();
                writer.close();
            }
        } else {
            log.error("数字签名验证失败");
            response.sendError(500, "数字签名验证失败");
        }
    }

    @PostMapping("/searchNativeAmectPayResult")
    @Operation(summary = "查询Native支付罚款订单结果")
    @SaCheckLogin
    public R searchNativeAmectPayResult(@Valid @RequestBody SearchNativeAmectPayResultForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        int amectId = form.getAmectId();

        HashMap param = new HashMap() {{
           put("userId", userId);
           put("amectId", amectId);
           put("status", 1);
        }};

        amectService.searchNativeAmectPayResult(param);

        int status = amectService.searchStatus(amectId);
        return R.ok().put("status",status);
    }

    @PostMapping("/searchChart")
    @Operation(summary = "查询Chart图表")
    @SaCheckPermission(value = {"ROOT", "AMECT:SELECT"}, mode = SaMode.OR)
    public R searchChart(@Valid @RequestBody SearchChartForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        HashMap map = amectService.searchChart(param);
        return R.ok(map);
    }
}
