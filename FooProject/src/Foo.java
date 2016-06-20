import intrange.qual.*;

public class Foo {

    public void subtyping(@FullIntRange int fr, @IntRange(from=0, to=200) int ir) {
        @FullIntRange int a = ir;
        @FullIntRange int b = fr;
        @IntRange int c = ir;
        @IntRange int d = fr;
    }

}
