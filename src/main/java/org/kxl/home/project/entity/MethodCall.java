package org.kxl.home.project.entity;

import lombok.Data;

@Data
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

    public MethodCall(String className, String methodName, Integer methodParamCount, String methodParamType, String callMethod,
                      String callClassMethod, String projectName) {
        this.className = className;
        this.methodName = methodName;
        this.methodParamCount = methodParamCount;
        this.methodParamType = methodParamType;
        this.callMethod = callMethod;
        this.callClassMethod = callClassMethod;
        this.projectName = projectName;
    }

    public String getCaller(){
        return className.substring(className.lastIndexOf(".")+1)+"."+methodName;
    }

}
