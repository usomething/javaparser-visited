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
        A a2 = new A();
        a2.foo("PP");
    }

    Integer cMethod(){
        return 1;
    }
}
 class B2{

    int testInt(){
        Bar bar = new Bar();
        return bar.cMethod().compareTo(0);
    }

 }