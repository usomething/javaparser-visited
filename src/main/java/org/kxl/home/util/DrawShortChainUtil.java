package org.kxl.home.util;

import org.kxl.home.project.entity.MethodCall;
import org.kxl.home.project.entity.MethodNode;

import java.util.*;
import java.util.stream.Collectors;

public class DrawShortChainUtil {

    public static List<MethodCall> drawShortChain(List<String> methodCalls) {
        String chain = null;
        Map<String, MethodNode> nodeMap = new HashMap<>();
        String [] chains = null;
        String parent = null;
        String node = null;
        MethodNode methodNode = null,parentNode = null;
        for (String _chain : methodCalls) {
            chain = _chain.replace("START -> ","");
            chains = chain.split("->");
            for(int i=0;i<chains.length;i++){
                node = chains[i].trim();
                if(!nodeMap.containsKey(node)){
                    nodeMap.put(node, new MethodNode(node));
                }
                methodNode = nodeMap.get(node);
                //这里只解析父到子的关系
                if(i>0){
                    parent = chains[i-1].trim();
                    parentNode = nodeMap.get(parent);
                    methodNode.addParent(parentNode);
                    parentNode.addChild(methodNode);
                }
            }
        }

        List<MethodNode> parentNodes = nodeMap.values().stream().filter(x->x.getParents()==null).collect(Collectors.toList());
        for(MethodNode chainNode : parentNodes){
            //TODO 这里开始打印每一根链条
        }


        return null;
    }

    private static void printMethodChain(MethodNode node) {
        int maxLength = 0;

    }

    private static int getMaxLen(MethodNode node,int size){
        List<MethodNode> children = node.getChildren();
        size+=1;
        if(children==null){
            return size;
        }
        int childSize = 0 ,tmp = 0;
        for(MethodNode child : children){
            tmp = getMaxLen(child,size);
            if(tmp>childSize){
                childSize = tmp;
            }
        }
        return childSize;
    }

    private static int getMaxHeight(List<MethodNode> nodes,int parentHeight){
        int selfHeight = nodes.size();
        int maxHeight = selfHeight>parentHeight?selfHeight:parentHeight;
        List<MethodNode> children = new ArrayList<>();
        for(MethodNode node : nodes){
            if(node.getChildren()!=null){
                children.addAll(node.getChildren());
            }
        }
        if(children.size() > 0){
            return getMaxHeight(children,maxHeight);
        }else {
            return maxHeight;
        }

    }

    private static MethodNode getRandNode(int length,int maxChildCount){
        MethodNode node = new MethodNode("0") , temp = node;
        Random r = new Random();
        List<MethodNode> children = new ArrayList<>();
        for(int i=1;i<=length;++i){
            int childCount = r.nextInt(maxChildCount);
            if(childCount==0){
                childCount = 1;
            }
            for(int c=0;c<childCount;++c){
                children.add(new MethodNode(i+"_"+c));
            }
            temp.setChildren(new ArrayList<>());
            temp.getChildren().addAll(children);
            int randChildIdx = r.nextInt(temp.getChildren().size());
            System.out.println("第"+i+"层有"+(childCount)+"个子节点");
            if(randChildIdx>0 && randChildIdx<temp.getChildren().size()){
                temp.getChildren().get(randChildIdx-1).addChild(new MethodNode("random child"));
                /*if(i==length){
                    System.out.println("总长度"+(length+2));
                }*/
                System.out.println("        "+(i+1)+"层加1");
            }/*else{
                if(i==length){
                    System.out.println("总长度"+(length+1));
                }
            }*/

            temp = temp.getChildren().get(randChildIdx);
            children.clear();
        }
        /*int len = getMaxLen(node,0);
        System.out.println(len);
        getMaxLen(node,0);*/
        return node;
    }

    public static void main(String[] args) {
        /*MethodNode root = getRandNode(12,64);
        System.out.println(getMaxHeight(Arrays.asList(root),0));
        System.out.println("HOLD ON");*/

        List<String> contents = FileUtil.readFile("C:/Users/Administrator/Desktop/oe-admin.txt");
        List<String> filterContents = contents.stream().filter(c -> c.startsWith("START ->") && !c.contains("无此方法") && !c.contains("这是")).collect(Collectors.toList());
        drawShortChain(filterContents);
    }

}
