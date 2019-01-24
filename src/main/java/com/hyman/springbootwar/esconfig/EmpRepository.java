package com.hyman.springbootwar.esconfig;

import com.hyman.springbootwar.entity.Employee;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


// 实体类必须加上 @Document 注解。
@Repository
public interface EmpRepository extends ElasticsearchRepository<Employee,Integer> {

}
