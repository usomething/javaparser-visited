package org.kxl.home.project.analyze.AUB;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.utils.Pair;
import org.kxl.home.project.analyze.ClassCallDesc;
import org.kxl.home.project.analyze.ClassDesc;
import org.kxl.home.project.analyze.MethodDesc;
import org.kxl.home.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodAnalyze {

    private final static String root = "C:/workspace/AutobestCheckout/src/main/java";

    private static CompilationUnit cu = null;

    static {
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
                System.err.println(fileName + "not match");
            }
        }

        List<String> contents = new ArrayList<>();
        List<String> sqls = new ArrayList<>();
        for (ClassCallDesc desc : allDescs) {
            contents.add(desc.printString());
        }
        FileUtil.writeFile("C:/workspace/javaparser-visited/src/main/java/org/kxl/home/project/analyze/methods.txt", contents, true);

        contents.clear();
        for (ClassDesc desc : classDescs) {
            contents.add(desc.printString());
            sqls.addAll(desc.generateSQLs("checkout"));
        }
        FileUtil.writeFile("C:/workspace/javaparser-visited/src/main/java/org/kxl/home/project/analyze/methods2.txt", contents, true);
        FileUtil.writeFile("C:/workspace/javaparser-visited/src/main/java/org/kxl/home/project/analyze/sql.txt", sqls, true);
    }

    private static Pair<List<ClassCallDesc>, ClassDesc> parseMethod(ClassOrInterfaceDeclaration ci) {
        List<ClassCallDesc> descs = new ArrayList<>();
        String className = ci.getFullyQualifiedName().get();
        ClassDesc cd = new ClassDesc(className);
        List<MethodDeclaration> methods = ci.findAll(MethodDeclaration.class).stream().collect(Collectors.toList());
        boolean scopeExist = false;
        for (MethodDeclaration md : methods) {
            List<MethodCallExpr> methodCallExprs = md.findAll(MethodCallExpr.class).stream().collect(Collectors.toList());
            String method = md.getNameAsString();
            ClassCallDesc desc = new ClassCallDesc(className, method);
            MethodDesc mdsc = new MethodDesc(method);
            cd.addMethodDesc(mdsc);
            for (MethodCallExpr mce : methodCallExprs) {
                scopeExist = mce.getScope().isPresent();
                if (scopeExist) {
                    desc.addCalledMethod(new ClassCallDesc(mce.getScope().get().toString(), mce.getNameAsString()));
                    mdsc.addCallDesc(mce.getScope().get().toString() + "." + mce.getNameAsString());
                } else {
                    desc.addCalledMethod(new ClassCallDesc("this", mce.getNameAsString()));
                    mdsc.addCallDesc("this." + mce.getNameAsString());
                }
            }

            descs.add(desc);

        }

        return new Pair<>(descs, cd);
    }
}
