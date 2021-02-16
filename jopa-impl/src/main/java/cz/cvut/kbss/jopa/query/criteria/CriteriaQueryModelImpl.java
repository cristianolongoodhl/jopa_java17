package cz.cvut.kbss.jopa.query.criteria;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.metamodel.Attribute;
import cz.cvut.kbss.jopa.model.query.criteria.*;

import java.util.Collection;
import java.util.List;

public class CriteriaQueryModelImpl<T> implements CriteriaQueryModel<T> {

    protected final Class<T> type;
    private final EntityManager em;

    public CriteriaQueryModelImpl(Class<T> type, EntityManager em) {
        this.type = type;
        this.em = em;
    }

    @Override
    public <Y> Path<Y> getAttr(String attributeName) throws IllegalArgumentException {
        Attribute attribute = em.getMetamodel().entity(type).getAttribute(attributeName);
        System.out.println("attribute name was:" + attributeName);
        System.out.println("attribute founded name:" + attribute.getName());
        System.out.println("attribute founded IRI:" + attribute.getIRI());
        System.out.println("attribute founded JavaType simple name:" + attribute.getJavaType().getSimpleName());
        System.out.println("attribute.getDeclaringType().toString():" + attribute.getDeclaringType().toString());
        return null;
    }

    @Override
    public Path<?> getParentPath() {
        return null;
    }

    @Override
    public Expression<Class<? extends T>> type() {
        return null;
    }

    @Override
    public Predicate in(Collection<?> values) {
        return null;
    }

    @Override
    public Predicate in(Expression<?> values) {
        return null;
    }

    @Override
    public Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
        return null;
    }

    @Override
    public Predicate and(Predicate... restrictions) {
        return null;
    }

    @Override
    public Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
        return null;
    }

    @Override
    public Predicate or(Predicate... restrictions) {
        return null;
    }

    @Override
    public Predicate not() {
        return null;
    }

    @Override
    public boolean isNegated() {
        return false;
    }

    @Override
    public Predicate attrEquals(Expression<?> x, Expression<?> y) {
        return null;
    }

    @Override
    public Predicate attrEquals(Expression<?> x, Object y) {
        return null;
    }

    @Override
    public Predicate attrNotEquals(Expression<?> x, Expression<?> y) {
        return null;
    }

    @Override
    public Predicate attrNotEquals(Expression<?> x, Object y) {
        return null;
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate attrGreaterThan(Expression<? extends Y> x, Expression<? extends Y> y) {
        return null;
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate attrGreaterThan(Expression<? extends Y> x, Y y) {
        return null;
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate attrGreaterOrEqual(Expression<? extends Y> x, Expression<? extends Y> y) {
        return null;
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate attrGreaterOrEqual(Expression<? extends Y> x, Y y) {
        return null;
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate attrLessThan(Expression<? extends Y> x, Expression<? extends Y> y) {
        return null;
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate attrLessThan(Expression<? extends Y> x, Y y) {
        return null;
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate attrLessOrEqual(Expression<? extends Y> x, Expression<? extends Y> y) {
        return null;
    }

    @Override
    public <Y extends Comparable<? super Y>> Predicate attrLessOrEqual(Expression<? extends Y> x, Y y) {
        return null;
    }

    @Override
    public boolean CompoundedSelection() {
        return false;
    }

    @Override
    public List<Selection<?>> getCompoundedSelectionItems() {
        return null;
    }

    @Override
    public Selection<T> alias(String name) {
        return null;
    }
}
