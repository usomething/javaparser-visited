package org.kxl.home.project.analyze;

import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedLambdaConstraintType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import lombok.Data;
import org.kxl.home.project.entity.MethodCall;

import java.util.*;

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

    public static Set<String> classMethodSet = new TreeSet<>();//这个变量纯粹为了打印

    public MethodDesc(String methodName, Integer paramCount, String paramTypes) {
        this.methodName = methodName;
        this.paramCount = paramCount;
        this.paramTypes = paramTypes;
    }

    //ci是类，md是类中的方法，mce是方法中调用的方法
    public void addCallMethodDescs(String callDesc, Integer paramCount, String className, ClassOrInterfaceDeclaration ci, MethodDeclaration md, MethodCallExpr mce){
        if (callMethodDescs == null) {
            callMethodDescs = new java.util.ArrayList<>();
        }
        //PS 这里实在没办法传方法名带【签名】
        CallMethodDesc callMethodDesc = new CallMethodDesc(callDesc, paramCount);
        try {
            String resolvedMethodAndParamType = mce.resolve().getQualifiedSignature();
            //这里要解析出方法名不含参数类型，还没做
            int start = resolvedMethodAndParamType.indexOf("(");
            callMethodDesc.setClassMethod(resolvedMethodAndParamType.substring(0,start));
            //TODO 这里从resolvedMethodAndParamType解析出参数类型
            callMethodDesc.setParamsType(resolvedMethodAndParamType.substring(start+1,resolvedMethodAndParamType.length()-1));
        }catch (Exception e){
            String classMethod = className+"."+md.getNameAsString()+" -> "+mce.getName().getParentNode().toString();
            if(mce.getScope().isPresent()) {
                if(mce.getScope().get().getDataKeys().size()>0) {
                    for (DataKey key : mce.getScope().get().getDataKeys()) {
                        Object type = mce.getScope().get().getData(key);
                        if (type instanceof ResolvedReferenceType) {
                            classMethod = "refer :" + classMethod;
                        } else if (type instanceof ResolvedTypeVariable) {
                            classMethod = "var : "+classMethod;
                        } else if (type instanceof ResolvedLambdaConstraintType) {
                            classMethod = "lambda : "+classMethod;
                        } else {
                            System.out.println("unknow type: " + type);
                            System.exit(-1);
                        }
                    }
                }else{
                    classMethod = "keys size=0 :"+classMethod;
                }
            }else{
                //这里有可能是父类的方法，不完全是，这里也有可能是找不到lombok
                classMethod = "no scope : "+classMethod;
            }
            if(classMethod!=null) {
                classMethodSet.add(classMethod);
            }else{
                e.printStackTrace();
            }
        }
        callMethodDescs.add(callMethodDesc);
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
                ret.add(new MethodCall(className, methodName, paramCount, paramTypes, callDesc.getRawMethod(), callDesc.getClassMethod(), callDesc.getParamCount(), callDesc.getParamsType(), projectName));
            }
        }
        return ret;
    }
}
