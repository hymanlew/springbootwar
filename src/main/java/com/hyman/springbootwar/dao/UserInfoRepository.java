package com.hyman.springbootwar.dao;

import com.hyman.springbootwar.entity.UserInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * https://docs.spring.io/spring-data/jpa/docs/2.0.13.RELEASE/reference/html/
 *
 */
@Repository
public interface UserInfoRepository extends CrudRepository<UserInfo,Long> {
}
