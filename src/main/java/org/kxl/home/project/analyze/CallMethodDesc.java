package org.kxl.home.project.analyze;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class CallMethodDesc {

    //原始方法调用表达式
    private String rawMethod;

    //类方法调用表达式
    private String classMethod = "";

    private static Map<String, Pattern> patternMap = new HashMap<>();

    public CallMethodDesc(String rawMethod) {
        this.rawMethod = rawMethod;
    }

    public void parseClassMethod(String className, Map<String, String> fieldTypeMap) {
        if (fieldTypeMap == null || fieldTypeMap.isEmpty()) return;
        boolean find = false;
        for (Map.Entry<String, String> me : fieldTypeMap.entrySet()) {
            String varName = me.getKey();
            String varType = me.getValue();

            String key = String.format("%s.%s", className, varName);

            if (!patternMap.containsKey(varName)) {
                Pattern pattern = Pattern.compile("\\b" + varName + "\\b");
                patternMap.put(key, pattern);
            }
            Pattern p = patternMap.get(key);
            Matcher m = p.matcher(rawMethod);
            if (find = m.find()) {
                classMethod = varType + ".";
                if (m.end() + 1 >= rawMethod.length()) {
                    continue;//可能找到内部中有一个匹配，但是超出了substring范围，说明找到的一定错误，直接忽略
                }
                String next = rawMethod.substring(m.end() + 1);
                int minEnd = getMinEnd(next);
                classMethod += next.substring(0, minEnd);
                break;
            }

        }
        if (!find) {
            if (rawMethod.startsWith("this.")) {
                if (rawMethod.startsWith("this.repository")) {

                } else if (rawMethod.startsWith("this.service")) {

                } else {
                    //把this替换成本类
                    classMethod = className.substring(className.lastIndexOf(".") + 1) + ".";
                    String next = rawMethod.substring(rawMethod.indexOf(".", 5) + 1);
                    int minEnd = getMinEnd(next);
                    classMethod += next.substring(0, minEnd);
                }
            }
        }
    }

    private static int getMinEnd(String next) {
        List<Integer> list = new ArrayList<>();
        int end = next.indexOf("(");
        if (end != -1) {
            list.add(end);
        }
        int end2 = next.indexOf(".");
        if (end2 != -1) {
            list.add(end2);
        }
        int end3 = next.length();
        if (end3 != -1) {
            list.add(end3);
        }
        int minEnd = list.stream().sorted().collect(Collectors.toList()).get(0);
        return minEnd;
    }

}
