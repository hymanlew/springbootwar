package com.hyman.springbootwar.controller;

import com.hyman.springbootwar.entity.Employee;
import com.hyman.springbootwar.esconfig.EmpRepository;
import com.hyman.springbootwar.util.LogUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class RabbitController {

    // 不可以运行，有异常
    //@Resource
    //private EmpRepository empRepository;
    //
    //@RequestMapping("es")
    //public Object test(){
    //    Employee employee = new Employee(1,"man", "20");
    //
    //    // 构建一个索引，索引（存储）的数据文档，索引库名，类型名，指定文档 id。
    //    empRepository.index(employee);
    //    //empRepository.save(employee);
    //    //elasticsearchTemplate.index().
    //    LogUtil.logger.info("==== 存储对象成功 ====");
    //    return employee;
    //}
}
