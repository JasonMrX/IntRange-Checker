import intrange.qual.*;

public class NumericalOperationTests {

    void testMethod(
            @IntRange(from=0, to=10) int a,
            @IntRange(from=10, to=20) int b) {
        
        /* + */
        @IntRange(from=11, to=30) int plus1 = a + b; // error
        @IntRange(from=10, to=30) int plus2 = a + b; //OK
        
        /* - */
        @IntRange(from=-20, to=-1) int minus1 = a - b; //error
        @IntRange(from=-20, to=0) int minus2 = a - b; //OK
        
        /* * */
        @IntRange(from=1, to=200) int mult1 = a * b; //error
        @IntRange(from=0, to=200) int mult2 = a * b; //OK
        
        /**
         * /
         * divided by zero?
         */
    }
    
}