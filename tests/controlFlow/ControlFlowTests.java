import intrange.qual.*;

public class ControlFlowTests {

    void testMethod(@IntRange(from=0, to=255) int rangeInt, 
            int fullInt, 
            int fullInt2,
            @IntRange(from=0, to=500) int firstPart,
            @IntRange(from=501, to=1000) int secondPart) {
        
        @IntRange(from=0, to=255) int a = rangeInt; //OK
        if (rangeInt > 128) {
            @IntRange(from=128, to=255) int b = rangeInt; //OK
        } else {
            @IntRange(from=0, to=128) int b = rangeInt; //OK
        }
        @IntRange(from=128, to=255) int c = rangeInt; //error
        
        if (fullInt > 255 || fullInt < 0) {
            return;
        }
        @IntRange(from=0, to=255) int d = fullInt; //OK
        
        if (fullInt2 > 500) {
            fullInt2 = secondPart;
            @IntRange(from=500, to=1000) int tmp = fullInt2; //OK
        } else {
            fullInt2 = firstPart;
            @IntRange(from=0, to=500) int tmp = fullInt2; //OK
        }
        @IntRange(from=0, to=1000) int e = fullInt2; //OK
        
    }
    
}