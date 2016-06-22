import java.io.File;

import org.checkerframework.framework.test.CheckerFrameworkTest;
import org.junit.runners.Parameterized.Parameters;

public class IntRangeTest extends CheckerFrameworkTest{

    public IntRangeTest(File testFile) {
        super(testFile,
                intrange.IntRangeChecker.class,
                "",
                "-Anomsgtext");
    }   
    
    @Parameters
    public static String[] getTestDirs() {
        return new String[]{"intrange-typeHierarchy"};
    }   
    
}
