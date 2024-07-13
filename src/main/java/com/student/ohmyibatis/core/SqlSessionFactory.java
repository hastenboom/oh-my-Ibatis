package com.student.ohmyibatis.core;

import com.student.ohmyibatis.core.datasource.DataSource;
import com.student.ohmyibatis.core.datasource.DataSourceProperty;
import com.student.ohmyibatis.core.datasource.PooledDataSource;
import com.student.ohmyibatis.core.transaction.TransactionManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Student
 */
@Data
@Slf4j
public class SqlSessionFactory
{

    private TransactionManager transaction;
    private DataSource dataSource;

    private SqlSessionFactoryProperty sqlSessionFactoryProperty;


    public SqlSessionFactory(DataSource dataSource,
                             SqlSessionFactoryProperty sqlSessionFactoryProperty)
    {
        this.dataSource = dataSource;
        this.sqlSessionFactoryProperty = sqlSessionFactoryProperty;
    }

    private void scanMappingLocations() throws IOException
    {
        List<String> mappingLocationList = sqlSessionFactoryProperty.getMappingLocations();
        if (mappingLocationList == null || mappingLocationList.isEmpty())
        {
            throw new IllegalArgumentException("No mapping location found.");
        }

        //TODO: this part should be extracted as ResourceLoader
        for (String mappingLocation : mappingLocationList)
        {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            org.springframework.core.io.Resource[] resources = resolver.getResources(mappingLocation);
            List<String> formattedPaths = new ArrayList<>();
            //handle every mapper interface;
            for (org.springframework.core.io.Resource resource : resources)
            {
                String resourcePath = resource.getURI().getPath();

                String mapperFullPath = resourcePath.substring(resourcePath.lastIndexOf("com"));

                //com.student.ohmyibatis.demo.mapper.UserMapper, used for Class.forName()
                mapperFullPath = mapperFullPath.replace("/", ".");
                mapperFullPath = mapperFullPath.replace(".class", "");
                String classSimpleName = mapperFullPath.substring(mapperFullPath.lastIndexOf(".") + 1);


                log.info("Found mapper interface:{} ", classSimpleName);
                log.info("Mapper full path:{} ", mapperFullPath);


                // TODO: 🤔 should it be putted into the Spring context?
//                SqlSession sqlSession = openSession(classSimpleName);

            }
        }
    }

    public SqlSession openSession(String mapperFullPath) throws Exception
    {
        Connection connection = dataSource.getConnection();
        this.transaction.setDataSource(dataSource);
        this.transaction.setConnection(connection);
        return openSession(transaction, mapperFullPath);
    }

    private SqlSession openSession(TransactionManager transaction, String mapperFullPath) throws SQLException
    {
        return new SqlSession(transaction, mapperFullPath);
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException
    {
        SqlSessionFactoryProperty sqlSessionFactoryProperty = new SqlSessionFactoryProperty();
        sqlSessionFactoryProperty.setMappingLocations(Arrays.asList("classpath:com/student/ohmyibatis/demo/mapper/**"));

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactory(new PooledDataSource(new DataSourceProperty()),
                sqlSessionFactoryProperty);
        sqlSessionFactory.setSqlSessionFactoryProperty(sqlSessionFactoryProperty);

        sqlSessionFactory.scanMappingLocations();

    }
}

