package org.javaparser.examples.chapter5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

class Bar extends HashMap<Double, Float> {

    private String a, c;

    private Integer i;

    private BigDecimal d;

    private A a1;

    void aMethod() {
        bMethod();
        while (true) {
            int a = 0;
            a = a + 1;
        }
    }

    void bMethod() {
        A a2 = new A();
        a2.foo("PP");
    }

    Integer cMethod() {
        return 1;
    }
}

/*
class B2 extends HashMap<Integer, String> {

    int testInt() {
        Bar bar = new Bar();
        return bar.cMethod().compareTo(0);
    }

}*/
