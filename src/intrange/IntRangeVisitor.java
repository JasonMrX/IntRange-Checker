package intrange;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;

import java.util.List;

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
public class IntRangeVisitor extends BaseTypeVisitor<IntRangeAnnotatedTypeFactory>{

	public IntRangeVisitor(BaseTypeChecker checker) {
		super(checker);
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

		if (!(elem.toString().equals(FullIntRange.class.getName())
				|| elem.toString().equals(IntRange.class.getName()))) {
			return super.visitAnnotation(node, p);
		}
		if (args.size() == 2 
				&& args.get(0).getKind() == Kind.ASSIGNMENT
				&& args.get(1).getKind() == Kind.ASSIGNMENT) {					

			ExpressionTree expFrom = ((AssignmentTree) args.get(0)).getExpression();
			ExpressionTree expTo = ((AssignmentTree) args.get(1)).getExpression();
			
			/*
			 * ugly. need to be refactored
			 */
			if ((expFrom.getKind() == Tree.Kind.INT_LITERAL || expFrom.getKind() == Tree.Kind.LONG_LITERAL)
					&& (expTo.getKind() == Tree.Kind.INT_LITERAL || expTo.getKind() == Tree.Kind.LONG_LITERAL)) {
				Number numFrom = expFrom.getKind() == Tree.Kind.INT_LITERAL ?
									(Integer)((LiteralTree) expFrom).getValue() :
									(Long)((LiteralTree) expFrom).getValue();
				Number numTo = expTo.getKind() == Tree.Kind.INT_LITERAL ?
									(Integer)((LiteralTree) expTo).getValue() :
									(Long)((LiteralTree) expTo).getValue();
				long valueFrom = numFrom.longValue();
				long valueTo = numTo.longValue();
				if (valueFrom > valueTo) {
					checker.report(Result.warning("from(" + Long.toString(valueFrom) 
						+ ").greater.than.to(" + Long.toString(valueTo) + ")"), node);
				}
			}
			
		}
		
		return super.visitAnnotation(node, p);
	}
	
}
