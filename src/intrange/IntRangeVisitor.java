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
public class IntRangeVisitor extends BaseTypeVisitor<IntRangeAnnotatedTypeFactory>{

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
			
			if (isCoveredKind(expFrom.getKind()) && isCoveredKind(expTo.getKind())) {
				long valueFrom, valueTo;
				String strFrom, strTo;
				
				switch (expFrom.getKind()) {
				case INT_LITERAL: 
					valueFrom = ((Number) ((LiteralTree) expFrom).getValue()).longValue();
					strFrom = Long.toString(valueFrom);
					break;
				case LONG_LITERAL:
					valueFrom = (Long) ((LiteralTree) expFrom).getValue();
					strFrom = Long.toString(valueFrom);
					break;
				default: // CHAR_LITERAL:
					valueFrom = (long) ((Character) ((LiteralTree) expFrom).getValue());
					strFrom = "'" + (new Character((char) valueFrom)) + "'";
					break;
				}
				
				switch (expTo.getKind()) {
				case INT_LITERAL:
					valueTo = ((Number) ((LiteralTree) expTo).getValue()).longValue();
					strTo = Long.toString(valueTo);
					break;
				case LONG_LITERAL:
					valueTo = (Long) ((LiteralTree) expTo).getValue();
					strTo = Long.toString(valueTo);
					break;
				default: // CHAR_LITERAL:
					valueTo = (long) ((Character) ((LiteralTree) expTo).getValue());
					strTo = "'" + (new Character((char) valueTo)) + "'";
					break;
				}
				
				if (valueFrom > valueTo) {
					checker.report(Result.warning("from(" + strFrom 
					+ ").greater.than.to(" + strTo + ")"), node);
				}
					
			}
			
		}
		
		return super.visitAnnotation(node, p);
	}
	
}
