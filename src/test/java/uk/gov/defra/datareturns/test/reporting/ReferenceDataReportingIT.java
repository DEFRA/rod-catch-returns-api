package uk.gov.defra.datareturns.test.reporting;

import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.species.SpeciesRepository;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;
import uk.gov.defra.datareturns.util.CsvUtil;

import javax.inject.Inject;

import static uk.gov.defra.datareturns.test.reporting.ReportingIT.readCsvFromResponse;
import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.getEntity;

/**
 * Integration tests for reporting reference data feeds
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@WithAdminUser
@Slf4j
public class ReferenceDataReportingIT {
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private SpeciesRepository speciesRepository;
    @Inject
    private MethodRepository methodRepository;

    @Test
    public void testLocationReferenceData() {
        final ValidatableResponse response = getEntity("/reporting/reference/locations");
        final CsvUtil.CsvReadResult<Object[]> result = readCsvFromResponse(response);
        Assertions.assertThat(result.getHeaders()).containsExactly("ID", "River", "Catchment", "Region", "Gate");
        // Expect 1 location per available river (the hierarchy of region->catchment->river being flattened)
        Assertions.assertThat(result.getRows().size()).isEqualTo(riverRepository.count());
    }

    @Test
    public void testSpeciesReferenceData() {
        final ValidatableResponse response = getEntity("/reporting/reference/species");
        final CsvUtil.CsvReadResult<Object[]> result = readCsvFromResponse(response);
        Assertions.assertThat(result.getHeaders()).containsExactly("ID", "Species", "Small Catch Mass");
        Assertions.assertThat(result.getRows().size()).isEqualTo(speciesRepository.count());
    }

    @Test
    public void testMethodReferenceData() {
        final ValidatableResponse response = getEntity("/reporting/reference/methods");
        final CsvUtil.CsvReadResult<Object[]> result = readCsvFromResponse(response);
        Assertions.assertThat(result.getHeaders()).containsExactly("ID", "Method");
        Assertions.assertThat(result.getRows().size()).isEqualTo(methodRepository.count());
    }
}
