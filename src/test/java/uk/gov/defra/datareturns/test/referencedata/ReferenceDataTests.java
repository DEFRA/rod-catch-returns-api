package uk.gov.defra.datareturns.test.referencedata;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.catchments.Catchment;
import uk.gov.defra.datareturns.data.model.catchments.CatchmentRepository;
import uk.gov.defra.datareturns.data.model.regions.Region;
import uk.gov.defra.datareturns.data.model.regions.RegionRepository;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Integration tests catch object property validation
 */
@RunWith(SpringRunner.class)
@ApiContextTest
@WithAdminUser
@Slf4j
public class ReferenceDataTests {
    @Inject
    private RegionRepository regionRepository;
    @Inject
    private CatchmentRepository catchmentRepository;

    @Test
    public void testRegionCatchments() {
        final Region region = new Region();
        region.setName("Test region");

        final Catchment c1 = new Catchment();
        c1.setName("Test catchment 1");
        c1.setRegion(region);

        final Catchment c2 = new Catchment();
        c2.setName("Test catchment 2");
        c2.setRegion(region);
        region.setCatchments(Arrays.asList(c1, c2));

        final Long id = regionRepository.saveAndFlush(region).getId();
        final Region result = regionRepository.findById(id).orElse(null);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCatchments()).hasSize(2);
        Assertions.assertThat(result.getCatchments().stream().map(Catchment::getRegion).collect(Collectors.toList())).containsExactly(region, region);
    }

    @Test
    public void testCatchmentRivers() {
        final Catchment catchment = new Catchment();
        catchment.setName("Test catchment");
        catchment.setRegion(regionRepository.getOne(1L));

        final River r1 = new River();
        r1.setName("Test river 1");
        r1.setCatchment(catchment);

        final River r2 = new River();
        r2.setName("Test river 2");
        r2.setCatchment(catchment);
        catchment.setRivers(Arrays.asList(r1, r2));

        final Long id = catchmentRepository.saveAndFlush(catchment).getId();
        final Catchment result = catchmentRepository.findById(id).orElse(null);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getRivers()).hasSize(2);
        Assertions.assertThat(result.getRivers().stream().map(River::getCatchment).collect(Collectors.toList()))
                .containsExactly(catchment, catchment);
    }


}
