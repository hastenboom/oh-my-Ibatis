package com.student.ohmyibatis.core.datasource;

import lombok.Data;

/**
 * @author Student
 */
@Data
public class DataSourceProperty
{
    private String type = "POOLED";
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/mp";
    private String username = "root";
    private String password = "123456";
    private int maxSize = 5;
}
