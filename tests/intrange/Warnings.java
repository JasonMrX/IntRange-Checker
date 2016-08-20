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
	
    public void divisionByZero(
            @IntRange(from=-2, to=-1) int a,
            @IntRange(from=1, to=2) int b) {
        int c;
        c = 3 / a;
        c = 3 / b;
        //:: warning: (possible.division.by.zero)
        c = 3 / (a + b);
    }
    
    public void shiftOutOfRange() {
        int a;
        int bit = 5;

        //:: warning: (shift.out.of.range)
        a = 3 << -1;
        //:: warning: (shift.out.of.range)
        a = 3 << 32;
        a = 3 << bit;

        //:: warning: (shift.out.of.range)
        a = 3 >> -1;
        //:: warning: (shift.out.of.range)
        a = 3 >> 32;
        a = 3 >> bit;

        //:: warning: (shift.out.of.range)
        a = 3 >>> -1;
        //:: warning: (shift.out.of.range)
        a = 3 >>> 32;
        a = 3 >>> bit;
    }
    
    public void negativeArrayDim(int dim) {
        int[] arr = new int[3];
        //:: warning: (possible.negative.array.dimension)
        int[] arr1 = new int[-1];
        //:: warning: (possible.negative.array.dimension)
        int[] arr2 = new int[dim];
        //:: warning: (possible.negative.array.dimension)
        int[][] arr3 = new int[3][dim];
    }
    
}