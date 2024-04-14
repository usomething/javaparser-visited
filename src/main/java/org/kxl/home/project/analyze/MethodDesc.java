package org.kxl.home.project.analyze;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class MethodDesc {

    private String methodName;

    private List<String> callDescs;

    private List<CallMethodDesc> callMethodDescs;

    public MethodDesc(String methodName) {
        this.methodName = methodName;
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
        CallMethodDesc callMethodDesc = new CallMethodDesc(callDesc);
        callMethodDesc.parseClassMethod(className, fieldTypeMap);
        callMethodDescs.add(callMethodDesc);
    }

    public List<String> generateSQLs(String className, String projectName) {
        if (callDescs == null || callDescs.size() == 0) {
            return Arrays.asList(String.format("('%s','%s','','','%s')", className, methodName, projectName));
        }
//        String format = "insert into autopart_method_call (class_name, method_name, call_method,call_class_method,call_class_method,project_name) values";
        List<String> ret = new ArrayList<>();
        for (CallMethodDesc callDesc : callMethodDescs) {
            ret.add(String.format("('%s','%s','%s','%s','%s')", className, methodName, callDesc.getRawMethod().replace("'", "\\'"), callDesc.getClassMethod(), projectName));
        }
        return ret;
    }
}
