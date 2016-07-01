package intrange;

import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeFactory;

public class IntRangeTransfer extends CFTransfer {
    
    AnnotatedTypeFactory atypefactory;
    
    public IntRangeTransfer(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(analysis);
        atypefactory = analysis.getTypeFactory();
        
    }

}
