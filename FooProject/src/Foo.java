import intrange.qual.*;
import org.checkerframework.common.value.qual.*;

public class Foo {

    public void testSubtypeRules(//@FullIntRange int fr,
            //@IntRange(from=0, to=255) int ir, 
            //@UnknownVal @IntVal({12, 65, 21, 546, 65, 31, 5, 31, 31, 321, 35, 54, 13}) int eir,
            @IntRange(from=255, to=0) int eir
            //,@FullIntRange int mir, int dir
            ) {
        // Assign to top always good
        /*
        @FullIntRange int a = fr;
    	@FullIntRange int b = ir;
        @FullIntRange int c = mir;

        // test 3 non-overlapping scenarios
        @IntRange(from=0, to=65535) int d = fr; //error
        @IntRange(from=0, to=65535) int e = ir;
        @IntRange(from=0, to=65535) int f = mir; //error

        // test IntRange qualifier default parameters
        @IntRange int g = fr; //error
        @IntRange int h = ir;
        @IntRange int i = mir;
        */
        // test error intrange qualifier
        //@IntRange(from=0, to=2000) int j = eir; //?? need to be enforced
        /*
        // test overlapping scenarios
        @IntRange(from=-255, to=128) int k = ir; //error
        @IntRange(from=128, to=1000) int l = ir; //error
        @IntRange(from=64, to=128) int m = ir; //error

        // test edge cases
        @IntRange(from=0, to=255) int n = ir; 
        @IntRange(from=0, to=300) int o = ir;
        @IntRange(from=-1, to=255) int p = ir;
        @IntRange(from=0, to=200) int q = ir; //error
        @IntRange(from=1, to=255) int r = ir; //error

        // test default qualifier
        int s = fr;
        int t = ir;
        int u = mir;
        @IntRange(from=0, to=255) int v = dir; //error

        // test method parameter passing check
        testMethodParameter(ir);
        testMethodParameter(mir); //error

        // test method return
        @IntRange(from=128, to=1000) int w = testMethodReturn(ir); //error
        */
    }
/*
    private void testMethodParameter(@IntRange(from=0, to=255) int num) {
    
    }

    private @IntRange(from=0, to=255) int testMethodReturn(@IntRange(from=0, to=255) int num) {
        return num;
    }
    */

}
