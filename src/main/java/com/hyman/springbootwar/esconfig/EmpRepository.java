package com.hyman.springbootwar.esconfig;

import com.hyman.springbootwar.entity.Employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


// 实体类必须加上 @Document 注解。
@Repository
public interface EmpRepository extends ElasticsearchRepository<Employee,Integer> {

    //{ "bool" :
    //    { "must" :
    //    [
    //        { "field" : {"name" : "?"} },
    //        { "field" : {"price" : "?"} }
    //    ]
    //    }
    //}
    List<Employee> findByLastName(String name, Integer price);

    /**
     * spring-data 默认是按照接口方法定义的名字（默认认为是驼峰写法），去到实体类中查找对应的字段，当找不到时，就报错了：
     * PropertyReferenceException: No property name found for type Employee!
     *
     * spring-data 规范要求 dao 中的 findBy***，必须和实体字段名称一致。例如 findByName，则实体中也必须有 name。并且字段命
     * 名不能是 a_b 这种格式，这个不符合驼峰规范。
     *
     */
    //List<Employee> findByNameOrPrice(String name, Integer price);
    //
    //Page<Employee> findByName(String name, Pageable page);
    //
    //Page<Employee> findByNameNot(String name,Pageable page);
    //
    //Page<Employee> findByPriceBetween(int price,Pageable page);
    //
    //Page<Employee> findByNameLike(String name,Pageable page);

    @Query("{\"bool\" : {\"must\" : {\"term\" : {\"age\" : 10}}}}")
    Page<Employee> findByAge(Integer age, Pageable pageable);

}
