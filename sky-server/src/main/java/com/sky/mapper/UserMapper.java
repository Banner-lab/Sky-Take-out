package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid=#{openid}")
    User queryByOpenId(String openid);


    void insert(User user);

    @Select("select * from user where id = #{id}")
    User queryById(Long id);
}
