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
import java.util.stream.Collectors;

public class CallChain {

    public static void main(String[] args) throws Exception {
        SqlSession sqlSession = MapperUtil.getSqlSession(true);
        String projectName = "oe-admin";
        MethodCall[] innerMethods = new MethodCall[]{new MethodCall("ShippingUtil.canFedexFreight", 5)};
        MethodCallMapper mapper = sqlSession.getMapper(MethodCallMapper.class);

        for (MethodCall innerMethod : innerMethods) {
            List<MethodCall> inn = new ArrayList<>();
            inn.add(innerMethod);
            List<List<MethodCall>> result = new ArrayList<>();
            result.add(inn);
            List<List<MethodCall>> chainDesc = parseRelations(mapper, projectName, result);
            for (List<MethodCall> oneChain : chainDesc) {
                System.out.println(oneChain.stream().map(m -> m.getCallClassMethod()).collect(Collectors.joining(" ->")));
            }
        }
    }

    private static List<List<MethodCall>> parseRelations(MethodCallMapper mapper, String projectName, List<List<MethodCall>> ret) {
        Set<String> chainSet = new HashSet<>();//去重set

        List<List<MethodCall>> result = new ArrayList<>();
        Boolean existUnEndedChain = false;
        for (List<MethodCall> chain : ret) {
            String chainStr = chain.stream().map(mc -> mc.getCallClassMethod()).collect(Collectors.joining(","));
            if (chainSet.contains(chainStr)) continue;
            chainSet.add(chainStr);

            List<List<MethodCall>> newChains = parseOneChain(mapper, projectName, chain);
            if (newChains == null) {
                if (!Objects.equals(chain.get(0).getCallClassMethod(), "START")) {
                    chain.add(0, new MethodCall("START", -1));
                }
                result.add(chain);
            } else {
                result.addAll(newChains);
                existUnEndedChain = true;
            }
        }

        if (existUnEndedChain) {
            return parseRelations(mapper, projectName, result);
        }

        return result;
    }

    private static List<List<MethodCall>> parseOneChain(MethodCallMapper mapper, String projectName, List<MethodCall> oneChain) {
        MethodCall innerMethod = oneChain.get(0);
        Integer paramCount = oneChain.get(0).getCallMethodParamCount();
        if (Objects.equals(innerMethod.getCallClassMethod(), "START")) {
            return null;
        }
        List<List<MethodCall>> ret = null;
        List<MethodCall> mcs = mapper.findByCallClassMethodAndProjectName(generateInnerMethod(innerMethod), projectName, paramCount);
        if (mcs != null && mcs.size() > 0) {
            ret = new ArrayList<>();
            for (MethodCall mc : mcs) {
                MethodCall caller = mc.getCallerClass();
                List<MethodCall> newChain = copyArray(oneChain);
                newChain.add(0, caller);
                ret.add(newChain);
            }
        }
        return ret;
    }

    private static List<MethodCall> copyArray(List<MethodCall> oneChain) {
        List<MethodCall> newList = new ArrayList<>();
        newList.addAll(oneChain);
        return newList;
    }

    private static List<String> generateInnerMethod(MethodCall innerMethod) {
        List<String> list = new ArrayList<>();
        list.add(innerMethod.getCallClassMethod());
        String[] split = innerMethod.getCallClassMethod().split("\\.");
        if (split[0].endsWith("Impl")) {
            list.add(split[0].replace("Impl", "") + "." + split[1]);
        }
        return list;
    }
}
