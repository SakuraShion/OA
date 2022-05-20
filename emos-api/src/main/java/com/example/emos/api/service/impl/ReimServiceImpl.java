package com.example.emos.api.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbReimDao;
import com.example.emos.api.db.pojo.TbReim;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.ReimService;
import com.example.emos.api.task.ReimWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created By zf
 * 描述:
 */
@Service
public class ReimServiceImpl implements ReimService {

    @Autowired
    private ReimWorkflow reimWorkflow;

    @Autowired
    private TbReimDao reimDao;

    @Override
    public PageUtils searchReimByPage(HashMap param) {
        ArrayList<HashMap> list = reimDao.searchReimByPage(param);
        long count = reimDao.searchReimCount(param);
        int start = MapUtil.getInt(param, "start");
        int length = MapUtil.getInt(param, "length");
        return new PageUtils(list, count, start, length);
    }

    @Override
    public int insert(TbReim reim) {
        int rows = reimDao.insert(reim);
        if (rows == 1) {
            // 异步线程调用工作流
            reimWorkflow.startReimWorkflow(reim.getId(), reim.getUserId());
        } else {
            throw new EmosException("报销申请保存失败");
        }
        return rows;
    }

    @Override
    public HashMap searchReimById(HashMap param) {
        HashMap map=reimDao.searchReimById(param);
        String instanceId = MapUtil.getStr(map, "instanceId");
        //把支付订单的URL生成二维码
        QrConfig qrConfig = new QrConfig();
        qrConfig.setWidth(70);
        qrConfig.setHeight(70);
        qrConfig.setMargin(2);
        String qrCodeBase64 = QrCodeUtil.generateAsBase64(instanceId, qrConfig, "jpg");
        map.put("qrCodeBase64",qrCodeBase64);
        return map;
    }

    @Override
    public int deleteReimById(HashMap param) {
        int id = MapUtil.getInt(param, "id");
        String instanceId = reimDao.searchInstanceIdById(id);
        int rows = reimDao.deleteReimById(param);
        if (rows == 1) {
            reimWorkflow.deleteReimWorkflow(instanceId, "报销申请", "删除报销申请");
        }
        return rows;
    }
}
