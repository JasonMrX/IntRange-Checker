import intrange.qual.IntRange;

class Desugarings {
    
    public void postfix() {
        int i = 3;
        @IntRange(from=3, to=3) int a = i++;
        @IntRange(from=4, to=4) int b = i;
        @IntRange(from=4, to=4) int c = i--;
        
    }
    
    public void prefix() {
        int i = 3;
        @IntRange(from=4, to=4) int a = ++i;
        @IntRange(from=4, to=4) int b = i;
        @IntRange(from=3, to=3) int c = --i;
    }
    
    public void compoundAssignments() {
        int i = 3;
        @IntRange(from=6, to=6) int a = (i += 3);
        @IntRange(from=3, to=3) int b = (i -= 3);
        @IntRange(from=9, to=9) int c = (i *= 3);
        @IntRange(from=3, to=3) int d = (i /= 3);
    }

}