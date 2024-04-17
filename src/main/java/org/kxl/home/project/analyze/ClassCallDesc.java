package org.kxl.home.project.analyze;

import lombok.Data;

import java.util.List;

@Data
public class ClassCallDesc {

    private String className;

    private String methodName;

    private Integer paramCount;

    private String paramTypes;

    private List<ClassCallDesc> calledMethods;

    public ClassCallDesc(String className, String methodName, Integer paramCount, String paramTypes) {
        this.className = className;
        this.methodName = methodName;
        this.paramCount = paramCount;
        this.paramTypes = paramTypes;
    }

    public ClassCallDesc(String className, String methodName, Integer paramCount) {
        this.className = className;
        this.methodName = methodName;
        this.paramCount = paramCount;
    }

    /*public ClassCallDesc(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }*/

    public void addCalledMethod(ClassCallDesc calledMethod) {
        if (calledMethods == null) {
            calledMethods = new java.util.ArrayList<>();
        }
        calledMethods.add(calledMethod);
    }

    public String printString() {
        String str = String.format("%s.%s(%s)", className, methodName, paramTypes);
        if (calledMethods != null) {
            str += " -> ";
            for (ClassCallDesc desc : calledMethods) {
                str += "\r\n\t" + desc.getClassName() + "." + desc.getMethodName();
            }
        }
        str += "\r\n";
        return str;
    }
}
