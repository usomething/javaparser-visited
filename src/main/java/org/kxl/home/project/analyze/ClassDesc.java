package org.kxl.home.project.analyze;

import lombok.Data;
import org.kxl.home.project.entity.MethodCall;

import java.util.List;

@Data
public class ClassDesc {
    //java文件中的 类名
    private String className;
    //此类定义的所有方法
    private List<MethodDesc> methodDescs;

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
                methodCalls.addAll(desc.getMethodCalls(className, projectName));
            }
        } else {
            methodCalls.add(new MethodCall(className, null, 0, null, null, null, 0,null, projectName));
        }
        return methodCalls;
    }
}
