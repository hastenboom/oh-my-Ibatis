package com.student.ohmyibatis.core;

import com.student.ohmyibatis.annotation.Insert;
import com.student.ohmyibatis.annotation.Select;
import com.student.ohmyibatis.core.transaction.TransactionManager;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Student
 */
@Slf4j
public class SqlSession
{
    final private TransactionManager transaction;
    final private String mapperFullPath;
    final private Connection connection;

    public SqlSession(TransactionManager transaction, String mapperFullPath) throws SQLException, ClassNotFoundException
    {
        this.transaction = transaction;
        this.mapperFullPath = mapperFullPath;
        this.connection = transaction.getConnection();
        Class<?> mapper = Class.forName(mapperFullPath);
        createProxy(mapper);
    }

    public <T> T createProxy(Class<T> mapperInterface) throws ClassNotFoundException
    {
        Object proxyObj = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{mapperInterface},
                (proxy, method, args) ->
                {
                    Object returnObj = null;
                    if (method.isAnnotationPresent(Select.class))
                    {
                        Class<?> returnType = method.getReturnType();
                        returnObj = handleSelect(method, args, returnType, proxy);
                    }
                    else if (method.isAnnotationPresent(Insert.class))
                    {
                        handleInsert(method, args);
                    }
                    return returnObj;
                }
        );
        return (T) proxyObj;
    }


    /**
     * TODO: Support only for the equal condition in the where clause and only for the first record.
     * TODO: Future plan is to support
     * <p>
     *     1. allow ListObject
     * </p>
     *
     * @param method
     * @param args
     * @param returnType
     * @param proxy
     * @param <T>
     * @return
     * @throws Exception
     */
    private <T> T handleSelect(Method method, Object[] args, Class<T> returnType, Object proxy) throws Exception
    {
        System.out.println("handleSelect");
        String sql = method.getAnnotation(Select.class).sql();
//        Map<String, Integer> placeHolderMap = new HashMap<>();
        List<String> placeHolderList = new ArrayList<>();


        //replacedSql : INSERT INTO address (street, city, state, zip) VALUES (?, ?, ?, ?)
        String replacedSql = parseSql(sql, placeHolderList);

        log.info("replaceSql:{} ", replacedSql);
        PreparedStatement preparedStatement = connection.prepareStatement(replacedSql);
        for (int i = 0; i < args.length; i++)
        {
            preparedStatement.setObject(i + 1, args[i]);
        }
        log.info("preparedStatement:{}", preparedStatement);


        List<String> fieldsList
                = extractFields(replacedSql);
        T returnObj;
        try
        {
            connection.setAutoCommit(false);
            ResultSet resultSet = preparedStatement.executeQuery();


            if (fieldsList.size() == 1 && fieldsList.get(0).equals("*"))
            {
                returnObj = handleSelectAsterisk(resultSet, returnType);
            }
            else
            {
                returnObj = handleSelectNormal(resultSet, returnType, fieldsList);
            }
            transaction.commit();
            return returnObj;
        }
        catch (Exception e)
        {
            connection.rollback();
            throw e;
        }
        finally
        {
            connection.setAutoCommit(true);
            transaction.close();
        }
    }

    private <T> T handleSelectNormal(ResultSet resultSet, Class<T> returnType, List<String> fieldsList) throws InstantiationException, IllegalAccessException, SQLException, NoSuchFieldException
    {
        T returnObj = (T) returnType.newInstance();

        //FIXME: Though next(), but it handle only one record;
        while (resultSet.next())
        {
            for (String fieldName : fieldsList)
            {
                Object value = resultSet.getObject(fieldName);
                Field declaredField = returnType.getDeclaredField(fieldName);
                declaredField.set(returnObj, value);
            }
        }
        return returnObj;
    }

    private <T> T handleSelectAsterisk(ResultSet resultSet, Class<T> returnType) throws SQLException,
            InstantiationException, IllegalAccessException, NoSuchFieldException
    {
        Field[] declaredFields = returnType.getDeclaredFields();
        List<String> fieldNameList = new ArrayList<>();
        for (Field declaredField : declaredFields)
        {
            declaredField.setAccessible(true);
            fieldNameList.add(declaredField.getName());
        }

        T returnObj = (T) returnType.newInstance();

        //FIXME: Though next(), but it handle only one record;
        while (resultSet.next())
        {
            for (String fieldName : fieldNameList)
            {
                Object value = resultSet.getObject(fieldName);
                Field declaredField = returnType.getDeclaredField(fieldName);
                declaredField.set(returnObj, value);
            }
        }
        return returnObj;
    }


    private void handleInsert(Method method, Object[] args) throws Exception
    {
        String sql = method.getAnnotation(Insert.class).sql();
//        Map<String, Integer> placeHolderMap = new HashMap<>();
        List<String> placeHolderList = new ArrayList<>();
        String replacedSql = parseSql(sql, placeHolderList);

        System.out.println("replaceSql: " + replacedSql);
        System.out.println("placeHolderList: " + placeHolderList);


        PreparedStatement preparedStatement = connection.prepareStatement(replacedSql);

        Class<?> parameterType = method.getParameters()[0].getType();
        for (int i = 0; i < placeHolderList.size(); i++)
        {
            String paramName = placeHolderList.get(i);
            String getMethodName = "get" + paramName.substring(0, 1).toUpperCase() + paramName.substring(1);

            Method declaredMethod = parameterType.getDeclaredMethod(getMethodName);

            preparedStatement.setObject(i + 1, declaredMethod.invoke(args[0]));
        }
        System.out.println(preparedStatement);
        try
        {
            connection.setAutoCommit(false);
            ResultSet resultSet = preparedStatement.executeQuery();

        }
        catch (Exception e)
        {
            connection.rollback();
        }
        finally
        {
            connection.setAutoCommit(true);
            transaction.close();
        }

    }

    private String parseSql(String sql, List<String> placeholderList)
    {
        Pattern pattern = Pattern.compile("#\\{([^}]*)}");
        Matcher matcher = pattern.matcher(sql);
        StringBuilder sb = new StringBuilder();

        int index = 0;
        while (matcher.find())
        {
            // 添加占位符名称到列表中
//            placeholdermap.put(matcher.group(1), index);
            placeholderList.add(matcher.group(1));
            // 替换 #{...} 为 ?
            matcher.appendReplacement(sb, "?");
            index++;
        }
        matcher.appendTail(sb);

        return sb.toString();
    }


    private List<String> extractFields(String sql)
    {
        List<String> fields = new ArrayList<>();
        // 正则表达式匹配字段名
        Pattern pattern = Pattern.compile("SELECT\\s+(.*?)\\s+FROM", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find())
        {
            // 提取匹配的部分，即字段名列表
            String fieldList = matcher.group(1);
            // 使用逗号和空格分隔字段名
            String[] splitFields = fieldList.split("\\s*,\\s*");
            for (String field : splitFields)
            {
                fields.add(field.trim());
            }
        }
        return fields;
    }
}

