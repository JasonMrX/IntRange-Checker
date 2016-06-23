import intrange.qual.*;

public class WarnFromGreaterThanTo {

	void testMethod(@IntRange(from=0, to=255) int a, //OK
		    //:: warning: (from(200).greater.than.to(100))
			@IntRange(from=200, to=100) int b, //warning
			@IntRange(from=150, to=150) int c, //OK
			//:: warning: (from('z').greater.than.to('a'))
			@IntRange(from='z', to='a') char d, //warning
			//:: warning: (from(20000000000).greater.than.to(0))
			@IntRange(from=20000000000L, to=0) long e //warning
			) {
		/**
		 * bug leave for later
		 * //:: error: (assignment.type.incompatible)
		 * a = b; //error: b is FullIntRange
		 * 
		 * b = c; //OK: b is FullIntRange
		*/
	}
	
}