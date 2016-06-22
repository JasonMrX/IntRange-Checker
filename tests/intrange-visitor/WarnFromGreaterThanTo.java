import intrange.qual.*;

public class WarnFromGreaterThanTo {

	void testMethod(@IntRange(from=0, to=255) int eir, //OK
		    //:: warning: (from(200).greater.than.to(100))
			@IntRange(from=200, to=100) int a, //warning
			@IntRange(from=150, to=150) int b //OK
			) {
	}
	
}