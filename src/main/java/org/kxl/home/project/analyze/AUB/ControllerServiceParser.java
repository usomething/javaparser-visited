package org.kxl.home.project.analyze.AUB;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.Type;

import java.util.HashMap;
import java.util.Map;

public class ControllerServiceParser {

    public static Map<String, String> parse(ClassOrInterfaceDeclaration ci) {
        Map<String, String> map = new HashMap<>();

        ci.getExtendedTypes().forEach(t -> {
            NodeList<Type> typesList = t.getTypeArguments().orElse(null);
            if (typesList != null) {
                typesList.stream().forEach(t1 -> {
                    String key = t1.getElementType().toString();
                    if (key.endsWith("Service")) {
                        map.put("this.service", key);
                    }
                });
            }
        });
        return map;
    }
}
