import intrange.qual.*;

public class Foo {

    public void testSubtypeRules(@FullIntRange int fr, @IntRange(from=0, to=255) int ir, @IntRange int mir, @IntRange(from=255, to=0) int eir) {
        // Assign to top always good
        @FullIntRange int a = fr;
    	@FullIntRange int b = ir;
        @FullIntRange int c = mir;

        // test 3 non-overlapping scenarios
        @IntRange(from=0, to=65535) int d = fr; //error
        @IntRange(from=0, to=65535) int e = ir;
        @IntRange(from=0, to=65535) int f = mir; //error

        // test default qualifier parameters
        @IntRange int g = fr; //error
        @IntRange int h = ir;
        @IntRange int i = mir;

        // test error intrange qualifier
        @IntRange(from=100, to=200) int j = eir; //?? need to be enforced

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
    }

}
