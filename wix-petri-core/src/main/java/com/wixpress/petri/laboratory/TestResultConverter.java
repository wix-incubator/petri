package com.wixpress.petri.laboratory;


/**
 * The {@code TestResultConverter} interface defines the API for converting the result of a test to a specific type.
 * A "test result" is the outcome of conducting an experiment, and its type, unless an implementation of this
 * interface is used, is {@link String}; for converting this type to a specific type, an implementation of this
 * interface should be used.
 * <p>
 * This interface defines a single method, {@link #convert(String)}, that accepts the test result as a {@link String},
 * and responsible for converting it to the desired type.
 * If the call to the {@link Laboratory}'s {@code #conductExperiment} method includes an implementation of this
 * interface, the {@link #convert(String)} method is invoked on that instance as an experiment is conducted, and a
 * result is at hand.
 * If no converter is specified, the default one, {@link com.wixpress.petri.laboratory.converters.StringConverter}, is
 * used.
 * </p>
 * <p>
 * Usage example:
 * </p>
 * <pre>
 *     public class MyClass {
 *        private final Laboratory lab;
 *
 *        public MyClass(final Laboratory lab) {
 *           this.lab = lab;
 *        }
 *
 *        public void foo() {
 *           final boolean testResult = lab.conductExperiment(MySpec.class, true, new TestResultConverter() {
 *              public boolean convert(final String value) {
 *                 return "Yes".equalsIgnoreCase(value) ? true : false;
 *              }
 *           });
 *
 *           ...
 *        }
 *     }
 * </pre>
 *
 * Several implementation of this interface are provided, as as
 * {@link com.wixpress.petri.laboratory.converters.IntegerConverter} and
 * {@link com.wixpress.petri.laboratory.converters.StringConverter}.
 *
 * @see Laboratory#conductExperiment(Class, Object, TestResultConverter)
 * @see com.wixpress.petri.laboratory.converters.IntegerConverter
 * @see com.wixpress.petri.laboratory.converters.StringConverter
 */
public interface TestResultConverter<T> {
    T convert(String value);
}
