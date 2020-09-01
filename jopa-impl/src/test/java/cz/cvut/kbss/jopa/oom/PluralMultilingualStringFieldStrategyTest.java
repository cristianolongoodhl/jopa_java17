package cz.cvut.kbss.jopa.oom;

import cz.cvut.kbss.jopa.environment.OWLClassM;
import cz.cvut.kbss.jopa.environment.OWLClassU;
import cz.cvut.kbss.jopa.environment.Vocabulary;
import cz.cvut.kbss.jopa.environment.utils.Generators;
import cz.cvut.kbss.jopa.environment.utils.MetamodelMocks;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.utils.Configuration;
import cz.cvut.kbss.ontodriver.descriptor.AxiomValueDescriptor;
import cz.cvut.kbss.ontodriver.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PluralMultilingualStringFieldStrategyTest {

    private static final String LANG = "en";
    private static final URI ID = Generators.createIndividualIdentifier();
    private static final NamedResource INDIVIDUAL = NamedResource.create(ID);

    @Mock
    private EntityMappingHelper mapperMock;

    private MetamodelMocks mocks;
    private final Descriptor descriptor = new EntityDescriptor();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        final Configuration configuration = new Configuration(
                Collections.singletonMap(JOPAPersistenceProperties.LANG, LANG));
        when(mapperMock.getConfiguration()).thenReturn(configuration);

        this.mocks = new MetamodelMocks();
        when(mapperMock.getEntityType(OWLClassM.class)).thenReturn(mocks.forOwlClassM().entityType());
    }

    private PluralMultilingualStringFieldStrategy<OWLClassU> createStrategy() {
        return new PluralMultilingualStringFieldStrategy<>(mocks.forOwlClassU().entityType(),
                mocks.forOwlClassU().uPluralStringAtt(), descriptor, mapperMock);
    }

    @Test
    void buildAxiomValuesTransformsAllTranslationsFromOneElementToAxiomValues() throws Exception {
        final PluralMultilingualStringFieldStrategy<OWLClassU> sut = createStrategy();
        final OWLClassU u = new OWLClassU(ID);
        final MultilingualString ms = new MultilingualString();
        ms.set("building", "en");
        ms.set("budova", "cs");
        u.setPluralStringAtt(Collections.singleton(ms));

        final AxiomValueGatherer builder = new AxiomValueGatherer(INDIVIDUAL, null);
        sut.buildAxiomValuesFromInstance(u, builder);
        final AxiomValueDescriptor valueDescriptor = OOMTestUtils.getAxiomValueDescriptor(builder);
        assertEquals(1, valueDescriptor.getAssertions().size());
        final Assertion assertion = valueDescriptor.getAssertions().iterator().next();
        assertEquals(Vocabulary.P_U_PLURAL_MULTILINGUAL_ATTRIBUTE, assertion.getIdentifier().toString());
        final List<Value<?>> values = valueDescriptor.getAssertionValues(assertion);
        verifyAssertionValues(values, ms);
    }

    private void verifyAssertionValues(List<Value<?>> values, MultilingualString... strings) {
        for (MultilingualString ms : strings) {
            for (Map.Entry<String, String> e : ms.getValue().entrySet()) {
                assertTrue(
                        values.stream().anyMatch(v -> v.getValue().equals(new LangString(e.getValue(), e.getKey()))));
            }
        }
    }

    @Test
    void buildAxiomValuesTransformsAllTranslationsFromAllElementsToAxiomValues() throws Exception {
        final PluralMultilingualStringFieldStrategy<OWLClassU> sut = createStrategy();
        final OWLClassU u = new OWLClassU(ID);
        final MultilingualString msOne = new MultilingualString();
        msOne.set("building", "en");
        msOne.set("budova", "cs");
        final MultilingualString msTwo = new MultilingualString();
        msTwo.set("construction", "en");
        msTwo.set("stavba", "cs");
        msTwo.set("construction");
        u.setPluralStringAtt(new HashSet<>(Arrays.asList(msOne, msTwo)));

        final AxiomValueGatherer builder = new AxiomValueGatherer(INDIVIDUAL, null);
        sut.buildAxiomValuesFromInstance(u, builder);
        final AxiomValueDescriptor valueDescriptor = OOMTestUtils.getAxiomValueDescriptor(builder);
        assertEquals(1, valueDescriptor.getAssertions().size());
        final Assertion assertion = valueDescriptor.getAssertions().iterator().next();
        assertEquals(Vocabulary.P_U_PLURAL_MULTILINGUAL_ATTRIBUTE, assertion.getIdentifier().toString());
        final List<Value<?>> values = valueDescriptor.getAssertionValues(assertion);
        verifyAssertionValues(values, msOne, msTwo);
    }

    @Test
    void buildAxiomValuesAddsNullValueToDescriptorWhenAttributeValueIsEmpty() throws Exception {
        final PluralMultilingualStringFieldStrategy<OWLClassU> sut = createStrategy();
        final OWLClassU u = new OWLClassU(ID);
        final AxiomValueGatherer builder = new AxiomValueGatherer(INDIVIDUAL, null);
        sut.buildAxiomValuesFromInstance(u, builder);
        final AxiomValueDescriptor valueDescriptor = OOMTestUtils.getAxiomValueDescriptor(builder);
        assertEquals(1, valueDescriptor.getAssertions().size());
        final Assertion assertion = valueDescriptor.getAssertions().iterator().next();
        assertEquals(Collections.singletonList(Value.nullValue()), valueDescriptor.getAssertionValues(assertion));
    }

    @Test
    void addValueFromAxiomAddsDifferentTranslationsToSameElement() {
        final PluralMultilingualStringFieldStrategy<OWLClassU> sut = createStrategy();
        final OWLClassU u = new OWLClassU(ID);
        final Assertion assertion = Assertion
                .createDataPropertyAssertion(URI.create(Vocabulary.P_U_PLURAL_MULTILINGUAL_ATTRIBUTE), false);
        final Axiom<LangString> axOne = new AxiomImpl<>(INDIVIDUAL, assertion,
                new Value<>(new LangString("construction", "en")));
        final Axiom<LangString> axTwo = new AxiomImpl<>(INDIVIDUAL, assertion,
                new Value<>(new LangString("stavba", "cs")));
        sut.addValueFromAxiom(axOne);
        sut.addValueFromAxiom(axTwo);
        sut.buildInstanceFieldValue(u);
        assertEquals(1, u.getPluralStringAtt().size());
        final MultilingualString msResult = u.getPluralStringAtt().iterator().next();
        assertEquals("construction", msResult.get("en"));
        assertEquals("stavba", msResult.get("cs"));
    }

    @Test
    void addValueFromAxiomAddsValueInSameLanguageAsNewElementIntoCollection() {
        final PluralMultilingualStringFieldStrategy<OWLClassU> sut = createStrategy();
        final OWLClassU u = new OWLClassU(ID);
        final Assertion assertion = Assertion
                .createDataPropertyAssertion(URI.create(Vocabulary.P_U_PLURAL_MULTILINGUAL_ATTRIBUTE), false);
        final Axiom<LangString> axOne = new AxiomImpl<>(INDIVIDUAL, assertion,
                new Value<>(new LangString("construction", "en")));
        final Axiom<LangString> axTwo = new AxiomImpl<>(INDIVIDUAL, assertion,
                new Value<>(new LangString("building", "en")));
        sut.addValueFromAxiom(axOne);
        sut.addValueFromAxiom(axTwo);
        sut.buildInstanceFieldValue(u);
        assertEquals(2, u.getPluralStringAtt().size());
        assertTrue(u.getPluralStringAtt().stream()
                    .anyMatch(ms -> ms.contains("en") && ms.get("en").equals("construction")));
        assertTrue(
                u.getPluralStringAtt().stream().anyMatch(ms -> ms.contains("en") && ms.get("en").equals("building")));
    }

    @Test
    void addValueFromAxiomAddsTranslationsToExistingElements() {
        final PluralMultilingualStringFieldStrategy<OWLClassU> sut = createStrategy();
        final OWLClassU u = new OWLClassU(ID);
        final Assertion assertion = Assertion
                .createDataPropertyAssertion(URI.create(Vocabulary.P_U_PLURAL_MULTILINGUAL_ATTRIBUTE), false);
        final Axiom<LangString> axOne = new AxiomImpl<>(INDIVIDUAL, assertion,
                new Value<>(new LangString("construction", "en")));
        final Axiom<LangString> axTwo = new AxiomImpl<>(INDIVIDUAL, assertion,
                new Value<>(new LangString("building", "en")));
        final Axiom<LangString> axThree = new AxiomImpl<>(INDIVIDUAL, assertion,
                new Value<>(new LangString("stavba", "cs")));
        sut.addValueFromAxiom(axOne);
        sut.addValueFromAxiom(axTwo);
        sut.addValueFromAxiom(axThree);
        sut.buildInstanceFieldValue(u);
        assertEquals(2, u.getPluralStringAtt().size());
        assertTrue(
                u.getPluralStringAtt().stream().anyMatch(ms -> ms.contains("en") && ms.get("en").equals("building")));
        assertTrue(u.getPluralStringAtt().stream()
                    .anyMatch(ms -> ms.contains("en") && ms.get("en").equals("construction")));
        assertTrue(u.getPluralStringAtt().stream().anyMatch(ms -> ms.contains("cs") && ms.get("cs").equals("stavba")));
    }

    @Test
    void buildInstanceFieldValueLeavesFieldNullWhenNoValuesWereAdded() {
        final PluralMultilingualStringFieldStrategy<OWLClassU> sut = createStrategy();
        final OWLClassU u = new OWLClassU(ID);
        sut.buildInstanceFieldValue(u);
        assertNull(u.getPluralStringAtt());
    }
}
