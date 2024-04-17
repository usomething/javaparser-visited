package org.javaparser.examples.chapter5;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

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

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(ci -> {
            ci.findAll(MethodDeclaration.class).forEach(md -> {
                String param = String.join(",",md.getParameters().stream().map(p->p.getType().asString()).collect(Collectors.toList()));
                System.out.println(md.getNameAsString()+"("+param+")");

                List<MethodCallExpr> mces = md.findAll(MethodCallExpr.class).stream().collect(Collectors.toList());/*.forEach(mce -> {
                    System.out.println("\t"+mce.getNameAsString()+"("+mce.getArguments().stream().map(a->a.).collect(Collectors.toList())+")");
                });*/
                for(MethodCallExpr mce : mces){
                    System.out.print(" -> "+mce.getNameAsString()+" : ");

                    List<Expression> ags = mce.getArguments();
                    for(Expression ag : ags){
                        System.out.print(ag.toString()+",");
                    }
                    System.out.println();
                }


            });
        });


        /*cu.findAll(ClassOrInterfaceDeclaration.class).forEach(ci -> {
            ci.getExtendedTypes().forEach(t -> {
                NodeList<Type> typesList = t.getTypeArguments().orElse(null);
                if (typesList != null) {
                    typesList.stream().forEach(t1 -> {
                        System.out.println(t1.getElementType().toString());
                    });
                }
            });
        });*/



        /*List<ClassOrInterfaceDeclaration> cids = cu.findAll(ClassOrInterfaceDeclaration.class).stream().collect(Collectors.toList());
        for (ClassOrInterfaceDeclaration cid : cids) {
            System.out.println(cid.getFullyQualifiedName().get() + " : ");

            List<ClassOrInterfaceType> types = cid.getExtendedTypes().stream().collect(Collectors.toList());
            for (ClassOrInterfaceType cit : types) {
                NodeList<Type> typesList = cit.getTypeArguments().get();
                typesList.stream().forEach(t -> {
                    System.out.println("\t" + t.getElementType());
                });
            }
            System.out.println("--------------------------------------");
        }*/

       /* cu.findAll(FieldDeclaration.class).forEach(fd -> {
            System.out.println(fd.getVariables().get(0).getTypeAsString() + " -> " + fd.getVariables().get(0).getNameAsString());
        });

        System.out.println("-----------------------------");

        //获取一个文件中的类名
        cu.getChildNodes().stream().filter(n -> n.getClass().equals(ClassOrInterfaceDeclaration.class)).forEach(n -> {
            ClassOrInterfaceDeclaration ci = (ClassOrInterfaceDeclaration) n;
            System.out.println(ci.getFullyQualifiedName().get());

            ci.findAll(MethodDeclaration.class).forEach(md -> {
                System.out.println(md.getNameAsString());

                md.findAll(MethodCallExpr.class).forEach(me -> {
                    boolean scopeExist = me.getScope().isPresent();
                    System.out.println(" -> " + (scopeExist ? me.getScope().get().toString() + "." : "this.") + me.getNameAsString());
                });

            });

        });*/

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
