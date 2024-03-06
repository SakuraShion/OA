package com.example.emos.api.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbAmectDao;
import com.example.emos.api.db.pojo.TbAmect;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.AmectService;
import com.example.emos.api.wxpay.MyWXPayConfig;
import com.example.emos.api.wxpay.WXPay;
import com.example.emos.api.wxpay.WXPayConfig;
import com.example.emos.api.wxpay.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created By zf
 * 描述:
 */
@Service
@Slf4j
public class AmectServiceImpl implements AmectService {
    private static final String notify_url = "http://4228yz.natappfree.cc/emos-api/amect/recieveMessage";

    @Autowired
    private TbAmectDao amectDao;

    @Autowired
    private MyWXPayConfig myWXPayConfig;

    @Override
    public PageUtils searchAmectByPage(HashMap param) {
        ArrayList<HashMap> list = amectDao.searchAmectByPage(param);
        long count = amectDao.searchAmectCount(param);
        int start = (int) param.get("start");
        int length = (int) param.get("length");
        return new PageUtils(list, count, start, length);
    }

    @Override
    public int insert(ArrayList<TbAmect> params) {
        for (TbAmect amect : params) {
            amectDao.insert(amect);
        }
        return params.size();
    }

    @Override
    public HashMap searchById(int id) {
        return amectDao.searchById(id);
    }

    @Override
    public int update(HashMap param) {
        return amectDao.update(param);
    }

    @Override
    public int delete(Integer[] ids) {
        return amectDao.delete(ids);
    }

    @Override
    @SuppressWarnings("all")
    public String createNativeAmectPayOrder(HashMap param) {
        int userId = MapUtil.getInt(param, "userId");
        int amectId = MapUtil.getInt(param, "amectId");
        // 根据罚款单 id 和用户 id 查找对应的罚款信息
        HashMap map = amectDao.searchAmectByCondition(param);

        if (map != null &&  map.size() > 0) {
            // 获取到罚金，并转换成分值
            String amount = new BigDecimal(MapUtil.getStr(map, "amount")).multiply(new BigDecimal("100")).intValue() + "";
            try {
                WXPay wxPay = new WXPay(myWXPayConfig);
                param.clear();
                // 生成随机字符串
                param.put("nonce_str", WXPayUtil.generateNonceStr());
                param.put("body", "缴纳罚款");
                // 将罚款表中的 uuid 作为商品订单 id
                param.put("out_trade_no", MapUtil.getStr(map, "uuid"));
                // 支付金额，单位：分
                param.put("total_fee", amount);
                // 商户系统的 IP 地址
                param.put("spbill_create_ip", "127.0.0.1");
                // 回调地址
                param.put("notify_url", notify_url);
                param.put("trade_type", "NATIVE");
                param.put("product_id", amectId + "");
                // 生成数字签名字符串
                String sign = WXPayUtil.generateSignature(param, myWXPayConfig.getKey());
                param.put("sign", sign);

                // todo 生成订单后关闭弹窗，一段时间后线程重复生成订单

                Map<String, String> result = wxPay.unifiedOrder(param); //创建支付订单
                String prepayId = result.get("prepay_id");  //微信订单ID
                String codeUrl = result.get("code_url");   //支付连接，需要生成二维码让手机扫码

                if (prepayId != null) {
                    param.clear();
                    param.put("prepayId", prepayId);
                    param.put("amectId", amectId);

                    // 将支付订单 id 更新到罚款信息中去
                    int rows = amectDao.updatePrepayId(param);
                    if (rows == 0) {
                        throw new EmosException("更新罚款单的支付ID失败");
                    }

                    // 将二维码图片编译成 base64 编码的字符串返回到前端生成二维码图片
                    QrConfig qrConfig = new QrConfig();
                    qrConfig.setWidth(255);
                    qrConfig.setHeight(255);
                    qrConfig.setMargin(2);
                    String qrCodeBase64 = QrCodeUtil.generateAsBase64(codeUrl, qrConfig, "jpg");

                    return qrCodeBase64;
                } else {
                    log.error("prepayId 为空");
                    throw new EmosException("prepayId 为空");
                }
            } catch (Exception e) {
                log.error("创建支付订单失败");
                throw new EmosException("创建支付订单失败");
            }
        } else {
            log.error("没有找到罚款单");
            throw new EmosException("没有找到罚款单");
        }
    }

