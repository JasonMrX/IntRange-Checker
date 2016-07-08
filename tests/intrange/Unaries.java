import intrange.qual.*;

class Unaries {
    
    public void complememnt(@IntRange(from=-2, to=10) int a) {
        @IntRange(from=-11, to=1) int b = ~a;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-10, to=1) int c = ~a;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=-11, to=0) int d = ~a;
    }
    
    public void postfix() {
        int i = 3;
        /* postfix increment/decrement not working -- need to explore why
        @IntRange(from=3, to=3) int a = i++;
        @IntRange(from=4, to=4) int b = i;
        @IntRange(from=4, to=4) int c = i--;
        */
    }
    
    public void prefix() {
        int i = 3;
        @IntRange(from=4, to=4) int a = ++i;
        @IntRange(from=4, to=4) int b = i;
        @IntRange(from=3, to=3) int c = --i;
    }
    
    public void unaryPlusMinus(@IntRange(from=-2, to=10) int a) {
        @IntRange(from=-2, to=10) int b = +a;
        @IntRange(from=-10, to=2) int c = -a;
    }
    
}