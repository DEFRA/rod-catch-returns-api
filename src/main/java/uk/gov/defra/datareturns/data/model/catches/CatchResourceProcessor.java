package uk.gov.defra.datareturns.data.model.catches;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import uk.gov.defra.datareturns.data.model.activities.Activity;

import javax.inject.Inject;

@Component
@ConditionalOnWebApplication
public class CatchResourceProcessor implements RepresentationModelProcessor<EntityModel<Catch>> {
    @Inject
    private RepositoryRestMvcConfiguration configuration;

    @Override
    public EntityModel<Catch> process(final EntityModel<Catch> resource) {
        final LinkBuilder link = configuration.entityLinks().linkForItemResource(Activity.class,
                resource.getContent().getActivity().getId());
        resource.add(link.withRel("activityEntity"));
        return resource;
    }
}
