package com.wixpress.common.petri.e2e;

import com.wixpress.common.petri.SampleUserIdConverter;
import com.wixpress.petri.test.TestBuilders;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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
        new FilterParametersExtractorsConfigTestUtil().replaceConfigWithGeoHeaderAndUserIdConverter(original.toFile());
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
        String countyCode = "IL";
        fullPetriClient.insertExperiment(TestBuilders.experimentWithFirstWinningAndGeoFilter("THE_KEY", countyCode).build());
        sampleAppRunner.updateTheCacheNow();

        String testResult = sampleAppRunner.conductExperimentWithGeoHeader("THE_KEY", "FALLBACK_VALUE", countyCode);

        assertThat(testResult, is("a"));
    }

    @Test
    public void conductingAnExperimentWithCustomGeoConverter() throws IOException {

        addSpec("THE_KEY");
        UUID userGuid = UUID.randomUUID();
        fullPetriClient.insertExperiment(TestBuilders.experimentWithFirstWinningAndUserIdFilter("THE_KEY", userGuid).build());
        sampleAppRunner.updateTheCacheNow();

        String encodedUserId = SampleUserIdConverter.encode(userGuid);

        String testResult = sampleAppRunner.conductExperimentByCustomUserId("THE_KEY", "FALLBACK_VALUE", encodedUserId);

        assertThat(testResult, is("a"));
    }
}
