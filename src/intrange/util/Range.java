package intrange.util;

public class Range {
    
    public long from;
    public long to;
    
    public Range(long from, long to) {
        this.from = from;
        this.to = to;
    }
    
    public Range plus(Range right) {
        long resultFrom = from + right.from;
        long resultTo = to + right.to;
        return new Range(resultFrom, resultTo);
    }
    
    public Range minus(Range right) {
        long resultFrom = from - right.to;
        long resultTo = to - right.from;
        return new Range(resultFrom, resultTo);
    }
    
    public Range times(Range right) {
        long[] possibleValues = new long[4];
        possibleValues[0] = from * right.from;
        possibleValues[1] = from * right.to;
        possibleValues[2] = to * right.from;
        possibleValues[3] = to * right.to;
        long resultFrom = Long.MAX_VALUE;
        long resultTo = Long.MIN_VALUE;
        for (long pv : possibleValues) {
            resultFrom = Math.min(resultFrom, pv);
            resultTo = Math.max(resultTo, pv);
        }
        return new Range(resultFrom, resultTo);
    }
    
}
