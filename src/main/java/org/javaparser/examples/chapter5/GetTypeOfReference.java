package org.javaparser.examples.chapter5;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class GetTypeOfReference {

    private static final String FILE_PATH =
            "src/main/java/org/javaparser/examples/chapter5/Bar.java";

    public static void main(String[] args) throws FileNotFoundException {
        TypeSolver typeSolver = new CombinedTypeSolver();

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser
                .getParserConfiguration()
                .setSymbolResolver(symbolSolver);

        CompilationUnit cu = StaticJavaParser.parse(new File(FILE_PATH));

        //获取一个文件中的类名
        cu.getChildNodes().stream().filter(n->n.getClass().equals(ClassOrInterfaceDeclaration.class)).forEach(n->{
            ClassOrInterfaceDeclaration ci = (ClassOrInterfaceDeclaration) n;
            System.out.println(ci.getFullyQualifiedName().get());

            ci.findAll(MethodDeclaration.class).forEach(md->{
                System.out.println(md.getNameAsString());

                md.findAll(MethodCallExpr.class).forEach(me->{
                    boolean scopeExist = me.getScope().isPresent();
                    System.out.println(" -> "+(scopeExist?me.getScope().get()+".":"this.")+me.getNameAsString());
                });

            });

        });

        /*cu.findAll(ClassOrInterfaceDeclaration.class).forEach(ci->{
            String clazzName = ci.getNameAsString();
            ci.findAll(MethodDeclaration.class).forEach(md->{
                String name = md.getNameAsString();
                List<MethodCallExpr> innerMethods = md.findAll(MethodCallExpr.class);
                if(innerMethods!=null && !innerMethods.isEmpty()){
                    innerMethods.forEach(me->{
                        System.out.println(clazzName+": "+name + " -> " + me.getNameAsString());
                    });
                }
            });
        });*/


        /*cu.findAll(MethodDeclaration.class).forEach(md ->{
            String name = md.getNameAsString();
            List<MethodCallExpr> innerMethods = md.findAll(MethodCallExpr.class);
            if(innerMethods!=null && !innerMethods.isEmpty()){
                innerMethods.forEach(me->{
                    System.out.println(name + " -> " + me.getNameAsString());
                });
            }
        });*/

        /*cu.findAll(AssignExpr.class).forEach(ae -> {
            ResolvedType resolvedType = ae.calculateResolvedType();
            System.out.println(ae + " is a: " + resolvedType);
        });*/
    }
}
