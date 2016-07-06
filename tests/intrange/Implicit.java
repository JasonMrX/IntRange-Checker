import intrange.qual.*;

class Implicit {
	
    public void longTest() {
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=255) long l0 = -1L;
        @IntRange(from=0, to=255) long l1 = 100L;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=255) long l2 = 1000L;
    }
    
    public void intTest() {
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=255) int i0 = -1;
        @IntRange(from=0, to=255) int i1 = 100;
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=255) int i2 = 1000;
    }
    
	void charTest() {
		//:: error: (assignment.type.incompatible)
		@IntRange(from='Z', to='b') int c0 = 'A'; // 'A' = 65;
		@IntRange(from='Z', to='b') int c1 = 'a'; // 'a' = 97;
		//:: error: (assignment.type.incompatible)
		@IntRange(from='Z', to='b') int c2 = 'z'; // 'z' = 122;
	}
}