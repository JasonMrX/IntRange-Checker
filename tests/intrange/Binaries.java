import intrange.qual.*;

public class Binaries {
    
    public void add(
            @IntRange(from=-5, to=5) int a,
            @IntRange(from=10, to=20) int b) {
        //:: error: (assignment.type.incompatible)
        @IntRange(from=6, to=25) int plus1 = a + b; // error
        @IntRange(from=0, to=25) int plus2 = a + b; //OK
        
        double c = 3.0;
        double d = c + a;
    }
    
    public void subtract(
            @IntRange(from=-5, to=5) int a,
            @IntRange(from=10, to=20) int b) {
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-25, to=-6) int minus1 = a - b; //error
        @IntRange(from=-25, to=-5) int minus2 = a - b; //OK
        
        double c = 4.0;
        double d = c - a;
    }
    
    public void multiply(
            @IntRange(from=-5, to=5) int a,
            @IntRange(from=10, to=20) int b,
            @IntRange(from=-20, to=-10) int c) {
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-100, to=99) int mult1 = a * b; //error
        @IntRange(from=-100, to=100) int mult2 = a * b; //OK
        
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-100, to=99) int mult3 = a * c; //error
        @IntRange(from=-100, to=100) int mult4 = a * c; //OK
        
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-400, to=-101) int mult5 = b * c; //error
        @IntRange(from=-400, to=-100) int mult6 = b * c; //OK
        
        double d = 10.0;
        double e = d - a;
    }
    
    public void divide(
            @IntRange(from=5, to=10) int gtz,
            @IntRange(from=0, to=5) int gez,
            @IntRange(from=-10, to=-5) int ltz,
            @IntRange(from=-5, to=0) int lez,
            @IntRange(from=-5, to=5) int ze) {
        
        @IntRange(from=1, to=10) int s11 = gtz / gez;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=1, to=9) int s12 = gtz / gez;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=2, to=10) int s13 = gtz / gez;
        
        
        @IntRange(from=-10, to=-1) int s21 = gtz / lez;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-9, to=-1) int s22 = gtz / lez;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-10, to=-2) int s23 = gtz / lez;
        
        
        @IntRange(from=-10, to=10) int s31 = gtz / ze;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-9, to=10) int s32 = gtz / ze;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-10, to=9) int s33 = gtz / ze;
        
        
        @IntRange(from=-10, to=-1) int s41 = ltz / gez;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-9, to=-1) int s42 = ltz / gez;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-10, to=-2) int s43 = ltz / gez;
        
        
        @IntRange(from=1, to=10) int s51 = ltz / lez;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=1, to=9) int s52 = ltz / lez;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=2, to=10) int s53 = ltz / lez;
        
        
        @IntRange(from=-10, to=10) int s61 = ltz / ze;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-9, to=10) int s62 = ltz / ze;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-10, to=9) int s63 = ltz / ze;
        
        
        @IntRange(from=-1, to=1) int s71 = ze / gtz;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=1) int s72 = ze / gtz;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-1, to=0) int s73 = ze / gtz;  
        
        
        @IntRange(from=-1, to=1) int s81 = ze / ltz;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=1) int s82 = ze / ltz;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-1, to=0) int s83 = ze / ltz; 
        
        
        @IntRange(from=-5, to=5) int s91 = ze / ze;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-4, to=5) int s92 = ze / ze;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-5, to=4) int s93 = ze / ze; 
    }
    
    public void remainder(
            @IntRange(from=5, to=10) int a,
            @IntRange(from=1, to=7) int b) {
        
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-1, to=2) int c = a % b;
        
        @IntRange(from=0, to=10) int d = a % b;
        
    }
    
}