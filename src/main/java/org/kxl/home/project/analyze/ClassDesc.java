package org.kxl.home.project.analyze;

import lombok.Data;

import java.util.List;

@Data
public class ClassDesc {

    private String className;

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

    public String printString() {
        String str = String.format("%s", className);
        if (methodDescs != null) {
            for (MethodDesc desc : methodDescs) {
                str += "\r\n\t" + desc.getMethodName();
                if (desc.getCallDescs() != null) {
                    str += " -> ";
                    for (String callDesc : desc.getCallDescs()) {
                        str += "\r\n\t\t[call]: " + callDesc;
                    }
                }
            }
        }
        str += "\r\n";
        return str;
    }

    public List<String> generateSQLs(String projectName) {
        List<String> sqls = new java.util.ArrayList<>();
        if (methodDescs != null && methodDescs.size() > 0) {
            for (MethodDesc desc : methodDescs) {
                sqls.addAll(desc.generateSQLs(className, projectName));
            }
        } else {
            sqls.add(String.format("('%s','','','','%s')", className, projectName));
        }
        return sqls;
    }
}
