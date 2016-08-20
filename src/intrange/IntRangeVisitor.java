package intrange;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

import intrange.qual.EmptyRange;
import intrange.qual.FullIntRange;
import intrange.qual.IntRange;
import intrange.util.Range;

/**
 * Visitor for the Integer Range Type System
 * 
 * @author JasonMrX
 *
 */
public class IntRangeVisitor extends BaseTypeVisitor<IntRangeAnnotatedTypeFactory> {

    private Set<Kind> coveredKinds;
    
    private Set<TypeKind> coveredTypeKinds;
    
    protected final AnnotationMirror EMPTYRANGE, INTRANGE, FULLINTRANGE;

    public IntRangeVisitor(BaseTypeChecker checker) {
        super(checker);

        coveredKinds = new HashSet<Kind>(3);
        coveredKinds.add(Kind.INT_LITERAL);
        coveredKinds.add(Kind.LONG_LITERAL);
        coveredKinds.add(Kind.CHAR_LITERAL);
        
        coveredTypeKinds = new HashSet<TypeKind>(5);
        coveredTypeKinds.add(TypeKind.BYTE);
        coveredTypeKinds.add(TypeKind.SHORT);
        coveredTypeKinds.add(TypeKind.CHAR);
        coveredTypeKinds.add(TypeKind.INT);
        coveredTypeKinds.add(TypeKind.LONG);

        EMPTYRANGE = AnnotationUtils.fromClass(elements, EmptyRange.class);
        INTRANGE = AnnotationUtils.fromClass(elements, IntRange.class);
        FULLINTRANGE = AnnotationUtils.fromClass(elements, FullIntRange.class);
    }

    private boolean isCoveredKind(Kind k) {
        return coveredKinds.contains(k);
    }
    
    private boolean isCoveredTypeKind(TypeKind tk) {
        return coveredTypeKinds.contains(tk);
    }

    private long getValueFromCoveredKinds(ExpressionTree exp) {
        switch (exp.getKind()) {
        case INT_LITERAL:
            return ((Number) ((LiteralTree) exp).getValue()).longValue();
        case LONG_LITERAL:
            return (Long) ((LiteralTree) exp).getValue();
        case CHAR_LITERAL:
            return (long) ((Character) ((LiteralTree) exp).getValue());
        default:
            throw new IllegalArgumentException(
                    "exp should be within the covered kinds (INT_LITERAL, LONG_LITERAL, CHAR_LITERAL"); 
        }
    }

    @Override
    protected IntRangeAnnotatedTypeFactory createTypeFactory() {
        return new IntRangeAnnotatedTypeFactory(checker);
    }

    /**
     * Issues a warning if from > to
     */
    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        List<? extends ExpressionTree> args = node.getArguments();

        if (args.isEmpty()) {
            // Nothing to do if there are no annotation arguments
            return super.visitAnnotation(node, p);
        }

        Element elem = TreeInfo.symbol((JCTree) node.getAnnotationType());

        if (!elem.toString().equals(IntRange.class.getName())) {
            return super.visitAnnotation(node, p);
        }

        if (args.size() == 2 && args.get(0).getKind() == Kind.ASSIGNMENT && args.get(1).getKind() == Kind.ASSIGNMENT) {

            ExpressionTree expFrom = ((AssignmentTree) args.get(0)).getExpression();
            ExpressionTree expTo = ((AssignmentTree) args.get(1)).getExpression();

            if (isCoveredKind(expFrom.getKind()) && isCoveredKind(expTo.getKind())) {
                long valueFrom = getValueFromCoveredKinds(expFrom);
                long valueTo = getValueFromCoveredKinds(expTo);

                if (valueFrom > valueTo) {
                    checker.report(Result.warning("from.greater.than.to", valueFrom, valueTo), node);
                }

            }

        }

        return super.visitAnnotation(node, p);
    }
    
    @Override
    public Void visitBinary(BinaryTree node, Void p) {
        ExpressionTree nodeLeft = node.getLeftOperand();
        ExpressionTree nodeRight = node.getRightOperand();
        AnnotatedTypeMirror typeLeft = atypeFactory.getAnnotatedType(nodeLeft);
        AnnotatedTypeMirror typeRight = atypeFactory.getAnnotatedType(nodeRight);
        if (!isCoveredTypeKind(typeLeft.getKind()) 
                || !isCoveredTypeKind(typeRight.getKind())) {
            return super.visitBinary(node, p);
        }
     
        switch(node.getKind()) {
        case REMAINDER:
        case DIVIDE:
        {
            Range rangeRight = getIntRange(node.getRightOperand());
            if (rangeRight.from <= 0 && rangeRight.to >= 0) {
                checker.report(Result.warning("possible.division.by.zero", rangeRight.from, rangeRight.to), node.getRightOperand());
            }
            break;
        }
        case LEFT_SHIFT:
        case RIGHT_SHIFT:
        case UNSIGNED_RIGHT_SHIFT:    
        {
            Range rangeRight = getIntRange(node.getRightOperand());
            if (rangeRight.from < 0 || rangeRight.to > 31) {
                // assume from <= to here
                checker.report(Result.warning("shift.out.of.range"), node.getRightOperand());
            }
        }
        default:
        }
        return super.visitBinary(node, p);
    }
    
    @Override
    public Void visitNewArray(NewArrayTree node, Void p) {
        List<? extends ExpressionTree> dimensions = node.getDimensions();
        for (ExpressionTree dim : dimensions) {
            Range rangeDim = getIntRange(dim);
            if (rangeDim.from < 0) {
                checker.report(Result.warning("possible.negative.array.dimension"), dim);
            }
        }
        return super.visitNewArray(node, p);
    }
    
    private Range getIntRange(ExpressionTree node) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        AnnotationMirror anno = type.getAnnotationInHierarchy(FULLINTRANGE);
        return IntRangeAnnotatedTypeFactory.getIntRange(anno);
    }

}
