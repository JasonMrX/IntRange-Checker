package intrange.util;

public class Range {
    
    public long from;
    public long to;
    
    private Range getRangeFromPossibleValues(long[] possibleValues) {
        long resultFrom = Long.MAX_VALUE;
        long resultTo = Long.MIN_VALUE;
        for (long pv : possibleValues) {
            resultFrom = Math.min(resultFrom, pv);
            resultTo = Math.max(resultTo, pv);
        }
        return new Range(resultFrom, resultTo);
    }
    
    private Range merge(Range right) {
        return new Range(Math.min(from, right.from), Math.max(to, right.to));
    }
    
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
        return getRangeFromPossibleValues(possibleValues);
    }
    
    public Range divide(Range right) {
        long resultFrom = Long.MIN_VALUE;
        long resultTo = Long.MAX_VALUE;

        // be careful of divided by zero!
        if (from > 0 && right.from >= 0) {
            resultFrom = from / Math.max(right.to, 1);
            resultTo = to / Math.max(right.from, 1);
        } else if (from > 0 && right.to <= 0) {
            resultFrom = to / Math.min(right.to, -1);
            resultTo = from / Math.min(right.from, -1);
        } else if (from > 0) {
            resultFrom = -to;
            resultTo = to;            
        } else if (to < 0 && right.from >=0) {
            resultFrom = from / Math.max(right.from, 1);
            resultTo = to / Math.max(right.to, 1);
        } else if (to < 0 && right.to <= 0) {
            resultFrom = to / Math.min(right.from, -1);
            resultTo = from / Math.min(right.to, -1);
        } else if (to < 0) {
            resultFrom = from;
            resultTo = -from;
        } else if (right.from >= 0) {
            resultFrom = from / Math.max(right.from, 1);
            resultTo = to / Math.max(right.from, 1);
        } else if (right.to <= 0) {
            resultFrom = to / Math.min(right.to, -1);
            resultTo = from / Math.min(right.to, -1);
        } else {
            resultFrom = Math.min(from, -to);
            resultTo = Math.max(-from, to);
        }
        return new Range(resultFrom, resultTo);
    }
    
    public Range remainder(Range right) {
        /*
         * calculate the bounds case by case:
         * + / + : [0, min(l, r - 1)]
         * + / - : [0, min(l, -r - 1)]
         * - / + : [max(l, -r + 1), 0]
         * - / - : [max(l, r + 1), 0]
         * 
         * too many different conditions
         * return a looser range
         */
        long[] possibleValues = new long[9];
        possibleValues[0] = 0;
        possibleValues[1] = Math.min(from, Math.abs(right.from) - 1);
        possibleValues[2] = Math.min(from, Math.abs(right.to) - 1);
        possibleValues[3] = Math.min(to, Math.abs(right.from) - 1);
        possibleValues[4] = Math.min(to, Math.abs(right.to) - 1);
        possibleValues[5] = Math.max(from, -Math.abs(right.from) + 1);
        possibleValues[6] = Math.max(from, -Math.abs(right.to) + 1);
        possibleValues[7] = Math.max(to, -Math.abs(right.from) + 1);
        possibleValues[8] = Math.max(to, -Math.abs(right.to) + 1);
        return getRangeFromPossibleValues(possibleValues);
    }
    
    
}