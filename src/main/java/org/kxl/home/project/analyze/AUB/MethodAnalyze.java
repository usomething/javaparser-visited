package org.kxl.home.project.analyze.AUB;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
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

/**
 * 这是解析程序类，负责把一个工程的调用关系入库
 */
public class MethodAnalyze {


    private final static String root = "C:/workspace/OE/AutoBestChina/target/generated-sources/delombok"
//            "C:/workspace/OE/AutoBestChina/src/main/java"
            ;
    private static String projectName = root.contains("AutoBestChina") ? "oe-admin" : root.contains("AutobestCheckout") ? "oe-online" : "unknow";

    private final static Boolean SHOW_DUPLICATED_METHOD_NAME = true;

    private static CompilationUnit cu = null;

    private static CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver(
            new ReflectionTypeSolver(),
            new JavaParserTypeSolver(root)
    );

    //PS 记得先运行 mvn dependency:copy-dependencies -DoutputDirectory=lib
    static {
        if (root.contains("APP")) {
            projectName = projectName.replace("oe", "app");
        }
        init();
    }

    private static void init() {
        File pomPath = new File(root);
        while(true) {
            Long cnt = Arrays.stream(pomPath.list()).filter(f -> Objects.equals(f,"pom.xml")).count();
            if(cnt == 0){
                pomPath = pomPath.getParentFile();
            }else {
                break;
            }
        }
        File libPath = new File(pomPath.getAbsolutePath(),"lib");
        for(File lib: libPath.listFiles()){
            try {
                combinedTypeSolver.add(new JarTypeSolver(lib));
            }catch (Exception e){
                System.err.println(lib+" not found");
                System.exit(-1);
            }
        }

        List<File> jreLibs = FileUtil.recurSionDir(new File("C:\\Program Files\\Java\\jdk1.8.0_333\\jre\\lib"),null);
        for(File lib : jreLibs){
            if(lib.getPath().endsWith(".jar")){
                try {
                    combinedTypeSolver.add(new JarTypeSolver(lib));
                }catch (Exception e){
                    System.err.println(lib+" not found");
                    System.exit(-1);
                }
            }
        }

        StaticJavaParser
                .getParserConfiguration()
                .setSymbolResolver(
                        new JavaSymbolSolver(
                                combinedTypeSolver
                        )
                );
    }

    private static List<File> traverseRoot(String root) {
        return FileUtil.recurSionDir(new File(root), null);
    }

    /**
     * 运行步骤：
     * 1、记得第一步先在pom.xml里加入delombok配置，具体查询网络
     * 2、运行 mvn dependency:copy-dependencies -DoutputDirectory=lib，这会在pom.xml同级目录生成一个lib文件夹，里面是运行需要用到的所有jar包，有同名文件夹和相同jar包也无所谓
     * 3、最后再来运行这里的main方法
    **/
    public static void main(String[] args) throws Exception {
        List<File> files = traverseRoot(root);
        int i = 0;
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
                    ClassDesc classDesc = parseMethod(ci);
                    classDescs.add(classDesc);
                } else if (Objects.equals(type, "Annotation")) {
                    AnnotationDeclaration an = (AnnotationDeclaration) n;
                    className = an.getFullyQualifiedName().get();
                }
            }
            if (!(chilNodes.size() == 1 && Objects.equals(fileName.replace("\\", ".").replace(".java", ""), className))) {
                System.err.println(fileName + " not match");
            }
        }

        for(String parserErr : MethodDesc.classMethodSet) {
            System.out.println("\r\nparser error : "+parserErr);
        }

        //先把原来的数据删除
        mapper.deleteByProjectName(projectName);

        int c = 0;
        List<MethodCall> methodCalls = new ArrayList<>();
//        classDescs.clear();//TODO remove me，这里加这个完全为了调试，避免入库
        for (ClassDesc desc : classDescs) {
            ++c;
            methodCalls.addAll(desc.getMethodCalls(projectName));
            if (methodCalls.size() >= 5000 || c == classDescs.size()) {
                mapper.saveBatch(methodCalls);
                System.out.println("save : " + methodCalls.size());
                methodCalls.clear();
            }
        }
    }

    private static ClassDesc parseMethod(ClassOrInterfaceDeclaration ci) {
        String className = ci.getFullyQualifiedName().get();
        ClassDesc cd = new ClassDesc(className);
        //设置父类
        if(ci.getExtendedTypes().isNonEmpty()) {
            cd.setParentClass(Optional.ofNullable(ci.getExtendedTypes(0)).map(p -> p.resolve().asReferenceType().getQualifiedName()).orElse(null));
        }
        //设置实现接口
        if(ci.getImplementedTypes().isNonEmpty()){
            cd.setImplementsClasses(ci.getImplementedTypes().stream().map(i->i.resolve().asReferenceType().getQualifiedName()).collect(Collectors.toList()));
        }
        List<MethodDeclaration> methods = ci.findAll(MethodDeclaration.class).stream().collect(Collectors.toList());
        boolean scopeExist = false;
        Set<String> duplicateMethodName = new HashSet<>();
        for (MethodDeclaration md : methods) {
            List<String> paramList = md.getParameters().stream().map(p -> p.getType().asString()).collect(Collectors.toList());
            String paramSignature = String.join(",", paramList);
            String method = md.getNameAsString();//DONE 这里要方法签名
            //如果有重复方法，旧显示出来，这里并没什么逻辑
            if (SHOW_DUPLICATED_METHOD_NAME) {
                String methodSignature = className + "." + method + "(" + paramSignature + ")";
                if (duplicateMethodName.contains(methodSignature)) {
                    System.out.println("duplicateed : "+methodSignature);
                } else {
                    duplicateMethodName.add(methodSignature);
                }
            }
            duplicateMethodName.add(method);
            MethodDesc mdsc = new MethodDesc(method, paramList.size(), paramSignature);
            //把本类中所有定义的方法都加入到methodDescs这个集合中
            cd.addMethodDesc(mdsc);
            List<MethodCallExpr> methodCallExprs = md.findAll(MethodCallExpr.class).stream().collect(Collectors.toList());
            for (MethodCallExpr mce : methodCallExprs) {
                //scope 是这个方法的主人，比如a.x(15)，方法是x,参数是15，而主人是a
                scopeExist = mce.getScope().isPresent();
                Integer paramCount = mce.getArguments().size();
                if (scopeExist) {
                    // 这里的方法签名很难给，需要解析所有参数的类型，那就要扫描本方法内的变量定义，入参方法定义，以及成员变量定义，还有全局变量定义
                    String rawMethod = mce.getScope().get().toString() + "." + mce.getNameAsString();//TODO 这里要方法签名
                    mdsc.addCallMethodDescs(rawMethod,paramCount,className,ci,md,mce);
                } else {
                    //没有scope说明调用的就是本类内的方法
                    String rawMethod = "this." + mce.getNameAsString();
                    mdsc.addCallMethodDescs(rawMethod,paramCount,className,ci,md,mce);
                }
            }
        }
        return cd;
    }
}