package org.kxl.home.project.analyze;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import lombok.Data;
import org.kxl.home.project.entity.MethodCall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class MethodDesc {
    //方法名
    private String methodName;
    //方法参数数量
    private Integer paramCount;
    //方法参数类型
    private String paramTypes;
    //此方法调用的其他方法
    private List<CallMethodDesc> callMethodDescs;

    public MethodDesc(String methodName, Integer paramCount, String paramTypes) {
        this.methodName = methodName;
        this.paramCount = paramCount;
        this.paramTypes = paramTypes;
    }

    /**
     * 加入调用链，被调用的方法们都进入callMethodDescs
     *
     * @param callDesc
     * @param fieldTypeMap
     * @param className
     * @param paramCount
     */
    public void addCallMethodDescs(String callDesc, Map<String, String> fieldTypeMap, Map<String,String> methodParamTypeMap, String className, Integer paramCount, MethodDeclaration md, MethodCallExpr mce) {
        if (callMethodDescs == null) {
            callMethodDescs = new java.util.ArrayList<>();
        }
        //PS 这里实在没办法传方法名带【签名】
        CallMethodDesc callMethodDesc = new CallMethodDesc(callDesc, paramCount);
        callMethodDesc.parseClassMethod(className, methodName, fieldTypeMap, methodParamTypeMap, md, mce);
        callMethodDescs.add(callMethodDesc);
    }

    //生成要DB保存的类
    public List<MethodCall> getMethodCalls(String className, String projectName) {
        List<MethodCall> ret = new ArrayList<>();
        if(callMethodDescs!=null) {
            for(CallMethodDesc callDesc : callMethodDescs) {
                ret.add(new MethodCall(className, methodName, paramCount, paramTypes, callDesc.getRawMethod(), callDesc.getClassMethod(), callDesc.getParamCount(), projectName));
            }
        }
        return ret;
    }
}
