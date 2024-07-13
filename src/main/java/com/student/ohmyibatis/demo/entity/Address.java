package com.student.ohmyibatis.demo.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Student
 */
@Data
@NoArgsConstructor
public class Address
{
    private long id;
    private long user_id;
    private String province;
    private String city;
    private String town;
    private String mobile;
    private String street;
    private String contact;
    private String is_default;
    private String notes;
    private short deleted;
}
