package org.kxl.home.project.entity;

import lombok.Data;

@Data
public class MethodCall {

    private Integer id;

    private String className;

    private String methodName;

    private String callMethod;

    private String callClassMethod;

    private String projectName;

}
