package com.example.emos.workflow.config.quartz;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.emos.workflow.db.pojo.TbAmect;
import com.example.emos.workflow.service.AmectService;
import com.example.emos.workflow.service.AmectTypeService;
import com.example.emos.workflow.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MeetingStatusJob extends QuartzJobBean {
    @Autowired
    private MeetingService meetingService;

    @Autowired
    private AmectService amectService;

    @Autowired
    private AmectTypeService amectTypeService;

    /**
     * 到时间自动更新会议状态
     */
    @SuppressWarnings("all")
    @Transactional
    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        Map map = ctx.getJobDetail().getJobDataMap();
        String uuid = MapUtil.getStr(map, "uuid");
        Integer status = MapUtil.getInt(map, "status");
        String title = MapUtil.getStr(map, "title");
        String date = MapUtil.getStr(map, "date");
        String start = MapUtil.getStr(map, "start");
        String end = MapUtil.getStr(map, "end");
        String flag = MapUtil.getStr(map, "flag");

        //更新会议状态
        HashMap param = new HashMap();
        param.put("status", status);
        param.put("uuid", uuid);
        meetingService.updateMeetingStatus(param);
        log.debug("会议状态更新成功");

        // 如果会议结束，则需要生成缺席会议人员的名单
        if ("end".equals(flag)) {
            /**
             * 我们在进入会议时会自动进行签到，将用户id 添加到已出席名单的 json 数组中，也就是 present 字段中
             * 此 dao 方法是查询所有会议人员不在 present 字段中的人员，也就是缺席人员
             */
            ArrayList<Integer> list = meetingService.searchMeetingUnpresent(uuid);
            // 存在缺席员工
            if (list != null && list.size() > 0) {
                JSONArray array = new JSONArray();
                // 遍历缺席名单，将其放入 json 数组中
                list.forEach(one -> {
                    array.put(one);
                });
                param = new HashMap() {{
                    put("uuid", uuid);
                    put("unpresent", JSONUtil.toJsonStr(array));
                }};
                // 更新缺席名单
                meetingService.updateMeetingUnpresent(param);

                // 对缺席人员进行罚款处理
                //查询缺席会议的罚款金额和ID
                map = amectTypeService.searchByType("缺席会议");
                BigDecimal money = (BigDecimal) map.get("money");
                Integer typeId = (Integer) map.get("id");

                //根据缺席名单生成罚款单
                TbAmect amect = new TbAmect();
                amect.setAmount(money);
                amect.setTypeId(typeId);
                amect.setReason("缺席" + date + " " + start + "~" + end + "的" + title);
                list.forEach(one -> {
                    amect.setUuid(IdUtil.simpleUUID());
                    amect.setUserId(one);
                    amectService.insert(amect);
                });
            }
        }
    }

}