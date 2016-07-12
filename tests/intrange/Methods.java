import intrange.qual.*;

class Methods {
    
    public void simple() {
        //:: error: (assignment.type.incompatible)
        @IntRange(from=0, to=15) int a = getNumber();
        
        char b = getChar();
    }
    
    @IntRange(from=0, to=20)
    public int getNumber() {
        //:: error: (return.type.incompatible)
        return 100;
    }
    
    @IntRange(from='a', to='z')
    public char getChar() {
        return 'c';
    }
    
}