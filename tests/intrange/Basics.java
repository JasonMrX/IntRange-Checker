import org.checkerframework.common.value.qual.IntVal;

import intrange.qual.*;

class Basics {

    public void IntegerTest() {
        Integer d = new Integer(0);

        @IntRange(from=0, to=2) Integer b = d;

        //:: error: (assignment.type.incompatible)
        @IntRange(from=1, to=1) Integer c = d;
    }
    
}