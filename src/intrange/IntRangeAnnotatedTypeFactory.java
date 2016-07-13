package intrange;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.framework.util.GraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy;
import org.checkerframework.framework.util.MultiGraphQualifierHierarchy.MultiGraphFactory;
import org.checkerframework.javacutil.AnnotationUtils;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;

import intrange.qual.EmptyRange;
import intrange.qual.FullIntRange;
import intrange.qual.IntRange;
import intrange.util.Range;


/**
 * AnnotatedTypeFactory for the Integer Range type system
 * 
 * @author JasonMrX
 *
 */

public class IntRangeAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

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

    /*
     * private AnnotationMirror createAnnotation(String name, Long from, Long
     * to) { AnnotationBuilder builder = new AnnotationBuilder(processingEnv,
     * name); builder.setValue("from", from); builder.setValue("to", to); return
     * builder.build(); }
     */

    @Override 
    public CFTransfer createFlowTransferFunction(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        return new IntRangeTransfer(analysis);
    }
    
    @Override
    public AnnotatedTypeMirror getAnnotatedType(Tree tree) {
        if (tree.getKind() == Tree.Kind.POSTFIX_DECREMENT
                || tree.getKind() == Tree.Kind.POSTFIX_INCREMENT) {
            return getPostFixAnno((UnaryTree) tree,
                    super.getAnnotatedType(tree));
        } else {
            return super.getAnnotatedType(tree);
        }
    }
    
    private AnnotatedTypeMirror getPostFixAnno(UnaryTree tree,
            AnnotatedTypeMirror anno) {
        if (anno.hasAnnotation(IntRange.class)) {
            return postFixInt(anno, 
                    tree.getKind() == Tree.Kind.POSTFIX_INCREMENT);
        }
        return anno;
    }
    
    private AnnotatedTypeMirror postFixInt(AnnotatedTypeMirror anno, 
            boolean increment) {
        Range range = getIntRange(anno.getAnnotation(IntRange.class));
        Range resultRange;
        if (increment) {
            resultRange = new Range(range.from - 1, range.to - 1);
        } else {
            resultRange = new Range(range.from + 1, range.to + 1);
        }
        anno.replaceAnnotation(createIntRangeAnnotation(resultRange));
        return anno;
    }
    
    @Override
    public QualifierHierarchy createQualifierHierarchy(MultiGraphFactory factory) {
        return new IntRangeQualifierHierarchy(factory, EMPTYRANGE);
    }

    @Override
    protected TypeAnnotator createTypeAnnotator() {
        return new ListTypeAnnotator(new IntRangeTypeAnnotator(this), super.createTypeAnnotator());
    }

    private class IntRangeTypeAnnotator extends TypeAnnotator {

        public IntRangeTypeAnnotator(AnnotatedTypeFactory atypeFactory) {
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

        private void replaceWithFullIntRangeIfFromGreaterThanTo(AnnotatedTypeMirror atm) {
            AnnotationMirror anno = atm.getAnnotationInHierarchy(INTRANGE);

            if (anno != null && anno.getElementValues().size() == 2) {
                Long valueFrom = AnnotationUtils.getElementValue(anno, "from", Long.class, true);
                Long valueTo = AnnotationUtils.getElementValue(anno, "to", Long.class, true);
                if (valueFrom > valueTo) {
                    /*
                     * TODO bug here. type.invalid error???
                     */
                    // atm.replaceAnnotation(FULLINTRANGE);
                    // System.err.println("Hello I am the compiler: " +
                    // atm.toString());
                }

            }

        }

    }

    private final class IntRangeQualifierHierarchy extends GraphQualifierHierarchy {

        public IntRangeQualifierHierarchy(MultiGraphQualifierHierarchy.MultiGraphFactory factory,
                AnnotationMirror bottom) {
            super(factory, bottom);
        }

        
        @Override
        public AnnotationMirror greatestLowerBound(AnnotationMirror a1, AnnotationMirror a2) {
            if (isSubtype(a1, a2)) {
                return a1;
            } else if (isSubtype(a2, a1)) {
                return a2;
            } else {
                return EMPTYRANGE;
            }
        }

        @Override
        public AnnotationMirror leastUpperBound(AnnotationMirror a1, AnnotationMirror a2) {
            if (!AnnotationUtils.areSameIgnoringValues(getTopAnnotation(a1), getTopAnnotation(a2))) {
                return null;
            } else if (isSubtype(a1, a2)) {
                return a2;
            } else if (isSubtype(a2, a1)) {
                return a1;
            } else {
                Range range1 = getIntRange(a1);
                Range range2 = getIntRange(a2);
                return createIntRangeAnnotation(range1.merge(range2));
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
                Range lhsRange = getIntRange(lhs);
                Range rhsRange = getIntRange(rhs);
                if (lhsRange.from <= rhsRange.from && lhsRange.to >= rhsRange.to) {
                    return true;
                }
            }

            return false;
        }

    }

    @Override
    protected TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(new IntRangeTreeAnnotator(this), new ImplicitsTreeAnnotator(this));

    }

    protected class IntRangeTreeAnnotator extends TreeAnnotator {

        public IntRangeTreeAnnotator(IntRangeAnnotatedTypeFactory factory) {
            super(factory);
        }

        @Override
        public Void visitLiteral(LiteralTree tree, AnnotatedTypeMirror type) {
            String underlyingType = type.getUnderlyingType().toString();

            if (underlyingType.equals("int") || underlyingType.equals("java.lang.Integer")
                    || underlyingType.equals("char") || underlyingType.equals("java.lang.Character")
                    || underlyingType.equals("long") || underlyingType.equals("java.lang.Long")) {
                long value;
                switch (tree.getKind()) {
                case INT_LITERAL:
                    value = ((Number) tree.getValue()).longValue();
                    AnnotationMirror intAnno = createLiteralAnnotation(value);
                    type.replaceAnnotation(intAnno);
                    return null;
                case CHAR_LITERAL:
                    value = (Character) tree.getValue();
                    AnnotationMirror charAnno = createLiteralAnnotation(value);
                    type.replaceAnnotation(charAnno);
                    return null;
                case LONG_LITERAL:
                    value = (Long) tree.getValue();
                    AnnotationMirror longAnno = createLiteralAnnotation(value);
                    type.replaceAnnotation(longAnno);
                    return null;
                default:
                    return null;
                }
            }
            return null;
        }
    }

    public AnnotationMirror createIntRangeAnnotation(Range range) {
        if (range.from > range.to
                || range.from == Long.MIN_VALUE && range.to == Long.MAX_VALUE) {
            return FULLINTRANGE;
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, IntRange.class);
        builder.setValue("from", range.from);
        builder.setValue("to", range.to);
        return builder.build();
    }

    private AnnotationMirror createLiteralAnnotation(long value) {
        return createIntRangeAnnotation(new Range(value, value));
    }
    
    public static Range getIntRange(AnnotationMirror rangeAnno) {
        if (rangeAnno == null) {
            return new Range();
        } else {
            return new Range(
                    AnnotationUtils.getElementValue(
                            rangeAnno, "from", Long.class, true),
                    AnnotationUtils.getElementValue(
                            rangeAnno, "to", Long.class, true));
        }
    }
    
    /**
     * TODO:
     * underflow/overflow detect
     * divided by zero
     * array initialize
     * 
     */

}
