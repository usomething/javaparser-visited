package org.kxl.home.project.analyze;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class CallMethodDesc {

    //原始方法调用表达式
    private String rawMethod;//PS 这里很难带上签名

    private Integer paramCount;//虽然带上方法签名很难，但是第一步，先带上参数个数，能解决80%以上的问题

    //类方法调用表达式
    private String classMethod = "";

    private String paramsType;

//    private static Map<String, Pattern> patternMap = new HashMap<>();

    public CallMethodDesc(String rawMethod, Integer paramCount) {
        this.rawMethod = rawMethod;
        this.paramCount = paramCount;
    }

    /**
     * 把调用方法的换成全限定类名+方法，这样更精确
     * @param className
     * @param fieldTypeMap
     */
    /*public void parseClassMethod(String className, String methodName, Map<String, String> fieldTypeMap, Map<String, String> methodParamTypeMap, MethodDeclaration md, MethodCallExpr mce) {
        if (fieldTypeMap == null || fieldTypeMap.isEmpty()) return;

        Map<String, String> paramTypeMap = new HashMap<>();
        paramTypeMap.putAll(fieldTypeMap);
        paramTypeMap.putAll(methodParamTypeMap);

        boolean find = false;
        for (Map.Entry<String, String> me : paramTypeMap.entrySet()) {
            String varName = me.getKey();
            String varType = me.getValue();

            Pattern pattern = Pattern.compile("\\b" + varName + "\\.\\b");
            Matcher m = pattern.matcher(rawMethod);
            if (find = m.find()) {//把成员变量.方法名，变成类名.方法名，比如orderRep.find 变成 OrderRepository.find
                classMethod = varType + ".";
                if (m.end() + 1 >= rawMethod.length()) {
                    //成员变量有一个userName，是String，注入字符串的，方法调用有一个Utils.userName，这两个正好对上了，仅oe-admin碰到这一起案例：EmailServiceImpl.sendInternationalOrderWireTransferEmail方法中
                    System.err.println(String.format("className:%s & rawMethod:%s & varName pattern:%s",className,rawMethod,varName));
                    continue;//可能找到内部中有一个匹配，但是超出了substring范围，说明找到的一定错误，直接忽略
                }
                String methodSignature = rawMethod.substring(m.end());
                int minEnd = getMinEnd(methodSignature);
                classMethod += methodSignature.substring(0, minEnd);
                break;
            }else{
                pattern = Pattern.compile("\\b" + varName + "\\[\\b");
                m = pattern.matcher(rawMethod);
                if (find = m.find()){
                    classMethod = varType + ".";
                    String methodSignature = rawMethod.substring(rawMethod.indexOf(".",m.end())+1);
                    int minEnd = getMinEnd(methodSignature);
                    classMethod += methodSignature.substring(0, minEnd);
                    break;
                }
            }
        }

        if (!find) {
            if (rawMethod.startsWith("this.")) {
                if (rawMethod.startsWith("this.repository")) {

                } else if (rawMethod.startsWith("this.service")) {

                } else {
                    //把this替换成本类
                    classMethod = className.substring(className.lastIndexOf(".") + 1) + ".";
                    //这里把this. 替换掉
                    String methodSignature = rawMethod.replace("this.", "");//rawMethod.substring(rawMethod.indexOf(".", 5) + 1);
                    int minEnd = getMinEnd(methodSignature);
                    classMethod += methodSignature.substring(0, minEnd);
                }
            } else {//这里非常有可能调用的不是成员变量，那就按照原来的样子写入吧，有可能是调用静态方法哦
                //猜测是字符串字面量
                Pair<Boolean,String> pair = convertStr(rawMethod);
                if(pair.getLeft()){
                    classMethod = pair.getRight();
                    return;
                }
                //猜测是catch语句块
                CatchClause catchClause = mce.findAncestor(CatchClause.class).orElse(null);
                if(catchClause!=null){
                    String expType = catchClause.getParameter().getTypeAsString();
                    classMethod = expType + rawMethod.substring(rawMethod.indexOf("."));
                    return;
                }
                //猜测是全限定名调用
                int firstDotIndex = rawMethod.indexOf(".");
                if(firstDotIndex > 0){
                    if(rawMethod.indexOf(".",firstDotIndex+1) > 0){
                        classMethod = rawMethod;
                        return;
                    }
                }
                //猜测是new XXXX()的语句块
                ObjectCreationExpr creationExpr = mce.findAll(ObjectCreationExpr.class).stream().findFirst().orElse(null);
                if(creationExpr!=null){
                    String expType = creationExpr.getTypeAsString();
                    classMethod = expType + rawMethod.substring(rawMethod.indexOf("."));
                    return;
                }

                if(isFirstCharLowcase(rawMethod) && !rawMethod.endsWith("hashCode") && !rawMethod.endsWith("equals")) {
                    System.err.println(String.format("cannot deal: %s.%s -> %s", className, methodName, rawMethod));
                }
                classMethod = rawMethod;//TODO 这里要完善的，必须找到scope对应类，这里类如果是本方法入参，本方法局部变量，本类成员变量都可以解析，唯独全局变量
            }
        }
    }

    private static int getMinEnd(String next) {//TODO 这里要把方法签名也匹配出来
        List<Integer> list = new ArrayList<>();
        int end = next.indexOf("(");
        if (end != -1) {
            list.add(end);
        }
        int end2 = next.indexOf(".");
        if (end2 != -1) {
            list.add(end2);
        }
        int end3 = next.length();
        if (end3 != -1) {
            list.add(end3);
        }
        int minEnd = list.stream().sorted().collect(Collectors.toList()).get(0);
        return minEnd;
    }

    private static Pair<Boolean,String> convertStr(String rawMethod){
        int len = rawMethod.length();
        boolean find = false;
        String ret = rawMethod;
        for(int i=0;i<len;++i){
            if(Objects.equals(rawMethod.charAt(i),'"')){
                if (find) {
                    ret = String.format("String.%s",rawMethod.substring(i+2));
                    break;
                }
                find = true;
            }
        }
        return Pair.of(!Objects.equals(rawMethod,ret),ret);
    }

    private static boolean isFirstCharLowcase(String str){
        Character c = str.charAt(0);
        return Objects.equals(Character.toLowerCase(c),c);
    }
*/
}
