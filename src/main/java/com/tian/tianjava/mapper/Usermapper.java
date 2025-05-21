package com.tian.tianjava.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tian.tianjava.entity.User;
import org.apache.ibatis.annotations.*;


@Mapper
public interface Usermapper extends BaseMapper  {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
    @Select("SELECT * FROM user WHERE Email = #{email}")
    User findByEmail(String email);

    @Select("SELECT id FROM user WHERE username = #{username}")
    Integer findUserIdByUsername(String username);//@Param("username")





//    int updatePasswordByEmail(@Param("email") String email, @Param("password") String hashedPassword);



    @Update("Update user set password=#{newPassword} where Email=#{email}")

    int updatePasswordByEmail(String email, String newPassword);

//    @Insert("INSERT INTO user (username, password, Email) VALUES (#{username}, #{password}, #{email})")
//    @Options(useGeneratedKeys = true, keyProperty = "id")
//    int register(User user);

    @Insert("INSERT INTO user (username, password, email, role) " +
            "VALUES (#{username}, #{password}, #{email}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int register(User user);

    @Delete("DELETE FROM user WHERE username = #{username}")
    int deleteByUsername(@Param("username") String username);

}


