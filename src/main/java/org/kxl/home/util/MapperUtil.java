package org.kxl.home.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

// 获取UserMapper的代理实现类,
// 通过getMapper方法，重写接口方法：通过UserMapper的全类名来找到当前对象的映射文件，再通过要调用的方法找到要调用的sql语句
/**
 * mapper接口和映射文件要保证两个一致:
 *         1，mapper接口的全类名和映射文件的namespace一致
 *         2、mapper接口中的方法的方法名要和映射文件中的sqL的d保持一致
 * */
public class MapperUtil {

    private static InputStream stream = null;

    // 获取SqlSessionFactoryBuilder对象
    private static SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();

    // 获取SqlSessionFactory对象
    private static SqlSessionFactory sqlSessionFactory;

    static{
        try {
            stream = Resources.getResourceAsStream("mybatis-config.xml");
            sqlSessionFactory = sqlSessionFactoryBuilder.build(stream);
        }catch(Exception e){
            e.printStackTrace();
            System.err.println("初始化mybatis错误，系统将退出");
        }
    }

    public static SqlSession getSqlSession(Boolean open){
        // 获取sql的会话对象SqlSession（不会自动提交事务）,是Mybatis提供的操作数据的对象
        // SqlSession sqlSession = sqlSessionFactory.openSession();
        // 获取sql的会话对象SqlSession（会自动提交事务）
        return sqlSessionFactory.openSession(open);
    }


}