    @Override
    public int updateStatus(HashMap param) {
        int count = amectDao.updateStatus(param);
        return count;
    }

    @Override
    public Integer searchUserByUUID(String uuid) {
        return amectDao.searchUserByUUID(uuid);
    }

    @Override
    public void searchNativeAmectPayResult(HashMap param) {
        HashMap map = amectDao.searchAmectByCondition(param);
        if (MapUtil.isNotEmpty(map)) {
            String uuid = MapUtil.getStr(map, "uuid");
            param.clear();
            param.put("appid", myWXPayConfig.getAppID());
            param.put("mch_id", myWXPayConfig.getMchID());
            param.put("out_trade_no", uuid);
            param.put("nonce_str", WXPayUtil.generateNonceStr());

            try {
                // 获取签名，通过 param 和 支付密钥
                String sign = WXPayUtil.generateSignature(param, myWXPayConfig.getKey());
                param.put("sign", sign);
                WXPay wxPay = new WXPay(myWXPayConfig);
                Map<String, String> result = wxPay.orderQuery(param);
                String returnCode = result.get("return_code");
                String resultCode = result.get("result_code");

                if ("SUCCESS".equals(resultCode) && "SUCCESS".equals(returnCode)) {
                    String tradeState = result.get("trade_state");
                    // 查询到订单支付成功
                    if ("SUCCESS".equals(tradeState)) {
                        amectDao.updateStatus(new HashMap(){{
                            put("uuid", uuid);
                            put("status", 2);
                        }});
                    }
                }

            } catch (Exception e) {
                log.error("执行异常", e);
                throw new EmosException("执行异常", e);
            }
        }
    }

    @Override
    public int searchStatus(int amectId) {
        return amectDao.searchStatus(amectId);
    }

    @Override
    public HashMap searchChart(HashMap param) {

        // 通过SQL 查询图表数据
        ArrayList<HashMap> chart_1 = amectDao.searchChart_1(param);
        ArrayList<HashMap> chart_2 = amectDao.searchChart_2(param);
        ArrayList<HashMap> chart_3 = amectDao.searchChart_3(param);
        param.clear();
        int year = DateUtil.year(new Date());
        param.put("year", year);
        param.put("status", 1);
        ArrayList<HashMap> list_1 = amectDao.searchChart_4(param);
        param.replace("status", 2);
        ArrayList<HashMap> list_2 = amectDao.searchChart_4(param);

        ArrayList<HashMap> chart_4_1 = new ArrayList<>();
        ArrayList<HashMap> chart_4_2 = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            HashMap map = new HashMap();
            map.put("month", i);
            map.put("ct", 0);
            chart_4_1.add(map);
            chart_4_2.add((HashMap) map.clone());
        }
        list_1.forEach(one -> {
            chart_4_1.forEach(temp -> {
                if (MapUtil.getInt(one, "month") == MapUtil.getInt(temp, "month")) {
                    temp.replace("ct", MapUtil.getInt(one, "ct"));
                }
            });
        });

        list_2.forEach(one -> {
            chart_4_2.forEach(temp -> {
                if (MapUtil.getInt(one, "month") == MapUtil.getInt(temp, "month")) {
                    temp.replace("ct", MapUtil.getInt(one, "ct"));
                }
            });
        });


        HashMap map = new HashMap() {{
            put("chart_1", chart_1);
            put("chart_2", chart_2);
            put("chart_3", chart_3);
            put("chart_4_1", chart_4_1);
            put("chart_4_2", chart_4_2);
        }};
        return map;
    }


}
