package org.kxl.home.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 这是要入库的的记录，一条代表一个方法调用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodCall {

    private Integer id;

    private String className;

    private String methodName;

    private Integer methodParamCount;

    private String methodParamType;

    private String callMethod;

    private String callClassMethod;

    private Integer callMethodParamCount;

    private String callMethodParamType;

    private String projectName;

    public MethodCall(String callClassMethod, Integer callMethodParamCount) {
        this.callClassMethod = callClassMethod;
        this.callMethodParamCount = callMethodParamCount;
    }

    public MethodCall(String className, String methodName, Integer methodParamCount, String methodParamType, String callMethod,
                      String callClassMethod, Integer callMethodParamCount, String callMethodParamType, String projectName) {
        this.className = className;
        this.methodName = methodName;
        this.methodParamCount = methodParamCount;
        this.methodParamType = methodParamType;
        this.callMethod = callMethod;
        this.callClassMethod = callClassMethod;
        this.callMethodParamCount = callMethodParamCount;
        this.callMethodParamType = callMethodParamType;
        this.projectName = projectName;
    }

    public String getCaller() {
        return className.substring(className.lastIndexOf(".") + 1) + "." + methodName;
    }

    public MethodCall getCallerClass() {
        MethodCall caller = new MethodCall();
        caller.setCallClassMethod(className.substring(className.lastIndexOf(".") + 1) + "." + methodName);
        caller.setCallMethodParamCount(methodParamCount);
        return caller;
    }

}
