package org.kxl.home.project.analyze.AUB;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.kxl.home.project.entity.MethodCall;
import org.kxl.home.project.mapper.MethodCallMapper;
import org.kxl.home.project.model.MethodCallNode;
import org.kxl.home.util.ListUtil;
import org.kxl.home.util.MapperUtil;

import java.util.*;
import java.util.stream.Collectors;

public class CallChain {

    public static void main(String[] args) throws Exception {
        SqlSession sqlSession = MapperUtil.getSqlSession(true);
        //这里是要改的
        String projectName = "oe-online";
        //这里也是要改的，注意：这里要找到对应接口的方法，实现类中的私有方法不行，自己手动上踪找到接口的定义方法（也就是实现类中的public方法）
        MethodCall[] innerMethods = new MethodCall[]{
                /*new MethodCall("CommonServiceImpl.addOperationProcessHistory", 5),
                new MethodCall("CommonServiceImpl.saveOrdersProcessHistory", 3),
                new MethodCall("EmailServiceImpl.addOperationHistory",5),
                new MethodCall("NotificationServiceImpl.addPaMsgOrderProcessHistory",4),
                new MethodCall("OrderServiceImpl.addOperationHistory",5),
                new MethodCall("OrderServiceImpl.insertIntoProcessHistory",5),
                new MethodCall("OrderServiceImpl.insertIntoProcessHistory",7)*/
                /*new MethodCall("CommonFunctionServiceImpl.insertIntoProcessHistory",8),
                new MethodCall("NotificationServiceImpl.insertIntoProcessHistory",8),
                new MethodCall("Notification2ServiceImpl.insertIntoProcessHistory",8)*/
                new MethodCall("OrdersProcessHistoryRepository.saveAndFlush",1)
        };
        List<List<MethodCall>> reverse = new ArrayList<>();
        MethodCallMapper mapper = sqlSession.getMapper(MethodCallMapper.class);
        for (MethodCall innerMethod : innerMethods) {
            List<MethodCall> inn = new ArrayList<>();
            inn.add(innerMethod);
            List<List<MethodCall>> result = new ArrayList<>();
            result.add(inn);
            List<List<MethodCall>> chainDesc = parseRelations(mapper, projectName, result);
            for (List<MethodCall> oneChain : chainDesc) {
                System.out.println(oneChain.stream().map(m -> m.getCallClassMethod()).collect(Collectors.joining(" ->")));
                reverse.add(ListUtil.reverse(oneChain));
            }
        }

        List<MethodCallNode> rootNodes = constructTree(reverse);
        for(MethodCallNode rootNode : rootNodes) {
            System.out.println(rootNode);
        }
    }

    //将给定的List<List<MethodCall>> ret有限分裂出所有的调用头，直到头再也没有头，也即是根为止
    private static List<List<MethodCall>> parseRelations(MethodCallMapper mapper, String projectName, List<List<MethodCall>> ret) {
        Set<String> chainSet = new HashSet<>();//去重set

        List<List<MethodCall>> result = new ArrayList<>();
        Boolean existUnEndedChain = false;
        for (List<MethodCall> chain : ret) {
            String chainStr = chain.stream().map(mc -> mc.getCallClassMethod()).collect(Collectors.joining(","));
            if (chainSet.contains(chainStr)) continue;
            chainSet.add(chainStr);
            //一分多
            List<List<MethodCall>> newChains = parseOneChain(mapper, projectName, chain);
            //如果碰到null，代表已经是完整的链条了，头上加上START
            if (newChains == null) {
                if (!Objects.equals(chain.get(0).getCallClassMethod(), "START")) {
                    chain.add(0, new MethodCall("START", -1));
                }
                result.add(chain);
            } else {
                //这里说明又有至少一根链条加上了调用头
                result.addAll(newChains);
                existUnEndedChain = true;
            }
        }

        if (!existUnEndedChain) {
            return result;
        }
        return parseRelations(mapper, projectName, result);
    }

    /**
     * 处理一根链条，找到上方调用者，可能有多个，那就先复制自己多根链条，把每个上方调用者都加到这些链条的头部，然后把分裂出的多跟链条作为
     * 如果没有上方调用者，自己就是根了，那就返回null
     * @param mapper
     * @param projectName
     * @param oneChain
     * @return
     */
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

    private static List<MethodCallNode> constructTree(List<List<MethodCall>> chains) {
        Map<String,MethodCallNode> map = new HashMap<>();
        List<MethodCallNode> allRoots = new ArrayList<>();

        String callClassMethod = null;

        for(List<MethodCall> chain : chains){
            for(int i=0;i<chain.size();++i){
                callClassMethod = chain.get(i).getCallClassMethod();
                if(i==0){//如果是根
                    if(!map.containsKey(callClassMethod)){
                        MethodCallNode root = new MethodCallNode();
                        root.setSelf(chain.get(i));
                        root.setCoord(Pair.of(1,0));
                        map.put(callClassMethod,root);
                        allRoots.add(root);
                    }else{
                        //已经放进去了，别再此重复放了
                    }
                }else{//如果是二级以及以后的调用端
                    if(Objects.equals("START",chain.get(i).getCallClassMethod())){
                        //START 跳过
                        continue;
                    }
                    //先找出父，且肯定能找到不存在为null的情况
                    MethodCallNode myParent = map.get(chain.get(i-1).getCallClassMethod());
                    //再找一下自己是不是也曾出现过
                    MethodCallNode mySelf = map.get(callClassMethod);
                    //如果自己没有出现过
                    if(mySelf==null){
                        //第一步先new
                        mySelf = new MethodCallNode();
                        mySelf.setSelf(chain.get(i));
                        mySelf.setParent(myParent);
                        //父加child
                        myParent.addChild(mySelf);
                        //操作坐标
                        Pair<Integer,Integer> parentCoord = myParent.getCoord();
                        myParent.setCoord(Pair.of(parentCoord.getKey(),parentCoord.getValue()+1));
                        mySelf.setCoord(Pair.of(parentCoord.getKey()+1,0));

                        map.put(callClassMethod,mySelf);
                    }else{
                        //如果自己出现过，说明已经在这棵树中的某一环，不需要重塑已有的枝丫
                    }
                }
            }
        }
        return allRoots;
    }
}
