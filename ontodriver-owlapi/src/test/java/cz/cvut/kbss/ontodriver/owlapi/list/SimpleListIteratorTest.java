package cz.cvut.kbss.ontodriver.owlapi.list;

import cz.cvut.kbss.ontodriver.owlapi.AxiomAdapter;
import cz.cvut.kbss.ontodriver.owlapi.connector.OntologyStructures;
import cz.cvut.kbss.ontodriver.owlapi.environment.TestUtils;
import cz.cvut.kbss.ontodriver_new.descriptors.SimpleListDescriptor;
import cz.cvut.kbss.ontodriver_new.descriptors.SimpleListDescriptorImpl;
import cz.cvut.kbss.ontodriver_new.model.Axiom;
import cz.cvut.kbss.ontodriver_new.model.NamedResource;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static cz.cvut.kbss.ontodriver.owlapi.list.ListHandlerTestBase.LIST_ITEMS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SimpleListIteratorTest {

    private static final String SUBJECT = "http://krizik.felk.cvut.cz/jopa#Individual";

    private OntologyStructures snapshot;
    private SimpleListTestHelper testHelper;
    private AxiomAdapter axiomAdapter;

    private SimpleListDescriptor descriptor;

    private SimpleListIterator iterator;

    @Before
    public void setUp() throws Exception {
        this.snapshot = TestUtils.initRealOntology(null);
        final OWLNamedIndividual individual = snapshot.getDataFactory().getOWLNamedIndividual(IRI.create(SUBJECT));
        this.axiomAdapter = new AxiomAdapter(snapshot.getDataFactory(), "en");
        this.testHelper = new SimpleListTestHelper(snapshot, individual);
        this.descriptor = new SimpleListDescriptorImpl(NamedResource.create(SUBJECT), ListHandlerTestBase.HAS_LIST,
                ListHandlerTestBase.HAS_NEXT);
        this.iterator = new SimpleListIterator(descriptor, snapshot, axiomAdapter);
    }

    @Test(expected = NoSuchElementException.class)
    public void nextWithoutMoreElementsThrowsException() {
        testHelper.persistList(LIST_ITEMS);
        while (iterator.hasNext()) {
            iterator.next();
        }
        iterator.next();
    }

    @Test
    public void testBasicIteration() throws Exception {
        testHelper.persistList(LIST_ITEMS);
        verifyIterationContent(iterator, LIST_ITEMS);
    }

    private void verifyIterationContent(SimpleListIterator it, List<URI> expected) {
        int i = 0;
        while (it.hasNext()) {
            final Axiom<NamedResource> item = it.next();
            assertEquals(expected.get(i), item.getValue().getValue().getIdentifier());
            i++;
        }
        assertEquals(expected.size(), i);
    }

    @Test
    public void removeWithoutReconnectOnAllElementsClearsTheWholeList() {
        testHelper.persistList(LIST_ITEMS);
        final List<OWLOntologyChange> changes = new ArrayList<>();
        while (iterator.hasNext()) {
            iterator.next();
            changes.addAll(iterator.removeWithoutReconnect());
        }
        snapshot.getOntologyManager().applyChanges(changes);
        final SimpleListIterator it = new SimpleListIterator(descriptor, snapshot, axiomAdapter);
        assertFalse(it.hasNext());
    }

    @Test
    public void removeWithoutReconnectClearsRestOfList() throws Exception {
        testHelper.persistList(LIST_ITEMS);
        final List<OWLOntologyChange> changes = new ArrayList<>();
        int i = 0;
        while (iterator.hasNext()) {
            iterator.next();
            if (i >= 5) {
                changes.addAll(iterator.removeWithoutReconnect());
            }
            i++;
        }
        snapshot.getOntologyManager().applyChanges(changes);
        final SimpleListIterator it = new SimpleListIterator(descriptor, snapshot, axiomAdapter);
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(5, count);
    }

    @Test
    public void testReplaceHead() throws Exception {
        final List<URI> lst = new ArrayList<>(LIST_ITEMS.subList(0, 5));
        testHelper.persistList(lst);
        lst.set(0, LIST_ITEMS.get(8));
        final List<OWLOntologyChange> changes = new ArrayList<>();
        iterator.next();
        changes.addAll(iterator.replaceNode(NamedResource.create(lst.get(0))));
        snapshot.getOntologyManager().applyChanges(changes);

        final SimpleListIterator it = new SimpleListIterator(descriptor, snapshot, axiomAdapter);
        verifyIterationContent(it, lst);
    }

    @Test
    public void testReplaceNodeInsideList() throws Exception {
        final List<URI> lst = new ArrayList<>(LIST_ITEMS.subList(0, 5));
        testHelper.persistList(lst);
        int replaceIndex = 2;
        lst.set(replaceIndex, LIST_ITEMS.get(8));
        final List<OWLOntologyChange> changes = new ArrayList<>();
        int i = 0;
        while (iterator.hasNext() && i < replaceIndex) {
            iterator.next();
            i++;
        }
        iterator.next();
        changes.addAll(iterator.replaceNode(NamedResource.create(lst.get(replaceIndex))));
        snapshot.getOntologyManager().applyChanges(changes);

        final SimpleListIterator it = new SimpleListIterator(descriptor, snapshot, axiomAdapter);
        verifyIterationContent(it, lst);
    }

    @Test
    public void testReplaceLastNode() throws Exception {
        final List<URI> lst = new ArrayList<>(LIST_ITEMS.subList(0, 5));
        testHelper.persistList(lst);
        int replaceIndex = lst.size() - 1;
        lst.set(replaceIndex, LIST_ITEMS.get(8));
        final List<OWLOntologyChange> changes = new ArrayList<>();
        int i = 0;
        while (iterator.hasNext() && i < replaceIndex) {
            iterator.next();
            i++;
        }
        iterator.next();
        changes.addAll(iterator.replaceNode(NamedResource.create(lst.get(replaceIndex))));
        snapshot.getOntologyManager().applyChanges(changes);

        final SimpleListIterator it = new SimpleListIterator(descriptor, snapshot, axiomAdapter);
        verifyIterationContent(it, lst);
    }
}