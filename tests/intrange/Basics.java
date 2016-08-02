import org.checkerframework.common.value.qual.IntVal;

import intrange.qual.*;

class Basics {

    public void IntegerTest() {
        Integer d = new Integer(1000);
        int a = d;
//        if (true) {
//            a = 2;
//        }
        @IntRange(from=0, to=2) Integer b = a;

        //:: error: (assignment.type.incompatible)
        @IntRange(from=1, to=1) Integer c = d;
    }
    
}