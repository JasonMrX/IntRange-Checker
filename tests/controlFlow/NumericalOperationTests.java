import intrange.qual.*;

public class NumericalOperationTests {

    void testMethod(
            @IntRange(from=-5, to=5) int a,
            @IntRange(from=10, to=20) int b,
            @IntRange(from=-20, to=-10) int c
          ) {
        
        /* + */
        @IntRange(from=6, to=25) int plus1 = a + b; // error
        @IntRange(from=5, to=25) int plus2 = a + b; //OK
        
        /* - */
        @IntRange(from=-25, to=-6) int minus1 = a - b; //error
        @IntRange(from=-25, to=-5) int minus2 = a - b; //OK
        
        /* * */
        @IntRange(from=-100, to=99) int mult1 = a * b; //error
        @IntRange(from=-100, to=100) int mult2 = a * b; //OK
        @IntRange(from=-100, to=99) int mult3 = a * c; //error
        @IntRange(from=-100, to=100) int mult4 = a * c; //OK
        @IntRange(from=-400, to=-101) int mult5 = b * c; //error
        @IntRange(from=-400, to=-100) int mult6 = b * c; //OK
        
        /**
         * /
         * divided by zero?
         */
    }
    
}