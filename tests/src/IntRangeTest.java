import java.io.File;
import java.util.List;

import org.checkerframework.framework.test.CheckerFrameworkPerFileTest;
import org.checkerframework.framework.test.TestUtilities;
import org.junit.runners.Parameterized.Parameters;

public class IntRangeTest extends CheckerFrameworkPerFileTest{

    public IntRangeTest(File testFile) {
        super(testFile,
                intrange.IntRangeChecker.class,
                "",
                "-Anomsgtext");
    }   
    
    @Parameters
       public static List<File> getTestFiles() {
         return TestUtilities.findNestedJavaTestFiles("intrange");
       }  
    
}
