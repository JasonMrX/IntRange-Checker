import intrange.qual.*;

public class Foo {

    public void subtyping(@FullIntRange int fr, @IntRange(from=0, to=255) int ir) {
        @FullIntRange int a = ir;
        @FullIntRange int b = fr;
        @IntRange(from=0, to=222) int c = ir;
        @IntRange int d = fr;
    }

}
