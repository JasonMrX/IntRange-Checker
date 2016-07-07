package intrange;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.IntegerDivisionNode;
import org.checkerframework.dataflow.cfg.node.IntegerRemainderNode;
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
import intrange.util.Range;

public class IntRangeTransfer extends CFTransfer {
    
    AnnotatedTypeFactory atypefactory;
    
    private Set<TypeKind> coveredKinds;
    
    public IntRangeTransfer(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(analysis);
        atypefactory = analysis.getTypeFactory();
        
        coveredKinds = new HashSet<TypeKind>(3);
        coveredKinds.add(TypeKind.INT);
        coveredKinds.add(TypeKind.LONG);
        coveredKinds.add(TypeKind.CHAR);
    }

    private boolean isCoveredKind(Node n) {
        return coveredKinds.contains(n.getType().getKind());
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
    
    private Range calculateNumericalBinaryOp(
            Node leftNode, Node rightNode,
            NumericalBinaryOps op,
            TransferInput<CFValue, CFStore> p) {
        Range leftRange = getIntRange(leftNode, p);
        Range rightRange = getIntRange(rightNode, p);
        switch (op) {
        case ADDITION:
            return leftRange.plus(rightRange);
        case SUBTRACTION:
            return leftRange.minus(rightRange);
        case MULTIPLICATION:
            return leftRange.times(rightRange);
        case DIVISION:
            return leftRange.divide(rightRange);
        case REMAINDER:
            return leftRange.remainder(rightRange);
        default:
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitNumericalAddition(
            NumericalAdditionNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitNumericalAddition(n, p);
        if (!isCoveredKind(n.getLeftOperand()) 
                || !isCoveredKind(n.getRightOperand())) {
            return transferResult;
        }
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
        if (!isCoveredKind(n.getLeftOperand()) 
                || !isCoveredKind(n.getRightOperand())) {
            return transferResult;
        }
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
        if (!isCoveredKind(n.getLeftOperand()) 
                || !isCoveredKind(n.getRightOperand())) {
            return transferResult;
        }
        Range resultRange = calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(),
                NumericalBinaryOps.MULTIPLICATION, p);
        return createNewResult(transferResult, resultRange);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitIntegerDivision(
            IntegerDivisionNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitIntegerDivision(n, p);
        if (!isCoveredKind(n.getLeftOperand()) 
                || !isCoveredKind(n.getRightOperand())) {
            return transferResult;
        }
        Range resultRange = calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(),
                NumericalBinaryOps.DIVISION, p);
        return createNewResult(transferResult, resultRange);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitIntegerRemainder(
            IntegerRemainderNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitIntegerRemainder(n, p);
        if (!isCoveredKind(n.getLeftOperand()) 
                || !isCoveredKind(n.getRightOperand())) {
            return transferResult;
        }
        Range resultRange = calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(),
                NumericalBinaryOps.REMAINDER, p);
        return createNewResult(transferResult, resultRange);
    }
            
}
















































