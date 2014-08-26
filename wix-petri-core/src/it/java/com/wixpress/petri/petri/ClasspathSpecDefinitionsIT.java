package com.wixpress.petri.petri;

import com.wixpress.petri.laboratory.ErrorHandler;
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
    public static final String ABSTRACT_SPECS_PACKAGE = "specs.abstracts";
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private ErrorHandler errorHandler;

    private void expectErrorForInvalidSpec() {
        context.checking(new Expectations() {{
            oneOf(errorHandler).handle(
                    with(allOf(containsString(String.format(ClasspathSpecDefinitions.ERROR_CREATING_SPEC, "")), containsString("InvalidStubSpecDefinition_1"))),
                    with(any(IllegalAccessException.class)));
        }});
    }

    @Before
    public void setup() {
        errorHandler = context.mock(ErrorHandler.class);
    }

    @Test
    public void collectsAllSpecsFromSpecDefinitions() throws Exception {
        ClasspathSpecDefinitions specDefs = new ClasspathSpecDefinitions(VALID_SPECS_PACKAGE, errorHandler);
        SpecDefinition value = new ValidStubSpecDefinition_1();
        assertThat(specDefs.get(), is(asList(value)));
    }

    @Test
    public void notifiesErrors() {
        ClasspathSpecDefinitions specDefs = new ClasspathSpecDefinitions(INVALID_SPECS_PACKAGE, errorHandler);
        expectErrorForInvalidSpec();
        assertThat(specDefs.get(), is(empty()));
    }

    @Test
    public void retrievesOnlyValidSpecs() {
        ClasspathSpecDefinitions specDefs = new ClasspathSpecDefinitions(VALID_AND_INVALID_SPECS_PACKAGE, errorHandler);
        expectErrorForInvalidSpec();
        SpecDefinition validSpecDef = new ValidStubSpecDefinition_1();
        assertThat(specDefs.get(), is(asList(validSpecDef)));
    }

    @Test
    public void skipsAbstractSpecDefinitions() throws Exception {
        ClasspathSpecDefinitions specDefs = new ClasspathSpecDefinitions(ABSTRACT_SPECS_PACKAGE, errorHandler);
        assertThat(specDefs.get(), is(empty()));
    }

}
