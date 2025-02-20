package org.kxl.home.project.analyze.AUB;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.kxl.home.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LombokSupplement {

    private final static String root = "C:/workspace/OE/AutoBestChina/src/main/java"//+"/com/autobest/backend/database/entities"
            ;

    private static CompilationUnit cu = null;

    private static CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver(
            new ReflectionTypeSolver(),
            new JavaParserTypeSolver(root)
    );

    static{
        init();
    }

    private static void init(){
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

    public static String generateAccessors(CompilationUnit cu,boolean setMethod,boolean getMethod) throws IOException {
        if(!cu.findAll(ClassOrInterfaceDeclaration.class).isEmpty()){
            // 解析源码
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach( clazz -> {

                clazz.findAll(FieldDeclaration.class).forEach(field -> {
                    // 过滤静态字段和final字段
                    if (field.isStatic()) return;
                    field.getVariables().forEach(variable -> {
                        ClassOrInterfaceDeclaration parentClass = field.findAncestor(ClassOrInterfaceDeclaration.class).get();
                        if(setMethod) {
                            if(!field.isFinal()) {
                                generateSetMethod(variable, parentClass);
                            }
                        }
                        if(getMethod) {
                            generateGetMethod(variable, parentClass);
                        }
                    });
                });
            });
        }else if (!cu.findAll(EnumDeclaration.class).isEmpty()){
            cu.findAll(EnumDeclaration.class).forEach( envm ->{
                envm.findAll(FieldDeclaration.class).forEach(field->{
                    field.getVariables().forEach(variable ->{
                        EnumDeclaration parentEnum = field.findAncestor(EnumDeclaration.class).get();
                        generateGetMethod(variable,parentEnum);
                    });
                });
            });
        }


        // 输出生成后的代码
        return cu.toString();
    }

    private static void generateSetMethod(VariableDeclarator variable, ClassOrInterfaceDeclaration parentClass) {
        String setMethodName = "set" + capitalize(variable.getNameAsString());
        if(parentClass.getMethodsByName(setMethodName).isEmpty()){
            // 生成setter
            MethodDeclaration setter = new MethodDeclaration()
                    .setName(setMethodName)
                    .setType(void.class) // 返回自身类型
                    .setPublic(true)
                    .addParameter(variable.getType(), variable.getNameAsString())
                    .setBody(new BlockStmt().addStatement(
                            "this." + variable.getNameAsString() + " = " + variable.getNameAsString() + ";"
                    ));
            parentClass.addMember(setter);
        }
    }

    private static void generateGetMethod(VariableDeclarator variable, ClassOrInterfaceDeclaration parentClass) {
        String getMethodName = (variable.getType().toString().equals("boolean")?"is":"get") + capitalize(variable.getNameAsString());
        if(parentClass.getMethodsByName(getMethodName).isEmpty()){
            // 生成getterparentClassparentClass
            MethodDeclaration getter = new MethodDeclaration()
                    .setName(getMethodName)
                    .setType(variable.getType())
                    .setPublic(true)
                    .setBody(new BlockStmt().addStatement(
                            "return this." + variable.getNameAsString() + ";"
                    ));
            parentClass.addMember(getter);
        }
    }

    private static void generateGetMethod(VariableDeclarator variable, EnumDeclaration parentEnum) {
        String getMethodName = (variable.getType().toString().equals("boolean")?"is":"get") + capitalize(variable.getNameAsString());
        if(parentEnum.getMethodsByName(getMethodName).isEmpty()){
            // 生成getterparentClassparentClass
            MethodDeclaration getter = new MethodDeclaration()
                    .setName(getMethodName)
                    .setType(variable.getType())
                    .setPublic(true)
                    .setBody(new BlockStmt().addStatement(
                            "return this." + variable.getNameAsString() + ";"
                    ));
            parentEnum.addMember(getter);
        }
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void main(String[] args) throws Exception {
        List<File> files = traverseRoot(root);
        for(File file : files){
            cu = StaticJavaParser.parse(file);
            boolean dataAnno = cu.getImports().stream().anyMatch(i->i.getName().toString().contains("lombok.Data"));
            boolean getterAnno = dataAnno || cu.getImports().stream().anyMatch(i->i.getName().toString().contains("lombok.Getter"));
            boolean setterAnno = dataAnno || cu.getImports().stream().anyMatch(i->i.getName().toString().contains("lombok.Setter"));

            if(dataAnno || getterAnno || setterAnno){
                String newFileContent = generateAccessors(cu,setterAnno,getterAnno);
                String javaFilePath = file.getPath();
                FileUtil.deleteFile(javaFilePath);
                FileUtil.writeFile(javaFilePath, Arrays.asList(newFileContent),true);
                System.out.println(javaFilePath+" write over~~");
            }
        }
    }


}

