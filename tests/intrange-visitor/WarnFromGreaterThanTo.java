import intrange.qual.*;

public class WarnFromGreaterThanTo {

    //:: warning: (from(200).greater.than.to(100))
	void testMethod(@IntRange(from=200, to=100) int eir) {
        // test warning of error intrange qualifier
        @IntRange(from=0, to=255) int j = eir; 
	}
	
}