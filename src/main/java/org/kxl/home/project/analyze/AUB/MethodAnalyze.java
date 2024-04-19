package org.kxl.home.project.analyze.AUB;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.utils.Pair;
import org.apache.ibatis.session.SqlSession;
import org.kxl.home.project.analyze.ClassCallDesc;
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

    private final static String root = "C:/workspace/OE/AutoBestChina/src/main/java";
    private final static String projectName = root.contains("AutoBestChina")?"oe-admin":root.contains("AutobestCheckout")?"oe-online":"unknow";

    private final static Boolean SHOW_DUPLICATED_METHOD_NAME = true;

    private static CompilationUnit cu = null;

    static {
        if(root.contains("APP")){
            projectName = projectName.replace("oe","app");
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
        List<ClassCallDesc> allDescs = new java.util.ArrayList<>();
        List<ClassDesc> classDescs = new java.util.ArrayList<>();
        SqlSession sqlSession = MapperUtil.getSqlSession(true);
        MethodCallMapper mapper = sqlSession.getMapper(MethodCallMapper.class);

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
                    Pair<List<ClassCallDesc>, ClassDesc> pair = parseMethod(ci);
                    allDescs.addAll(pair.a);
                    classDescs.add(pair.b);
                } else if (Objects.equals(type, "Annotation")) {
                    AnnotationDeclaration an = (AnnotationDeclaration) n;
                    className = an.getFullyQualifiedName().get();
                }
            }
            if (!(chilNodes.size() == 1 && Objects.equals(fileName.replace("\\", ".").replace(".java", ""), className))) {
                System.err.println(fileName + " not match");
            }
        }

        List<String> contents = new ArrayList<>();
        List<String> sqls = new ArrayList<>();
        for (ClassCallDesc desc : allDescs) {
            contents.add(desc.printString());
        }
        FileUtil.writeFile("C:/workspace/javaparser-visited/src/main/java/org/kxl/home/project/analyze/methods.txt", contents, true);
        contents.clear();
        String sqlInsert = "insert into autopart_method_call (class_name, method_name, method_param_count, method_param_type, call_method,call_class_method,project_name) values";
        int c = 0;
        List<String> sqlPart = new ArrayList<>();
        List<MethodCall> methodCalls = new ArrayList<>();
        for (ClassDesc desc : classDescs) {
            ++c;
            contents.add(desc.printString());

            sqlPart.addAll(desc.generateSQLs(projectName));
            methodCalls.addAll(desc.getMethodCalls(projectName));
            if (sqlPart.size() >= 5000 || c == classDescs.size()) {
                sqls.add(String.format("%s %s;", sqlInsert, String.join(",", sqlPart)));
                sqlPart.clear();
            }
            if(methodCalls.size() >= 5000 || c == classDescs.size()){
                mapper.saveBatch(methodCalls);
                methodCalls.clear();
            }
        }
        FileUtil.writeFile("C:/workspace/javaparser-visited/src/main/java/org/kxl/home/project/analyze/methods2.txt", contents, true);
        FileUtil.writeFile("C:/workspace/javaparser-visited/src/main/java/org/kxl/home/project/analyze/sql.txt", sqls, true);
    }

    private static Pair<List<ClassCallDesc>, ClassDesc> parseMethod(ClassOrInterfaceDeclaration ci) {
        List<ClassCallDesc> descs = new ArrayList<>();
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
            ClassCallDesc desc = new ClassCallDesc(className, method, paramList.size(), paramSignature);
            MethodDesc mdsc = new MethodDesc(method, paramList.size(), paramSignature);
            cd.addMethodDesc(mdsc);
            for (MethodCallExpr mce : methodCallExprs) {
                scopeExist = mce.getScope().isPresent();
                Integer paramCount = mce.getArguments().size();
                if (scopeExist) {
                    desc.addCalledMethod(new ClassCallDesc(mce.getScope().get().toString(), mce.getNameAsString(), paramCount));
                    String rawMethod = mce.getScope().get().toString() + "." + mce.getNameAsString();//TODO 这里要方法签名
                    mdsc.addCallDesc(rawMethod);
                    mdsc.addCallMethodDescs(rawMethod, filedTypeMap, className);
                } else {
                    desc.addCalledMethod(new ClassCallDesc("this", mce.getNameAsString(), paramCount));
                    String rawMethod = "this." + mce.getNameAsString();
                    mdsc.addCallDesc(rawMethod);
                    mdsc.addCallMethodDescs(rawMethod, filedTypeMap, className);
                }
            }
            descs.add(desc);
        }
        return new Pair<>(descs, cd);
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