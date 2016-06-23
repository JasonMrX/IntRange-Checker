package intrange;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.AnnotationBuilder;
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
	
	protected final AnnotationMirror EMPTYRANGE, INTRANGE, FULLINTRANGE;

	public IntRangeAnnotatedTypeFactory(BaseTypeChecker checker) {
		super(checker);
		EMPTYRANGE = AnnotationUtils.fromClass(elements, EmptyRange.class);
		INTRANGE = AnnotationUtils.fromClass(elements, IntRange.class);
		FULLINTRANGE = AnnotationUtils.fromClass(elements, FullIntRange.class);
		if (this.getClass().equals(IntRangeAnnotatedTypeFactory.class)) {
			this.postInit();
		}
	}
	
	private AnnotationMirror createAnnotation(String name, int from, int to) {
		AnnotationBuilder builder = new AnnotationBuilder(processingEnv, name);
		builder.setValue("from", from);
		builder.setValue("to", to);
		return builder.build();
	}
	
	@Override 
	public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
		return new IntRangeQualifierHierarchy(factory, EMPTYRANGE);
	}
	
	@Override
	protected TypeAnnotator createTypeAnnotator() {
		return new ListTypeAnnotator(new IntRangeAnnotator(this),
				super.createTypeAnnotator());
	}
	
	private class IntRangeAnnotator extends TypeAnnotator {
		
		public IntRangeAnnotator(AnnotatedTypeFactory atypeFactory) {
			super(atypeFactory);
		}
	
		@Override
		public Void visitPrimitive(AnnotatedPrimitiveType type, Void p) {
			replaceWithFullIntRangeIfFromGreaterThanTo((AnnotatedTypeMirror) type);
			 
			return super.visitPrimitive(type, p);
		}
		
		@Override
		public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
			replaceWithFullIntRangeIfFromGreaterThanTo((AnnotatedTypeMirror) type);
			
			return super.visitDeclared(type, p);
		}
		
		private void replaceWithFullIntRangeIfFromGreaterThanTo (
				AnnotatedTypeMirror atm) {
			AnnotationMirror anno = atm.getAnnotationInHierarchy(INTRANGE);
			
			if (anno != null && anno.getElementValues().size() == 2) {
				int valueFrom = AnnotationUtils.getElementValue(anno, "from", Integer.class, true);
				int valueTo = AnnotationUtils.getElementValue(anno, "to", Integer.class, true);
				if (valueFrom > valueTo) {
					/* TODO
					 * bug here. type.invalid error???
					 */
					//atm.replaceAnnotation(FULLINTRANGE);
					//System.err.println("Hello I am the compiler: " + atm.toString());
				}
				
			}
			
		}
		
	}
	
	private final class IntRangeQualifierHierarchy extends
			GraphQualifierHierarchy {
		
		public IntRangeQualifierHierarchy(
				MultiGraphQualifierHierarchy.MultiGraphFactory factory, AnnotationMirror bottom) {
			super(factory, bottom);
		}
		
		@Override
		public AnnotationMirror greatestLowerBound(AnnotationMirror a1, 
				AnnotationMirror a2) {
			if (isSubtype(a1, a2)) {
				return a1;
			} else if (isSubtype(a2, a1)) {
				return a2;
			} else {
				return EMPTYRANGE;
			}
		}
		
		@Override
		public AnnotationMirror leastUpperBound(AnnotationMirror a1,
				AnnotationMirror a2) {
			if (!AnnotationUtils.areSameIgnoringValues(getTopAnnotation(a1), getTopAnnotation(a2))) {
	            return null;
	        } else if (isSubtype(a1, a2)) {
	            return a2;
	        } else if (isSubtype(a2, a1)) {
	            return a1;
	        } else {
	            int a1From = AnnotationUtils.getElementValue(a1, "from", Integer.class, true);
	            int a2From = AnnotationUtils.getElementValue(a2, "from", Integer.class, true);
	            int a1To = AnnotationUtils.getElementValue(a1, "from", Integer.class, true);
	            int a2To = AnnotationUtils.getElementValue(a2, "to", Integer.class, true);
	            int newFrom = Math.min(a1From, a2From);
	            int newTo = Math.max(a1To, a2To);
	            return createAnnotation(a1.getAnnotationType().toString(), newFrom, newTo);
	        }
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

