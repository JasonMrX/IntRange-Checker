package intrange;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;

import intrange.qual.EmptyRange;
import intrange.qual.FullIntRange;
import intrange.qual.IntRange;

/**
 * AnnotatedTypeFactory for the Integer Range type system
 * 
 * @author JasonMrX
 *
 */

public class IntRangeAnnotatedTypeFactory extends BaseAnnotatedTypeFactory{
	
	protected final AnnotationMirror EMPTYRANGE, FULLINTRANGE;

	public IntRangeAnnotatedTypeFactory(BaseTypeChecker checker) {
		super(checker);
		EMPTYRANGE = AnnotationUtils.fromClass(elements, EmptyRange.class);
		FULLINTRANGE = AnnotationUtils.fromClass(elements, FullIntRange.class);
		if (this.getClass().equals(IntRangeAnnotatedTypeFactory.class)) {
			this.postInit();
		}
	}
	
	@Override 
	public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
		return new IntRangeQualifierHierarchy(factory, EMPTYRANGE);
	}
	
	/*
	private class IntRangeAnnotator extends TypeAnnotator {
		
		public IntRangeAnnotator(AnnotatedTypeFactory atypeFactory) {
			super(atypeFactory);
		}
		
	}
	*/
	
	private final class IntRangeQualifierHierarchy extends
			GraphQualifierHierarchy {
		
		public IntRangeQualifierHierarchy(
				MultiGraphQualifierHierarchy.MultiGraphFactory factory, AnnotationMirror bottom) {
			super(factory, bottom);
		}
		
		@Override
		public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
			
			if (AnnotationUtils.areSameByClass(lhs, FullIntRange.class)
					|| AnnotationUtils.areSameByClass(rhs, EmptyRange.class)) {
				return true;
			} else if (AnnotationUtils.areSameByClass(lhs, EmptyRange.class) 
					|| AnnotationUtils.areSameByClass(rhs, FullIntRange.class)) {
				return false;
			} else if (AnnotationUtils.areSameByClass(lhs, IntRange.class)
					&& AnnotationUtils.areSameByClass(rhs, IntRange.class)) {
				int lhsFrom = AnnotationUtils.getElementValue(lhs, "from", Integer.class, true);
				int lhsTo = AnnotationUtils.getElementValue(lhs, "to", Integer.class, true);
				int rhsFrom = AnnotationUtils.getElementValue(rhs, "from", Integer.class, true);
				int rhsTo = AnnotationUtils.getElementValue(rhs, "to", Integer.class, true);
				if (lhsFrom <= rhsFrom && lhsTo >= rhsTo) {
					return true;
				}
			}
			
			return false;
			/** TODO
			 * Detect From <= TO
			 */
		}
		
	}
	
}

