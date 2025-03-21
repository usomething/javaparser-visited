package org.kxl.home.project.mapper;

import org.apache.ibatis.annotations.Param;
import org.kxl.home.project.entity.MethodCall;

import java.util.List;

public interface MethodCallMapper {

    List<MethodCall> findByCallClassMethodAndProjectName(@Param("callDescs") List<String> callClassMethod, @Param("projectName") String projectName, @Param("callMethodParamCount") Integer callMethodParamCount);

    List<MethodCall> findByClassNameAndMethodNameAndParamCount(@Param("className")String className, @Param("methodName")String methodName, @Param("methodParamCount")Integer methodParamCount, @Param("projectName") String projectName);

    List<MethodCall> findByCallClassLikeMethodAndProjectName(@Param("callDesc") String callClassMethod, @Param("projectName") String projectName);

    Integer saveBatch(@Param("list") List<MethodCall> list);

    Integer deleteByProjectName(@Param("projectName") String projectName);
}
