package intrange;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;

import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.FlowExpressions.Receiver;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.BitwiseComplementNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.IntegerDivisionNode;
import org.checkerframework.dataflow.cfg.node.IntegerRemainderNode;
import org.checkerframework.dataflow.cfg.node.LeftShiftNode;
import org.checkerframework.dataflow.cfg.node.LessThanNode;
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

import intrange.IntRangeAnnotatedTypeFactory;
import intrange.qual.IntRange;
import intrange.util.Range;


/**
 * Transfer functions for IntRange Checker
 * 
 * @author JasonMrX
 *
 */

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
        return ((IntRangeAnnotatedTypeFactory) atypefactory).
                createIntRangeAnnotation(range);
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
        CFValue value = p.getValueOfSubNode(subNode);
        AnnotationMirror rangeAnno = value.getType().getAnnotation(IntRange.class);
        return IntRangeAnnotatedTypeFactory.getIntRange(rangeAnno);
    }
    
    /**
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
                NumericalUnaryOps.BITWISE_COMPLEMENT, p);
        return createNewResult(transferResult, resultRange);
    }
    
    /**
     * For expression leftN < rightN or leftN >= rightN:
     * refine the annotation of {@code leftN} if {@code rightN} is integer
     * @param res
     *              The previous result
     * @param leftN
     * @param rightN
     * @param firstValue
     *              not used here
     * @param secondValue
     *              not used here
     * @param notLessThan
     *              If true, indicates the logic is flipped i.e (GreaterOrEqualThan)
     * @return
     */
    protected TransferResult<CFValue, CFStore> strengthenAnnotationOfLessThan (
            TransferResult<CFValue, CFStore> res,
            Node leftN, Node rightN,
            CFValue firstValue, CFValue secondValue,
            boolean notLessThan) {
        if (isCoveredKind(leftN) && isCoveredKind(rightN)) {
            Range leftRange = IntRangeAnnotatedTypeFactory.getIntRange(
                    firstValue.getType().getAnnotation(IntRange.class));
            Range rightRange = IntRangeAnnotatedTypeFactory.getIntRange(
                    secondValue.getType().getAnnotation(IntRange.class));
            Range thenRange = leftRange.lessThan(rightRange);
            Range elseRange = leftRange.greaterThanEq(rightRange);
            AnnotationMirror thenAnno = createIntRangeAnnotation(thenRange);
            AnnotationMirror elseAnno = createIntRangeAnnotation(elseRange);
            CFStore thenStore = res.getThenStore();
            CFStore elseStore = res.getElseStore();
            List<Node> secondParts = splitAssignments(leftN);
            for (Node secondPart : secondParts) {
                if (isCoveredKind(secondPart)) { 
                    Receiver secondInternal = FlowExpressions.internalReprOf(
                            analysis.getTypeFactory(), secondPart);
                    if (CFStore.canInsertReceiver(secondInternal)) {
                        thenStore = thenStore == null ? res.getThenStore()
                                : thenStore;
                        elseStore = elseStore == null ? res.getElseStore()
                                : elseStore;
                        thenStore.insertValue(secondInternal, 
                                notLessThan ? elseAnno : thenAnno);
                        elseStore.insertValue(secondInternal, 
                                notLessThan ? thenAnno : elseAnno);
                    }
                }
            }
            
            if (thenStore != null) {
                return new ConditionalTransferResult<>(res.getResultValue(),
                        thenStore, elseStore);
            }
        }
        
        return res;
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitLessThan(LessThanNode n,
            TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> res = super.visitLessThan(n, p);

        Node leftN = n.getLeftOperand();
        Node rightN = n.getRightOperand();
        CFValue leftV = p.getValueOfSubNode(leftN);
        CFValue rightV = p.getValueOfSubNode(rightN);

        return strengthenAnnotationOfLessThan(res, leftN, rightN, leftV, rightV, false);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitGreaterThanOrEqual(GreaterThanOrEqualNode n,
            TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> res = super.visitGreaterThanOrEqual(n, p);

        Node leftN = n.getLeftOperand();
        Node rightN = n.getRightOperand();
        CFValue leftV = p.getValueOfSubNode(leftN);
        CFValue rightV = p.getValueOfSubNode(rightN);

        return strengthenAnnotationOfLessThan(res, leftN, rightN, leftV, rightV, true);
    }
    
}
















































