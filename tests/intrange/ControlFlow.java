import intrange.qual.*;

class ControlFlow {
    
    public void leastUpperBound(
            boolean b,
            boolean c,
            @IntRange(from=0, to=10) int i,
            @IntRange(from=20, to=30) int j,
            @IntRange(from=50, to=100) int k) {
        int a = i;
        if (b) {
            a = j;
        } else if (c) {
            a = k;
        }
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=30) int m = a;
        
        @IntRange(from=0, to=100) int n = a;
        
        if (true) {
            a = i;
        }
        // is this a control flow bug?
        //@IntRange(from=0, to=10) int l = a; //control flow
    }
    
    public void equalTo(
            @IntRange(from=0, to=100) int a,
            @IntRange(from=10, to=20) int b,
            @IntRange(from=-50, to=50) int e) {
        @IntRange(from=10, to=20) int c;
        @IntRange(from=11, to=20) int d;
        @IntRange(from=1, to=100) int f;
        if (a == b) {
            c = a;
            //:: error: (assignment.type.incompatible)
            d = a;
        } 
        
        if (a != e) {
            //:: error: (assignment.type.incompatible)
            f = a; // a has type of @IntRange(from=0, to=100)
        }
    }
    
    public void lessThan(@IntRange(from=0, to=100) int a) {
        @IntRange(from=0, to=30) int b;
        @IntRange(from=0, to=29) int c;
        @IntRange(from=31, to=100) int d;
        @IntRange(from=32, to=100) int e;
        
        if (a < 31) {
            b = a; // OK
            //:: error: (assignment.type.incompatible)
            c = a; // a has type @IntRange(0, 30);
        } else {
            d = a; // OK
            //:: error: (assignment.type.incompatible)
            e = a; // a has type @IntRange(31, 100);
        }
    }
    
    public void greaterThan(@IntRange(from=0, to=100) int a) {
        @IntRange(from=0, to=30) int b;
        @IntRange(from=0, to=29) int c;
        @IntRange(from=31, to=100) int d;
        @IntRange(from=32, to=100) int e;
        
        if (a > 30) {
            d = a; // OK
            //:: error: (assignment.type.incompatible)
            e = a; // a has type @IntRange(31, 100);
        } else {
            b = a; // OK
            //:: error: (assignment.type.incompatible)
            c = a; // a has type @IntRange(0, 30);
        }
    }
    
    public void emptyRange(@IntRange(from=0, to=100) int a) {
        @IntRange(from=0, to=10) int b;
        
        if (a > 120) {
            //:: error: (assignment.type.incompatible)
            b = a;// impossible
        }
    }
    
}