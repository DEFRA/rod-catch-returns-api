package uk.gov.defra.datareturns.data.model.catches;

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
public class CatchResourceProcessor implements ResourceProcessor<Resource<Catch>> {
    @Inject
    private RepositoryRestMvcConfiguration configuration;

    @Override
    public Resource<Catch> process(final Resource<Catch> resource) {
        final LinkBuilder link = configuration.entityLinks().linkForSingleResource(Activity.class, resource.getContent().getActivity().getId());
        resource.add(link.withRel("activityEntity"));
        return resource;
    }
}
