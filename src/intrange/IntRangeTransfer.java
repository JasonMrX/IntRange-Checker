package intrange;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.dataflow.analysis.FlowExpressions;
import org.checkerframework.dataflow.analysis.FlowExpressions.Receiver;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.BitwiseComplementNode;
import org.checkerframework.dataflow.cfg.node.EqualToNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanNode;
import org.checkerframework.dataflow.cfg.node.GreaterThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.IntegerDivisionNode;
import org.checkerframework.dataflow.cfg.node.IntegerRemainderNode;
import org.checkerframework.dataflow.cfg.node.LeftShiftNode;
import org.checkerframework.dataflow.cfg.node.LessThanNode;
import org.checkerframework.dataflow.cfg.node.LessThanOrEqualNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NotEqualNode;
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
import org.checkerframework.javacutil.TypesUtils;

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
    
    public IntRangeTransfer(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(analysis);
        atypefactory = analysis.getTypeFactory();
    }

    private boolean isCoveredKind(Node n) {
        return TypesUtils.isIntegral(n.getType());
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
                stringVal, result.getResultValue().getUnderlyingType());
        return new RegularTransferResult<>(newResultValue,
                result.getRegularStore());
    }
    
    private Range getIntRange(CFValue value) {
        for (AnnotationMirror anno : value.getAnnotations()) {
            if (AnnotationUtils.areSameByClass(anno, IntRange.class)) {
                return IntRangeAnnotatedTypeFactory.getIntRange(anno);
            }
        }
        return new Range();
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
        Range leftRange = getIntRange(p.getValueOfSubNode(leftNode));
        Range rightRange = getIntRange(p.getValueOfSubNode(rightNode));
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
        Range operandRange = getIntRange(p.getValueOfSubNode(operand));
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
    
    enum ComparisonOperators {
        EQUAL, NOT_EQUAL, GREATER_THAN, GREATER_THAN_EQ, LESS_THAN, LESS_THAN_EQ;
    }
    
    /**
     * For expression leftN < rightN or leftN >= rightN:
     * refine the annotation of {@code leftN} if {@code rightN} is integer
     * @param res
     *              The previous result
     * @param leftN
     * @param rightN
     * @param firstValue
     *              get left annotation
     * @param secondValue
     *              get right annotation
     * @param notLessThan
     *              If true, indicates the logic is flipped i.e (GreaterOrEqualThan)
     * @return
     */
    protected TransferResult<CFValue, CFStore> strengthenAnnotationOfComparison (
            TransferResult<CFValue, CFStore> res,
            Node leftN, Node rightN,
            CFValue firstValue, CFValue secondValue,
            ComparisonOperators op) {
        if (isCoveredKind(leftN) && isCoveredKind(rightN)) {
            Range leftRange = getIntRange(firstValue);
            Range rightRange = getIntRange(secondValue);
            Range thenRange, elseRange;
            switch (op) {
            case LESS_THAN:
                thenRange = leftRange.lessThan(rightRange);
                elseRange = leftRange.greaterThanEq(rightRange);
                break;
            case GREATER_THAN:
                thenRange = leftRange.greaterThan(rightRange);
                elseRange = leftRange.lessThanEq(rightRange);
                break;
            case LESS_THAN_EQ:
                thenRange = leftRange.lessThanEq(rightRange);
                elseRange = leftRange.greaterThan(rightRange);
                break;
            case GREATER_THAN_EQ:
                thenRange = leftRange.greaterThanEq(rightRange);
                elseRange = leftRange.lessThan(rightRange);
                break;
            case EQUAL:
                thenRange = leftRange.equalTo(rightRange);
                elseRange = leftRange.notEqualTo(rightRange);
                break;
            case NOT_EQUAL:
                thenRange = leftRange.notEqualTo(rightRange);
                elseRange = leftRange.equalTo(rightRange);
                break;
            default:
                throw new UnsupportedOperationException();
            }
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
                        thenStore.insertValue(secondInternal, thenAnno);
                        elseStore.insertValue(secondInternal, elseAnno);
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

        return strengthenAnnotationOfComparison(res, leftN, rightN, leftV, rightV, 
                ComparisonOperators.LESS_THAN);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitLessThanOrEqual(LessThanOrEqualNode n,
            TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> res = super.visitLessThanOrEqual(n, p);

        Node leftN = n.getLeftOperand();
        Node rightN = n.getRightOperand();
        CFValue leftV = p.getValueOfSubNode(leftN);
        CFValue rightV = p.getValueOfSubNode(rightN);

        return strengthenAnnotationOfComparison(res, leftN, rightN, leftV, rightV, 
                ComparisonOperators.LESS_THAN_EQ);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitGreaterThan(GreaterThanNode n,
            TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> res = super.visitGreaterThan(n, p);

        Node leftN = n.getLeftOperand();
        Node rightN = n.getRightOperand();
        CFValue leftV = p.getValueOfSubNode(leftN);
        CFValue rightV = p.getValueOfSubNode(rightN);

        return strengthenAnnotationOfComparison(res, leftN, rightN, leftV, rightV, 
                ComparisonOperators.GREATER_THAN);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitGreaterThanOrEqual(GreaterThanOrEqualNode n,
            TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> res = super.visitGreaterThanOrEqual(n, p);

        Node leftN = n.getLeftOperand();
        Node rightN = n.getRightOperand();
        CFValue leftV = p.getValueOfSubNode(leftN);
        CFValue rightV = p.getValueOfSubNode(rightN);

        return strengthenAnnotationOfComparison(res, leftN, rightN, leftV, rightV, 
                ComparisonOperators.GREATER_THAN_EQ);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitEqualTo(EqualToNode n,
            TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> res = super.visitEqualTo(n, p);

        Node leftN = n.getLeftOperand();
        Node rightN = n.getRightOperand();
        CFValue leftV = p.getValueOfSubNode(leftN);
        CFValue rightV = p.getValueOfSubNode(rightN);

        return strengthenAnnotationOfComparison(res, leftN, rightN, leftV, rightV, 
                ComparisonOperators.EQUAL);
    }
    
    @Override
    public TransferResult<CFValue, CFStore> visitNotEqual(NotEqualNode n,
            TransferInput<CFValue, CFStore> p) {
        TransferResult<CFValue, CFStore> res = super.visitNotEqual(n, p);

        Node leftN = n.getLeftOperand();
        Node rightN = n.getRightOperand();
        CFValue leftV = p.getValueOfSubNode(leftN);
        CFValue rightV = p.getValueOfSubNode(rightN);

        return strengthenAnnotationOfComparison(res, leftN, rightN, leftV, rightV, 
                ComparisonOperators.NOT_EQUAL);
    }
    
}
















































