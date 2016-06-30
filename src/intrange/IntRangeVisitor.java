package intrange;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

import intrange.qual.FullIntRange;
import intrange.qual.IntRange;

/**
 * Visitor for the Integer Range Type System
 * 
 * @author JasonMrX
 *
 */
public class IntRangeVisitor extends BaseTypeVisitor<IntRangeAnnotatedTypeFactory> {

    private Set<Kind> coveredKinds;

    public IntRangeVisitor(BaseTypeChecker checker) {
        super(checker);

        coveredKinds = new HashSet<Kind>(3);
        coveredKinds.add(Tree.Kind.INT_LITERAL);
        coveredKinds.add(Tree.Kind.LONG_LITERAL);
        coveredKinds.add(Tree.Kind.CHAR_LITERAL);

    }

    private boolean isCoveredKind(Kind k) {
        return coveredKinds.contains(k);
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
                    checker.report(Result.warning("from.greater.than.to"), node);
                }

            }

        }

        return super.visitAnnotation(node, p);
    }

}
