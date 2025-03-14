package org.kxl.home.project.analyze.AUB;

import org.apache.commons.lang3.StringUtils;
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
        String projectName = "oe-admin";
        //这里也是要改的，注意：这里要找到对应接口的方法，实现类中的私有方法不行，自己手动上踪找到接口的定义方法（也就是实现类中的public方法）
        MethodCall[] innerMethods = new MethodCall[]{
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","changeLMOrderLight",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateAdminAndDealerHasNew",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateAVerifyAddress",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateBName",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateBVerifyAddress",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateCancel",4),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateCancelIDAndReason",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateCancelPaymentConversion",5),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateDelivered",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateESD",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateExpeditedIndicatorType",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateFirstPendingOrder",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateFraudScoreCorrect",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateGPGManualLabelProcess",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateGroupId",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateHasOpen",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateHasUnRead",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateIsShip",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateIsShipAndStatus",4),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateManualChargeTotal",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateManualChargeTotalOnly",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateNoteHistory",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateNotification",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateOrderCreateShipmentIssueCount",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateOrdersEligibleInfo",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateOrderStatusId",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateOrderStatusIdAndProcessDate",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateOrderTypeAndHasNew",4),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateOrderTypeRelation",6),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateOrderVoidStatus",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updatePackageAction",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updatePaypalAuthor",4),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updatePaypalCapture",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updatePickupStatus",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateProcessDate",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateRiskLevel",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updaterOrderNum",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateShip",4),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateShipbyDate",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateShippingCostStatus",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateSName",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateTotalRelation",5),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateTrackingNo",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateTrackingNoEmpty",1),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateTransferOrderInfo",1),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateTryTime",1),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateUpdateToYYAndRApproved",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateVendorTotalAndNo",3),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateVerifyAddress",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateWorkStatusID",2),
                new MethodCall("com.autobest.backend.database.repositories.OrderRepository","updateyourPayRelate",5),
        };
        List<List<MethodCall>> reverse = new ArrayList<>();
        MethodCallMapper mapper = sqlSession.getMapper(MethodCallMapper.class);
        for (MethodCall innerMethod : innerMethods) {
            List<MethodCall> inn = mapper.findByClassNameAndMethodNameAndParamCount(innerMethod.getClassName(),innerMethod.getMethodName(),innerMethod.getMethodParamCount(),projectName);
            List<List<MethodCall>> result = new ArrayList<>();
            result.add(inn);
            List<List<MethodCall>> chainDesc = parseRelations(mapper, projectName, result);
            for (List<MethodCall> oneChain : chainDesc) {
                System.out.println(oneChain.stream().map(m -> String.format("%s.%s[%s]",m.getSimpleClassName(),m.getMethodName(),m.getMethodParamCount())).collect(Collectors.joining("-> ")));
                reverse.add(ListUtil.reverse(oneChain));
            }

            System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        }

        /*List<MethodCallNode> rootNodes = constructTree(reverse);
        for(MethodCallNode rootNode : rootNodes) {
            System.out.println(rootNode);
        }*/
    }

    //将给定的List<List<MethodCall>> ret有限分裂出所有的调用头，直到头再也没有头，也即是根为止
    private static List<List<MethodCall>> parseRelations(MethodCallMapper mapper, String projectName, List<List<MethodCall>> ret) {
        Set<String> chainSet = new HashSet<>();//去重set

        List<List<MethodCall>> result = new ArrayList<>();
        Boolean existUnEndedChain = false;
        for (List<MethodCall> chain : ret) {
            String chainStr = chain.stream().map(mc -> String.format("%s.%s[%s]",mc.getClassName(),mc.getMethodName(),mc.getMethodParamCount())).collect(Collectors.joining(","));
            if (chainSet.contains(chainStr)) {
                System.err.println("这里出现了重复: "+chainStr);
                continue;
            }
            chainSet.add(chainStr);
            //一分多
            List<List<MethodCall>> newChains = parseOneChain(mapper, projectName, chain);
            //如果碰到null，代表已经是完整的链条了，头上加上START
            if (newChains == null) {
                if (!Objects.equals(chain.get(0).getClassName(), "START")) {
                    chain.add(0, new MethodCall("START", "",0));
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
        if (Objects.equals(innerMethod.getCallClassMethod(), "START")) {
            return null;
        }
        Integer methodParamCount = innerMethod.getMethodParamCount();
        List<List<MethodCall>> ret = null;
        List<MethodCall> mcs = mapper.findByCallClassMethodAndProjectName(generateInnerMethod(innerMethod), projectName, methodParamCount);
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
        list.add(String.format("%s.%s",innerMethod.getClassName(),innerMethod.getMethodName()));
        if(StringUtils.isNotBlank(innerMethod.getParentClass())){
            list.add(String.format("%s.%s",innerMethod.getParentClass(),innerMethod.getMethodName()));
        }
        if(StringUtils.isNotBlank(innerMethod.getImplementsClasses())){
            for(String ic : innerMethod.getImplementsClasses().split(",")){
                list.add(String.format("%s.%s",ic,innerMethod.getMethodName()));
            }
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
