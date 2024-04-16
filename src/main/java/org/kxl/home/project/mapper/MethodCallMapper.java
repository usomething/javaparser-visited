package org.kxl.home.project.mapper;

import org.apache.ibatis.annotations.Param;
import org.kxl.home.project.entity.MethodCall;

import java.util.List;

public interface MethodCallMapper {

    List<MethodCall> findByCallClassMethodAndProjectName(@Param("callDesc") String callClassMethod, @Param("projectName") String projectName);
}