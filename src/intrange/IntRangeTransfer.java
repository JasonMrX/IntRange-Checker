package intrange;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.BitwiseComplementNode;
import org.checkerframework.dataflow.cfg.node.IntegerDivisionNode;
import org.checkerframework.dataflow.cfg.node.IntegerRemainderNode;
import org.checkerframework.dataflow.cfg.node.LeftShiftNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NumericalAdditionNode;
import org.checkerframework.dataflow.cfg.node.NumericalMinusNode;
import org.checkerframework.dataflow.cfg.node.NumericalMultiplicationNode;
import org.checkerframework.dataflow.cfg.node.NumericalPlusNode;
import org.checkerframework.dataflow.cfg.node.NumericalSubtractionNode;
import org.checkerframework.dataflow.cfg.node.SignedRightShiftNode;
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
        if (resultRange == null) {
            return result;
        }
        AnnotationMirror stringVal = createIntRangeAnnotation(resultRange);
        CFValue newResultValue = analysis.createSingleAnnotationValue(
                stringVal, result.getResultValue().getType().getUnderlyingType());
        return new RegularTransferResult<>(newResultValue,
                result.getRegularStore());
    }
    
    private Range getIntRange(Node subNode, 
            TransferInput<CFValue, CFStore> p) {
        // TODO: need to handle FULLINTRANGE 
        CFValue value = p.getValueOfSubNode(subNode);
        AnnotationMirror rangeAnno = value.getType().getAnnotation(IntRange.class);
        long valueFrom;
        long valueTo;
        if (rangeAnno == null) {
            valueFrom = Long.MIN_VALUE;
            valueTo = Long.MAX_VALUE;
        } else {
            valueFrom = AnnotationUtils.getElementValue(rangeAnno, "from", Long.class, true);
            valueTo = AnnotationUtils.getElementValue(rangeAnno, "to", Long.class, true);
        }
        return new Range(valueFrom, valueTo);
    }
    
    /*
     * Binary operation refinement that are supported by intrange checker.
     * Ignore UNSIGNED_SHIFT_RIGHT, BITWISE_AND, BITWISE_OR, BITWISE_XOR,
     * because their resulted ranges are too complex to be determined.
     * Treated as FULLINTRANGE.
     */
    
    enum NumericalBinaryOps {
        ADDITION, 
        SUBTRACTION, 
        DIVISION, 
        REMAINDER, 
        MULTIPLICATION,
        SHIFT_LEFT,
        SIGNED_SHIFT_RIGHT;
    }
    
    private Range calculateNumericalBinaryOp(
            Node leftNode, Node rightNode,
            NumericalBinaryOps op,
            TransferInput<CFValue, CFStore> p) {
        if (!isCoveredKind(leftNode) 
                || !isCoveredKind(rightNode)) {
            return null;
        }
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
        case SHIFT_LEFT:
            return leftRange.shiftLeft(rightRange);
        case SIGNED_SHIFT_RIGHT:
            return leftRange.signedShiftRight(rightRange);
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
    
    @Override
    public TransferResult<CFValue, CFStore> visitIntegerDivision(
            IntegerDivisionNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitIntegerDivision(n, p);
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
        Range resultRange = calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(),
                NumericalBinaryOps.REMAINDER, p);
        return createNewResult(transferResult, resultRange);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitLeftShift(
            LeftShiftNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitLeftShift(n, p);
        Range resultRange = calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(),
                NumericalBinaryOps.SHIFT_LEFT, p);
        return createNewResult(transferResult, resultRange);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitSignedRightShift(
            SignedRightShiftNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitSignedRightShift(n, p);
        Range resultRange = calculateNumericalBinaryOp(
                n.getLeftOperand(), n.getRightOperand(),
                NumericalBinaryOps.SIGNED_SHIFT_RIGHT, p);
        return createNewResult(transferResult, resultRange);
    }
            
    /*
     * Ignore BITWISE_COMPLEMENT
     */
    
    enum NumericalUnaryOps{
        PLUS, MINUS, BITWISE_COMPLEMENT;
    }
    
    private Range calculateNumericalUnaryOp(Node operand,
            NumericalUnaryOps op, TransferInput<CFValue, CFStore> p) {
        if (!isCoveredKind(operand)) {
            return null;
        }
        Range operandRange = getIntRange(operand, p);
        switch (op) {
        case PLUS:
            return operandRange.unaryPlus();
        case MINUS:
            return operandRange.unaryMinus();
        case BITWISE_COMPLEMENT:
            return operandRange.bitwiseComplement();
        default:
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitNumericalPlus(
            NumericalPlusNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitNumericalPlus(n, p);
        Range resultRange = calculateNumericalUnaryOp(n.getOperand(),
                NumericalUnaryOps.PLUS, p);
        return createNewResult(transferResult, resultRange);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitNumericalMinus(
            NumericalMinusNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitNumericalMinus(n, p);
        Range resultRange = calculateNumericalUnaryOp(n.getOperand(),
                NumericalUnaryOps.MINUS, p);
        return createNewResult(transferResult, resultRange);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitBitwiseComplement(
            BitwiseComplementNode n, TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> transferResult = super
                .visitBitwiseComplement(n, p);
        Range resultRange = calculateNumericalUnaryOp(n.getOperand(),
                NumericalUnaryOps.MINUS, p);
        return createNewResult(transferResult, resultRange);
    }
    
}
















































