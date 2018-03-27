package cz.cvut.kbss.ontodriver.jena.list;

import cz.cvut.kbss.ontodriver.descriptor.ReferencedListDescriptor;
import cz.cvut.kbss.ontodriver.descriptor.ReferencedListDescriptorImpl;
import cz.cvut.kbss.ontodriver.descriptor.ReferencedListValueDescriptor;
import cz.cvut.kbss.ontodriver.jena.environment.Generator;
import cz.cvut.kbss.ontodriver.model.Assertion;
import cz.cvut.kbss.ontodriver.model.NamedResource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class ReferencedListHandlerTest
        extends ListHandlerTestBase<ReferencedListDescriptor, ReferencedListValueDescriptor> {

    private static final Assertion HAS_CONTENT = Assertion
            .createObjectPropertyAssertion(Generator.generateUri(), false);
    private static final Property HAS_CONTENT_PROPERTY = createProperty(HAS_CONTENT.getIdentifier().toString());

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.handler = new ReferencedListHandler(connectorMock);
        super.setUp();
        listUtil.setHasContent(HAS_CONTENT_PROPERTY);
    }

    @Override
    List<URI> generateList(String context) {
        if (context != null) {
            return listUtil.generateReferencedList(context);
        } else {
            return listUtil.generateReferencedList();
        }
    }

    @Override
    ReferencedListDescriptor listDescriptor() {
        return new ReferencedListDescriptorImpl(OWNER, HAS_LIST, HAS_NEXT, HAS_CONTENT);
    }

    @Override
    ReferencedListValueDescriptor listValueDescriptor() {
        return new ReferencedListValueDescriptor(OWNER, HAS_LIST, HAS_NEXT, HAS_CONTENT);
    }

    @Test
    public void persistListInsertsStatementsCorrespondingToList() {
        final List<URI> list = listUtil.generateList();
        final ReferencedListValueDescriptor descriptor = listValueDescriptor();
        list.forEach(item -> descriptor.addValue(NamedResource.create(item)));
        handler.persistList(descriptor);
        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(connectorMock).add(captor.capture());
        final List<Statement> added = captor.getValue();
        for (URI value : list) {
            assertTrue(added.stream().anyMatch(s -> s.getObject().asResource().getURI().equals(value.toString()) &&
                    s.getPredicate().equals(HAS_CONTENT_PROPERTY)));
        }
    }

    @Test
    public void persistListWithContextInsertsStatementsCorrespondingToListIntoContext() {
        final List<URI> list = listUtil.generateList();
        final ReferencedListValueDescriptor descriptor = listValueDescriptor();
        list.forEach(item -> descriptor.addValue(NamedResource.create(item)));
        final URI context = Generator.generateUri();
        descriptor.setContext(context);
        handler.persistList(descriptor);
        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(connectorMock).add(captor.capture(), eq(context.toString()));
        final List<Statement> added = captor.getValue();
        for (URI value : list) {
            assertTrue(added.stream().anyMatch(s -> s.getObject().asResource().getURI().equals(value.toString()) &&
                    s.getPredicate().equals(HAS_CONTENT_PROPERTY)));
        }
    }
}