import intrange.qual.*;

public class WarnFromGreaterThanTo {

	void testMethod(@IntRange(from=0, to=255) int a, //OK
		    //:: warning: (from.greater.than.to)
			@IntRange(from=200, to=100) int b, //warning
			@IntRange(from=150, to=150) int c, //OK
			//:: warning: (from.greater.than.to)
			@IntRange(from='z', to='a') char d, //warning
			//:: warning: (from.greater.than.to)
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