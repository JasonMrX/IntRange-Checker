import intrange.qual.*;

public class Warnings {
    
    public void intTest() {
        //:: warning: (from.greater.than.to)
        @IntRange(from=200, to=100) int b; //warning
        @IntRange(from=0, to=255) int a; //OK
        @IntRange(from=150, to=150) int c; //OK
    }
    
    public void charTest() {
        //:: warning: (from.greater.than.to)
        @IntRange(from='z', to='a') char a; //warning
        @IntRange(from='a', to='c') char b; //OK
    }
    
    public void longTest() {
        //:: warning: (from.greater.than.to)
        @IntRange(from=20000000000L, to=0) long e; //warning
        @IntRange(from=-20000000000L, to=2000000000L) long b; //OK
    }
	
}