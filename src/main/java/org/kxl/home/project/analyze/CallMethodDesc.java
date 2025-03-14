package org.kxl.home.project.analyze;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 这是一个被调用方法的描述，包括方法名，参数个数，调用方法所属类名以及参数类型，其实就是方法签名
 */
@Data
public class CallMethodDesc {

    //原始方法调用表达式
    private String rawMethod;//PS 这里很难带上签名

    private Integer paramCount;//虽然带上方法签名很难，但是第一步，先带上参数个数，能解决80%以上的问题

    //类方法调用表达式
    private String classMethod = "";

    private String paramsType;

    public CallMethodDesc(String rawMethod, Integer paramCount) {
        this.rawMethod = rawMethod;
        this.paramCount = paramCount;
    }


}
