package com.hyman.springbootwar.service;

import com.hyman.springbootwar.util.LogUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    public void NoAsync(String s){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtil.logger.info(s);
    }

    // 异步调用注解
    @Async
    public void Async(String s) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtil.logger.info(s);
    }

    /** MON-SUN
     *
     字段	允许值	    允许的特殊字符
      秒	    0-59	    , - * /
      分 	0-59	    , - * /
      小时	0-23	    , - * /
      日期	1-31	    , - * ? / L W C
      月份	1-12	    , - * /
      星期	0-7（SUN-SAT，0 和 7是SUN）  , - * ? / L C #

     特殊字符	代表含义
        ,	    枚举（0,1,2,3，每个时间都执行一次）
        -	    区间（0-3，在此时间范围内都执行）
        *	    任意
        /	    步长（0/4，每隔一定时间执行一次）
        ?	    日/星期冲突匹配
        L	    最后
        W	    工作日
        C	    和 calendar 联系后计算过的值
        #	    星期，4#2 = 第2个星期四

     * ("0 0/5 14,18 * * ?")    每天14点，和18点，每隔5分钟执行一次
     * ("0 15 10 ? * 1-6")      每个月的周一至周六 10：15 分执行一次
     * ("0 0 2 ? * 6L")         每个月的最后一个周六凌晨 2点执行一次
     * ("0 0 2 LW * ?")         每个月的最后一个工作日凌晨 2点执行一次
     * ("0 0 2-4 ? * 1#1")      每个月的第一个周一凌晨 2到4点期间，每个整点执行一次
     */
    //@Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(cron = "0,1,2,3 * * * * 0-7")
    public void schedul(){
        LogUtil.logger.info("===== 定时任务 =====");
    }
}
