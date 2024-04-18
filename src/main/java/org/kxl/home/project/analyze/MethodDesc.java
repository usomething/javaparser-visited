package org.kxl.home.project.analyze;

import lombok.Data;
import org.kxl.home.project.entity.MethodCall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class MethodDesc {

    private String methodName;

    private Integer paramCount;

    private String paramTypes;

    private List<String> callDescs;

    private List<CallMethodDesc> callMethodDescs;

    public MethodDesc(String methodName, Integer paramCount, String paramTypes) {
        this.methodName = methodName;
        this.paramCount = paramCount;
        this.paramTypes = paramTypes;
    }

    public void addCallDesc(String callDesc) {
        if (callDescs == null) {
            callDescs = new java.util.ArrayList<>();
        }
        callDescs.add(callDesc);
    }

    public void addCallMethodDescs(String callDesc, Map<String, String> fieldTypeMap, String className) {
        if (callMethodDescs == null) {
            callMethodDescs = new java.util.ArrayList<>();
        }
        //PS 这里实在没办法传方法名带【签名】
        CallMethodDesc callMethodDesc = new CallMethodDesc(callDesc);
        callMethodDesc.parseClassMethod(className, fieldTypeMap);
        callMethodDescs.add(callMethodDesc);
    }

    public List<String> generateSQLs(String className, String projectName) {
        if (callDescs == null || callDescs.size() == 0) {
            return Arrays.asList(String.format("('%s','%s',0,'','','','%s')", className, methodName, projectName));
        }
//        String format = "insert into autopart_method_call (class_name, method_name, method_param_count, method_param_type, call_method,call_class_method,call_class_method,project_name) values";
        List<String> ret = new ArrayList<>();
        for (CallMethodDesc callDesc : callMethodDescs) {
            ret.add(String.format("('%s','%s','%s','%s','%s','%s','%s')", className, methodName, paramCount, paramTypes, callDesc.getRawMethod().replace("'", "\\'"), callDesc.getClassMethod(), projectName));
        }
        return ret;
    }

    public List<MethodCall> getMethodCalls(String className, String projectName) {
        if (callDescs == null || callDescs.size() == 0) {
            return Arrays.asList(new MethodCall(className, methodName, 0, null, null, null, projectName));
        }
        List<MethodCall> ret = new ArrayList<>();
        for (CallMethodDesc callDesc : callMethodDescs) {
            ret.add(new MethodCall(className, methodName, paramCount, paramTypes, callDesc.getRawMethod(),callDesc.getClassMethod(), projectName));
        }
        return ret;
    }
}
