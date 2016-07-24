import android.support.annotation.*;
class AliasedAnnotations {
    
    void useIntRangeAnnotation() {
        //:: error: (assignment.type.incompatible)
        @android.support.annotation.IntRange(from=0, to=10) int i = 12;

        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=10) int j = 13;
        
        @IntRange(from=0) int k = 10;
        
    }
}