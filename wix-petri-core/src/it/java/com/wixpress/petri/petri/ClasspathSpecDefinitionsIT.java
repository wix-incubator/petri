package com.wixpress.petri.petri;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import specs.valid.ValidStubSpecDefinition_1;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 * @author sagyr
 * @since 10/6/13
 */
public class ClasspathSpecDefinitionsIT {

    public static final String VALID_SPECS_PACKAGE = "specs.valid";
    public static final String INVALID_SPECS_PACKAGE = "specs.invalid";
    public static final String VALID_AND_INVALID_SPECS_PACKAGE = "specs";
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private PetriSupport petriSupport;

    @Before
    public void setup() {
        petriSupport = context.mock(PetriSupport.class);
    }

    @Test
    public void collectsAllSpecsFromSpecDefinitions() throws Exception {
        ClasspathSpecDefinitions specDefs = new ClasspathSpecDefinitions(VALID_SPECS_PACKAGE, petriSupport);
        SpecDefinition value = new ValidStubSpecDefinition_1();
        assertThat(specDefs.get(), is(asList(value)));
    }

    @Test
    public void notifiesErrors() {
        ClasspathSpecDefinitions specDefs = new ClasspathSpecDefinitions(INVALID_SPECS_PACKAGE, petriSupport);
        context.checking(new Expectations() {{
            oneOf(petriSupport).report(with(ClasspathSpecDefinitions.ERROR_CREATING_SPEC), with(containsString("InvalidStubSpecDefinition_1")),
                    with((String) null));
        }});
        assertThat(specDefs.get(), is(empty()));
    }

    @Test
    public void retrievesOnlyValidSpecs() {
        ClasspathSpecDefinitions specDefs = new ClasspathSpecDefinitions(VALID_AND_INVALID_SPECS_PACKAGE, petriSupport);
        context.checking(new Expectations() {{
            oneOf(petriSupport).report(with(ClasspathSpecDefinitions.ERROR_CREATING_SPEC), with(containsString("InvalidStubSpecDefinition_1")),
                    with((String) null));
        }});
        SpecDefinition validSpecDef = new ValidStubSpecDefinition_1();
        assertThat(specDefs.get(), is(asList(validSpecDef)));
    }

}
