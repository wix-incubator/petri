package com.wixpress.common.petri.e2e;

import com.wixpress.petri.test.TestBuilders;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FilterParametersConfigTest extends BaseTest {


    private static Path original;
    private static Path tempFile;

    @BeforeClass
    public static void startServers() throws Exception {
        original = Paths.get(BaseTest.SAMPLE_WEBAPP_PATH, "WEB-INF/filters.yaml");
        tempFile = Files.createTempFile("filters-temp", "yaml");
        copy(original, tempFile);
        new FilterParametersExtractorsConfigTestUtil().replaceConfig(original.toFile());
        BaseTest.startServers();
    }

    private static void copy(Path from, Path to) throws IOException {
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterClass
    public static void teardown() throws Exception {
        copy(tempFile, original);
    }

    @Test
    public void conductingAnExperimentWithGeoFilterWithCustomizedDataExtractor() throws IOException {

        addSpec("THE_KEY");

        fullPetriClient.insertExperiment(TestBuilders.experimentOnRegisteredWithFilter("THE_KEY").build());

        assertThat(petriClient.fetchActiveExperiments().size(), is(1));

        String nonTestGroupTestResult = sampleAppRunner.conductExperiment("THE_KEY", "FALLBACK_VALUE");
        assertThat(nonTestGroupTestResult, is("FALLBACK_VALUE"));

        String testResult = sampleAppRunner.conductExperimentWithGeoHeader("THE_KEY", "FALLBACK_VALUE");
        assertThat(testResult, is("a"));
    }
}
