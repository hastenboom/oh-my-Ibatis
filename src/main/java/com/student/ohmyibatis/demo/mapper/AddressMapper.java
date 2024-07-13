package com.student.ohmyibatis.demo.mapper;

import com.student.ohmyibatis.annotation.*;
import com.student.ohmyibatis.demo.entity.Address;

/**
 *
 * @author Student
 */
@Mapper
public interface AddressMapper
{
    @Select(sql = "SELECT * FROM address WHERE id = #{id} and name = #{name}")
    Address selectById(long id, String name);

    @Insert(sql = "INSERT INTO address (street, city) " +
            "VALUES (#{street}, #{city})")
    void insert(Address address);

    @Update(sql = "UPDATE address SET street = #{street}, city = #{city}, state = #{state}, zip = #{zip} WHERE id = " +
            "#{id}")
    void update(Address address);

    @Delete(sql = "DELETE FROM address WHERE id = #{id}")
    void deleteById(long id);

}

