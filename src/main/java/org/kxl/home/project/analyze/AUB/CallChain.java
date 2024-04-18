package org.kxl.home.project.analyze.AUB;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.kxl.home.project.entity.MethodCall;
import org.kxl.home.project.mapper.MethodCallMapper;
import org.kxl.home.util.MapperUtil;

import java.io.InputStream;
import java.util.*;

public class CallChain {

    public static void main(String[] args) throws Exception {
        SqlSession sqlSession = MapperUtil.getSqlSession(true);

        String[] innerMethods =
                new String[]{"OrderRepository.updatePaypalCaptureAndStatus","OrderRepository.updatePaypalAuthor","OrderRepository.updateCancel","OrderRepository.updateCancelPaymentConversion","OrderRepository.updateShip","OrderRepository.updateIsShipAndStatus","OrderRepository.updateOrderStatusId","OrderRepository.updateOrderStatusIdAndProcessDate","OrderRepository.updateyourPayRelate"};//oe-admin
                //new String[]{"OrderRepository.updateOrderStatus","OrderRepository.updateOrderStatusId","OrderRepository.ordersUpdatePaypalAuthorization","OrderRepository.updateYourPayRelate","OrderRepository.updateCancel"};//oe-online

        MethodCallMapper mapper = sqlSession.getMapper(MethodCallMapper.class);

        for(String innerMethod : innerMethods){
            List<String> inn = new ArrayList<>();
            inn.add(innerMethod);
            List<List<String>> result = new ArrayList<>();
            result.add(inn);
            List<List<String>> chainDesc = parseRelations(mapper,"oe-admin",result);
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
