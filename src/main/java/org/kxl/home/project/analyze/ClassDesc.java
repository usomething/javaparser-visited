package org.kxl.home.project.analyze;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.kxl.home.project.entity.MethodCall;

import java.util.List;

/**
 * 这是解析一个java类就生成一个ClassDesc的类，methodDescs是所有内部定义方法的描述
 */
@Data
public class ClassDesc {
    //java文件中的 类名
    private String className;
    //此类定义的所有方法
    private List<MethodDesc> methodDescs;
    //继承的父类
    private String parentClass;
    //实现的接口
    private List<String> implementsClasses;

    public ClassDesc(String className) {
        this.className = className;
    }


    public void addMethodDesc(MethodDesc methodDesc) {
        if (methodDescs == null) {
            methodDescs = new java.util.ArrayList<>();
        }
        methodDescs.add(methodDesc);
    }

    public List<MethodCall> getMethodCalls(String projectName) {
        List<MethodCall> methodCalls = new java.util.ArrayList<>();
        if (methodDescs != null && methodDescs.size() > 0) {
            for (MethodDesc desc : methodDescs) {
                methodCalls.addAll(desc.getMethodCalls(className, projectName, parentClass, implementsClasses));
            }
        } else {
            String implementsClassesStr = null;
            if(CollectionUtils.isNotEmpty(implementsClasses)){
                implementsClassesStr = String.join(",", implementsClasses);
            }
            methodCalls.add(new MethodCall(className, parentClass, implementsClassesStr, null, 0, null, null, null, 0,null, projectName));
        }
        return methodCalls;
    }
}
