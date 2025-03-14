package org.kxl.home.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 这是要入库的的记录，一条代表一个方法调用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodCall {

    private Integer id;

    private String className;

    private String parentClass;

    private String implementsClasses;

    private String methodName;

    private Integer methodParamCount;

    private String methodParamType;

    private String callMethod;

    private String callClassMethod;

    private Integer callMethodParamCount;

    private String callMethodParamType;

    private String projectName;

    public MethodCall(String className,String methodName,Integer methodParamCount){
        this.className = className;
        this.methodName = methodName;
        this.methodParamCount = methodParamCount;
    }

    public MethodCall(String className, String parentClass, String implementsClasses, String methodName, Integer methodParamCount, String methodParamType, String callMethod,
                      String callClassMethod, Integer callMethodParamCount, String callMethodParamType, String projectName) {
        this.className = className;
        this.parentClass = parentClass;
        this.implementsClasses = implementsClasses;
        this.methodName = methodName;
        this.methodParamCount = methodParamCount;
        this.methodParamType = methodParamType;
        this.callMethod = callMethod;
        this.callClassMethod = callClassMethod;
        this.callMethodParamCount = callMethodParamCount;
        this.callMethodParamType = callMethodParamType;
        this.projectName = projectName;
    }

    public MethodCall getCallerClass() {
        MethodCall m = new MethodCall();
        m.setId(id);
        m.setClassName(className);
        m.setParentClass(parentClass);
        m.setImplementsClasses(implementsClasses);
        m.setMethodName(methodName);
        m.setMethodParamCount(methodParamCount);
        m.setMethodParamType(methodParamType);
        m.setCallMethod(callMethod);
        m.setCallClassMethod(callClassMethod);
        m.setCallMethodParamCount(callMethodParamCount);
        m.setCallMethodParamType(callMethodParamType);
        m.setProjectName(projectName);
        return m;
    }

    public String getSimpleClassName(){
        if(StringUtils.isNotBlank(className) && className.indexOf(".")>0){
            int dotPos = className.lastIndexOf(".");
            return className.substring(dotPos+1);
        }
        return className;
    }

}
