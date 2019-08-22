package uk.gov.defra.datareturns.data.model.smallcatches;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import uk.gov.defra.datareturns.data.model.activities.Activity;

import javax.inject.Inject;

@Component
@ConditionalOnWebApplication
public class SmallCatchResourceProcessor implements ResourceProcessor<Resource<SmallCatch>> {
    @Inject
    private RepositoryRestMvcConfiguration configuration;

    @Override
    public Resource<SmallCatch> process(final Resource<SmallCatch> resource) {
        final LinkBuilder link = configuration.entityLinks().linkForSingleResource(Activity.class, resource.getContent().getActivity().getId());
        resource.add(link.withRel("activityEntity"));
        return resource;
    }
}
