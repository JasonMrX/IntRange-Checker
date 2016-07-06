package intrange;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NumericalAdditionNode;
import org.checkerframework.dataflow.cfg.node.NumericalMultiplicationNode;
import org.checkerframework.dataflow.cfg.node.NumericalSubtractionNode;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.javacutil.AnnotationUtils;

import intrange.IntRangeAnnotatedTypeFactory;
import intrange.qual.IntRange;

public class IntRangeTransfer extends CFTransfer {
    
    AnnotatedTypeFactory atypefactory;
    
    public IntRangeTransfer(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(analysis);
        atypefactory = analysis.getTypeFactory();
        
    }

    private class Range {
        
        public final long from;
        public final long to;
        
        public Range(long from, long to) {
            this.from = from;
            this.to = to;
        }
        
    }
    
    private AnnotationMirror createIntRangeAnnotation(Range range) {
        if (range.from > range.to) {
            return ((IntRangeAnnotatedTypeFactory) atypefactory).FULLINTRANGE;
        }
        return ((IntRangeAnnotatedTypeFactory) atypefactory).
                createIntRangeAnnotation(range.from, range.to);
    }
    
    private TransferResult<CFValue, CFStore> createNewResult(
            TransferResult<CFValue, CFStore> result, 
            Range resultRange) {
        AnnotationMirror stringVal = createIntRangeAnnotation(resultRange);
        CFValue newResultValue = analysis.createSingleAnnotationValue(
                stringVal, result.getResultValue().getType().getUnderlyingType());
        return new RegularTransferResult<>(newResultValue,
                result.getRegularStore());
    }
    
    private Range getIntRange(Node subNode, 
            TransferInput<CFValue, CFStore> p) {
        CFValue value = p.getValueOfSubNode(subNode);
        AnnotationMirror rangeAnno = value.getType().getAnnotation(IntRange.class);
        return new Range(
                AnnotationUtils.getElementValue(rangeAnno, "from", Long.class, true),
                AnnotationUtils.getElementValue(rangeAnno, "to", Long.class, true));
    }
    
    enum NumericalBinaryOps {
        ADDITION, 
        SUBTRACTION, 
        DIVISION, 
        REMAINDER, 
        MULTIPLICATION;
    }

    private Range calculateAdditionRange(Range lefts, Range rights) {
        long resultFrom = lefts.from + rights.from;
        long resultTo = lefts.to + rights.to;
        return new Range(resultFrom, resultTo);
    }
    
    private Range calculateSubtractionRange(Range lefts, Range rights) {
        long resultFrom = lefts.from - rights.to;
        long resultTo = lefts.to - rights.from;
        return new Range(resultFrom, resultTo);
    }
    
    private Range calculateMultiplicationRange(Range lefts, Range rights) {
        long[] possibleValues = new long[4];
        possibleValues[0] = lefts.from * rights.from;
        possibleValues[1] = lefts.from * rights.to;
        possibleValues[2] = lefts.to * rights.from;
        possibleValues[3] = lefts.to * rights.to;
        long resultFrom = Long.MAX_VALUE;
        long resultTo = Long.MIN_VALUE;
        for (long pv : possibleValues) {
            resultFrom = Math.min(resultFrom, pv);
            resultTo = Math.max(resultTo, pv);
        }
        return new Range(resultFrom, resultTo);
    }
    
    private Range calculateNumericalBinaryOp(
            Node leftNode, Node rightNode,
            NumericalBinaryOps op,
            TransferInput<CFValue, CFStore> p) {
        Range lefts = getIntRange(leftNode, p);
        Range rights = getIntRange(rightNode, p);
        switch (op) {
        case ADDITION:
            return calculateAdditionRange(lefts, rights);
        case SUBTRACTION:
            return calculateSubtractionRange(lefts, rights);
        case MULTIPLICATION:
            return calculateMultiplicationRange(lefts, rights);
        default:
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitNumericalAddition(
            NumericalAdditionNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitNumericalAddition(n, p);
        Range resultRange = calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(),
                NumericalBinaryOps.ADDITION, p);
        return createNewResult(transferResult, resultRange);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitNumericalSubtraction( 
            NumericalSubtractionNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitNumericalSubtraction(n, p);
        Range resultRange = calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(),
                NumericalBinaryOps.SUBTRACTION, p);
        return createNewResult(transferResult, resultRange);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitNumericalMultiplication(
            NumericalMultiplicationNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitNumericalMultiplication(n, p);
        Range resultRange = calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(),
                NumericalBinaryOps.MULTIPLICATION, p);
        return createNewResult(transferResult, resultRange);
    }
}
















































