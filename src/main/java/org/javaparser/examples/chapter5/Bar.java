package org.javaparser.examples.chapter5;

class Bar {

    private String a;

    void aMethod() {
        bMethod();
        while (true) {
            int a = 0;
            a = a + 1;
        }
    }

    void bMethod(){
        System.out.printf("");
    }
}