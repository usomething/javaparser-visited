package org.kxl.home.project.analyze.AUB;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.kxl.home.project.entity.MethodCall;
import org.kxl.home.project.mapper.MethodCallMapper;

import java.io.InputStream;
import java.util.*;

public class CallChain {

    public static void main(String[] args) throws Exception {
        InputStream stream = Resources.getResourceAsStream("mybatis-config.xml");
        // 获取SqlSessionFactoryBuilder对象
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        // 获取SqlSessionFactory对象
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(stream);
        // 获取sql的会话对象SqlSession（不会自动提交事务）,是Mybatis提供的操作数据的对象
        // SqlSession sqlSession = sqlSessionFactory.openSession();
        // 获取sql的会话对象SqlSession（会自动提交事务）
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        // 获取UserMapper的代理实现类,
        // 通过getMapper方法，重写接口方法：通过UserMapper的全类名来找到当前对象的映射文件，再通过要调用的方法找到要调用的sql语句
        /**
         * mapper接口和映射文件要保证两个一致:
         *         1，mapper接口的全类名和映射文件的namespace一致
         *         2、mapper接口中的方法的方法名要和映射文件中的sqL的d保持一致
         * */
        String[] innerMethods = new String[]{"OrderRepository.updateOrderStatus","OrderRepository.updateOrderStatusId","OrderRepository.ordersUpdatePaypalAuthorization","OrderRepository.updateYourPayRelate"};

        MethodCallMapper mapper = sqlSession.getMapper(MethodCallMapper.class);

        for(String innerMethod : innerMethods){
            List<String> inn = new ArrayList<>();
            inn.add(innerMethod);
            List<List<String>> result = new ArrayList<>();
            result.add(inn);
            List<List<String>> chainDesc = parseRelations(mapper,"oe-online",result);
            for(List<String> oneChain : chainDesc){
                System.out.println(String.join(" -> ",oneChain));
            }
        }
    }

    private static List<List<String>> parseRelations(MethodCallMapper mapper,String projectName,List<List<String>> ret) {
        Set<String> chainSet = new HashSet<>();//去重set

        List<List<String>> result = new ArrayList<>();
        Boolean existUnEndedChain = false;
        for(List<String> chain : ret){
            String chainStr = String.join(",",chain);
            if(chainSet.contains(chainStr))continue;
            chainSet.add(chainStr);

            List<List<String>> newChains = parseOneChain(mapper,projectName,chain);
            if(newChains == null){
                if(!Objects.equals(chain.get(0),"START")) {
                    chain.add(0, "START");
                }
                result.add(chain);
            }else {
                result.addAll(newChains);
                existUnEndedChain = true;
            }
        }

        if(existUnEndedChain){
            return parseRelations(mapper,projectName,result);
        }

        return result;
    }

    private static List<List<String>> parseOneChain(MethodCallMapper mapper,String projectName,List<String> oneChain) {
        String innerMethod = oneChain.get(0);
        if(Objects.equals(innerMethod,"START")){
            return null;
        }
        List<List<String>> ret = null;
        List<MethodCall> mcs = mapper.findByCallClassMethodAndProjectName(generateInnerMethod(innerMethod), projectName);
        if(mcs!=null && mcs.size()>0) {
            ret = new ArrayList<>();
            for(MethodCall mc : mcs) {
                String caller = mc.getCaller();
                List<String> newChain = copyArray(oneChain);
                newChain.add(0, caller);
                ret.add(newChain);
            }
        }
        return ret;
    }

    private static List<String> copyArray(List<String> oneChain){
        List<String> newList = new ArrayList<>();
        newList.addAll(oneChain);
        return newList;
    }

    private static List<String> generateInnerMethod(String innerMethod){
        List<String> list = new ArrayList<>();
        list.add(innerMethod);
        String[] split = innerMethod.split("\\.");
        if(split[0].endsWith("Impl")){
            list.add(split[0].replace("Impl","")+"."+split[1]);
        }
        return list;
    }
}
