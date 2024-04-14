package org.kxl.home.project.analyze;

import lombok.Data;

import java.util.List;

@Data
public class MethodDesc {

    private String methodName;

    private List<String> callDescs;

    public MethodDesc(String methodName) {
        this.methodName = methodName;
    }

    public void addCallDesc(String callDesc) {
        if (callDescs == null) {
            callDescs = new java.util.ArrayList<>();
        }
        callDescs.add(callDesc);
    }

    public String generateSQLs(String className, String projectName) {
        if (callDescs == null || callDescs.size() == 0) {
            return String.format("insert into autopart_method_call (class_name, method_name, call_method,project_name) values ('%s','%s','','%s');\r\n", className, methodName, projectName);
        }
        String format = "insert into autopart_method_call (class_name, method_name, call_method,project_name) values\r\n";
        int i = 0;
        for (String callDesc : callDescs) {
            ++i;
            format += String.format("('%s','%s','%s','%s')", className, methodName, callDesc.replace("'", "\\'"), projectName);
            if (i < callDescs.size()) {
                format += ",\r\n";
            } else {
                format += ";\r\n";
            }
        }
        return format;
    }
}
