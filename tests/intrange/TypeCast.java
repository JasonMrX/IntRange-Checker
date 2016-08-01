import intrange.qual.IntRange;

class TypeCast {
    
    public void castTest(
            @IntRange(from=65, to=90) int i,
            @IntRange(from=65L, to=90L) long l,
            @IntRange(from='A', to='Z') char c,
            @IntRange(from=65, to=90) short s) {
        
        l = (long)i;
        l = (long)s;
        l = (long)c;
        i = (int)s;
        i = (int)c;
        s = (short)c;
        
        double d = (double)i;
        int b = (int)d;
        
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=10) int a = (int)3.0; 
        // cast types other than integer to integer would result in @FullIntRange

        //:: error: (assignment.type.incompatible)
        l = (b += 3.0);
    }
    
}