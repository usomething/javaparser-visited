package org.kxl.home.project.analyze.AUB;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import org.apache.ibatis.session.SqlSession;
import org.kxl.home.project.analyze.ClassDesc;
import org.kxl.home.project.analyze.MethodDesc;
import org.kxl.home.project.entity.MethodCall;
import org.kxl.home.project.mapper.MethodCallMapper;
import org.kxl.home.util.FileUtil;
import org.kxl.home.util.MapperUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MethodAnalyze {

    private final static String root = "C:/workspace/AutoBestChina/src/main/java";
    private static String projectName = root.contains("AutoBestChina") ? "oe-admin" : root.contains("AutobestCheckout") ? "oe-online" : "unknow";

    private final static Boolean SHOW_DUPLICATED_METHOD_NAME = true;

    private static CompilationUnit cu = null;

    static {
        if (root.contains("APP")) {
            projectName = projectName.replace("oe", "app");
        }

        StaticJavaParser
                .getParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(new CombinedTypeSolver()));
    }

    private static List<File> traverseRoot(String root) {
        return FileUtil.recurSionDir(new File(root), null);
    }

    public static void main(String[] args) throws Exception {
        List<File> files = traverseRoot(root);
        int i = 0;
        List<ClassDesc> classDescs = new java.util.ArrayList<>();
        SqlSession sqlSession = MapperUtil.getSqlSession(true);
        MethodCallMapper mapper = sqlSession.getMapper(MethodCallMapper.class);

        // 还需要解析继承关系和接口实现关系
        Map<String, String> extendsRelation = new HashMap<>();//继承关系肯定一对一
        Map<String, List<String>> implementsRelation = new HashMap<>();//接口实现关系是一对多

        for (File file : files) {
            if (!file.getName().endsWith(".java")) continue;
            ++i;
            String fileName = file.getAbsolutePath().substring(root.length() + 1);
            cu = StaticJavaParser.parse(file);

            String type = null;
            //找出这个类的解析节点
            List<Node> chilNodes = null;
            if (chilNodes == null || chilNodes.isEmpty()) {
                chilNodes = cu.getChildNodes().stream().filter(n -> n.getClass().equals(ClassOrInterfaceDeclaration.class)).collect(Collectors.toList());
                type = "Class";
            }
            if (chilNodes == null || chilNodes.isEmpty()) {
                chilNodes = cu.getChildNodes().stream().filter(n -> n.getClass().equals(EnumDeclaration.class)).collect(Collectors.toList());
                type = "Enum";
            }
            if (chilNodes == null || chilNodes.isEmpty()) {
                chilNodes = cu.getChildNodes().stream().filter(n -> n.getClass().equals(AnnotationDeclaration.class)).collect(Collectors.toList());
                type = "Annotation";
            }
            String className = null;
            //遍历这个节点,获取类的全限定名
            for (Node n : chilNodes) {
                if (Objects.equals(type, "Enum")) {
                    EnumDeclaration en = (EnumDeclaration) n;
                    className = en.getFullyQualifiedName().get();
                } else if (Objects.equals(type, "Class")) {
                    ClassOrInterfaceDeclaration ci = (ClassOrInterfaceDeclaration) n;
                    className = ci.getFullyQualifiedName().get();
                    ClassDesc classDesc = parseMethod(ci);
                    classDescs.add(classDesc);
                    parseExtendsRelation(ci, extendsRelation);
                    parseImplementsRelation(ci, implementsRelation);
                } else if (Objects.equals(type, "Annotation")) {
                    AnnotationDeclaration an = (AnnotationDeclaration) n;
                    className = an.getFullyQualifiedName().get();
                }
            }
            if (!(chilNodes.size() == 1 && Objects.equals(fileName.replace("\\", ".").replace(".java", ""), className))) {
                System.err.println(fileName + " not match");
            }
        }

        int c = 0;
        List<MethodCall> methodCalls = new ArrayList<>();
        for (ClassDesc desc : classDescs) {
            ++c;
            methodCalls.addAll(desc.getMethodCalls(projectName));
            if (methodCalls.size() >= 5000 || c == classDescs.size()) {
                mapper.saveBatch(methodCalls);
                methodCalls.clear();
            }
        }
    }

    //key:子类，value:父类
    private static void parseExtendsRelation(ClassOrInterfaceDeclaration ci, Map<String, String> extendsRelation) {
        NodeList<ClassOrInterfaceType> extendsList = ci.getExtendedTypes();
        if (extendsList.isNonEmpty()) {
            String className = ci.getFullyQualifiedName().get();
            extendsList.forEach(e -> {
                if (!extendsRelation.containsKey(className)) {
                    extendsRelation.put(className, e.getNameAsString());
                } else {
                    System.err.println("重复子类 : " + className);
                }
            });
        }
    }

    //key:子类，value:接口们
    public static void parseImplementsRelation(ClassOrInterfaceDeclaration ci, Map<String, List<String>> implementsRelation) {
        NodeList<ClassOrInterfaceType> implementsList = ci.getImplementedTypes();
        if (implementsList.isNonEmpty()) {
            String className = ci.getFullyQualifiedName().get();
            List<String> list = implementsList.stream().map(e -> e.getNameAsString()).collect(Collectors.toList());
            if (!list.isEmpty()) {
                if (implementsRelation.containsKey(className)) {
                    System.err.println("重复实现接口类:" + className);
                } else {
                    implementsRelation.put(className, list);
                }
            }
        }
    }

    private static ClassDesc parseMethod(ClassOrInterfaceDeclaration ci) {
        String className = ci.getFullyQualifiedName().get();
        ClassDesc cd = new ClassDesc(className);
        List<MethodDeclaration> methods = ci.findAll(MethodDeclaration.class).stream().collect(Collectors.toList());
        Map<String, String> filedTypeMap = parseVariables(ci);
        boolean scopeExist = false;
        Set<String> duplicateMethodName = new HashSet<>();
        for (MethodDeclaration md : methods) {
            List<MethodCallExpr> methodCallExprs = md.findAll(MethodCallExpr.class).stream().collect(Collectors.toList());
            List<String> paramList = md.getParameters().stream().map(p -> p.getType().asString()).collect(Collectors.toList());
            String paramSignature = String.join(",", paramList);
            String method = md.getNameAsString();//DONE 这里要方法签名
            if (SHOW_DUPLICATED_METHOD_NAME) {
                String methodSignature = className + "." + method + "(" + paramSignature + ")";
                if (duplicateMethodName.contains(methodSignature)) {
                    System.out.println(methodSignature);
                } else {
                    duplicateMethodName.add(methodSignature);
                }
            }
            duplicateMethodName.add(method);
            MethodDesc mdsc = new MethodDesc(method, paramList.size(), paramSignature);
            cd.addMethodDesc(mdsc);
            for (MethodCallExpr mce : methodCallExprs) {
                scopeExist = mce.getScope().isPresent();
                Integer paramCount = mce.getArguments().size();
                if (scopeExist) {
                    String rawMethod = mce.getScope().get().toString() + "." + mce.getNameAsString();//TODO 这里要方法签名
                    mdsc.addCallMethodDescs(rawMethod, filedTypeMap, className, paramCount);
                } else {
                    String rawMethod = "this." + mce.getNameAsString();
                    mdsc.addCallMethodDescs(rawMethod, filedTypeMap, className, paramCount);
                }
            }
        }
        return cd;
    }

    private static Map<String, String> parseVariables(ClassOrInterfaceDeclaration ci) {
        Map<String, String> ret = new HashMap<>();
        List<FieldDeclaration> vars = ci.findAll(FieldDeclaration.class).stream().collect(Collectors.toList());
        if (vars != null && !vars.isEmpty()) {
            for (FieldDeclaration fd : vars) {
                List<VariableDeclarator> vds = fd.getVariables();
                for (VariableDeclarator vd : vds) {
                    ret.put(vd.getNameAsString(), vd.getTypeAsString());
                }
            }
        }

        Map<String, String> repositoryMap = ServiceRepositoryParser.parse(ci);
        if (repositoryMap != null && !repositoryMap.isEmpty()) {
            ret.putAll(repositoryMap);
        }

        Map<String, String> serviceMap = ControllerServiceParser.parse(ci);
        if (serviceMap != null && !serviceMap.isEmpty()) {
            ret.putAll(serviceMap);
        }
        return ret;
    }
}