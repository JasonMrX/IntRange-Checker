package intrange;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.value.qual.StaticallyExecutable;
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
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
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
        
        addAliasedAnnotation(android.support.annotation.IntRange.class, INTRANGE);
        
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
    public AnnotationMirror aliasedAnnotation(AnnotationMirror anno) {
        if (AnnotationUtils.areSameByClass(
                anno, android.support.annotation.IntRange.class)) {
            Range range = getIntRange(anno);
            return createIntRangeAnnotation(range);
        }
        return super.aliasedAnnotation(anno);
    }

    @Override 
    public CFTransfer createFlowTransferFunction(
            CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        return new IntRangeTransfer(analysis);
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
                Range range1 = getIntRange(a1);
                Range range2 = getIntRange(a2);
                return createIntRangeAnnotation(range1.intersect(range2));
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
                return createIntRangeAnnotation(range1.union(range2));
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
            if (TypesUtils.isIntegral(type.getUnderlyingType())) {
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
        
        @Override
        public Void visitTypeCast(TypeCastTree tree, AnnotatedTypeMirror type) {
            if (TypesUtils.isIntegral(type.getUnderlyingType())) {
                AnnotatedTypeMirror castedAnnotation = getAnnotatedType(tree.getExpression());
                Range range = getRange(castedAnnotation);
                if (range != null) {
                    AnnotationMirror anno = createIntRangeAnnotation(range);
                    type.replaceAnnotation(anno);
                }
            }
            return null; 
        }
        
        private Range getRange(AnnotatedTypeMirror type) {
            if (!TypesUtils.isIntegral(type.getUnderlyingType())) {
                return null;
            } 
            AnnotationMirror anno = type.getAnnotationInHierarchy(FULLINTRANGE);
            return getIntRange(anno);
        }
        
        @Override
        public Void visitNewClass(NewClassTree tree, AnnotatedTypeMirror type) {
            boolean wrapperClass = isBoxedIntegral(type.getUnderlyingType());
            if (wrapperClass) {
                // get arugment values
                List<? extends ExpressionTree> arguments = tree.getArguments();
                if (arguments.size() == 1) {
                    // wrapper constructor only take one argument
                    ExpressionTree argument = arguments.get(0);
                    AnnotatedTypeMirror argType = getAnnotatedType(argument);
                    Range argRange;
                    if (argument.getKind() == Tree.Kind.STRING_LITERAL) {
                        // to handle wrapper constructor with String input
                        long value = new Integer(((LiteralTree) argument).getValue().toString());
                        argRange = new Range(value);
                    } else {
                        // to handle integral cases; return null if not integral type
                        argRange = getRange(argType);
                    }
                    if (argRange != null) {
                        AnnotationMirror anno = createIntRangeAnnotation(argRange);
                        type.replaceAnnotation(anno);
                    }
                }
            }
            return null;
        }
        
        private boolean isBoxedIntegral(TypeMirror type) {
            if (type.getKind() != TypeKind.DECLARED) {
                return false;
            }
            String qualifiedName = TypesUtils.getQualifiedName((DeclaredType) type).toString();
            return (qualifiedName.equals("java.lang.Byte")
                    || qualifiedName.equals("java.lang.Short")
                    || qualifiedName.equals("java.lang.Integer")
                    || qualifiedName.equals("java.lang.Character")
                    || qualifiedName.equals("Java.lang.Float"));
            
        }
        
        /**
         * TODO:
         * visitMethodInvocation
         * visitNewArray
         */
        
    }

    public AnnotationMirror createIntRangeAnnotation(Range range) {
        if (range.from > range.to
                || range.from == Long.MIN_VALUE && range.to == Long.MAX_VALUE) {
            return FULLINTRANGE;
        }
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
        } else if (AnnotationUtils.areSameByClass(rangeAnno, FullIntRange.class)) {
            return new Range();
        } else if (AnnotationUtils.areSameByClass(rangeAnno, EmptyRange.class)) {
            return null;
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
