package cz.cvut.kbss.jopa.query.criteria.expressions;

import cz.cvut.kbss.jopa.model.metamodel.Attribute;
import cz.cvut.kbss.jopa.model.metamodel.Metamodel;
import cz.cvut.kbss.jopa.query.criteria.CriteriaParameterFiller;
import cz.cvut.kbss.jopa.sessions.CriteriaBuilder;

public class ExpressionAttributeImpl<Y> extends AbstractPathExpression<Y> {

    protected String attributeName;
    protected Attribute attribute;

    public ExpressionAttributeImpl(Class<Y> type, AbstractPathExpression pathSource, Metamodel metamodel, Attribute attribute, CriteriaBuilder cb) {
        super(type, pathSource, metamodel, cb);
        this.attribute = attribute;
        this.attributeName = attribute.getName();
    }


    @Override
    public void setExpressionToQuery(StringBuilder query, CriteriaParameterFiller parameterFiller) {
        if (this.pathSource != null){
            this.pathSource.setExpressionToQuery(query, parameterFiller);
            query.append("." + attributeName);
        } else {
            query.append(attributeName);
        }
    }
}
