/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.rutebanken.nabu.event.filter;

import com.google.common.collect.Sets;
import no.rutebanken.nabu.domain.event.JobEvent;
import no.rutebanken.nabu.domain.event.JobState;
import no.rutebanken.nabu.event.user.dto.organisation.OrganisationDTO;
import no.rutebanken.nabu.event.user.dto.user.EventFilterDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobEventMatcherTest {

    @Test
    void eventMatchingFilterWithoutOrganisationSet() {
        EventFilterDTO filter = testFilter();

        assertThat(new JobEventMatcher(filter).matches(matchingJobEvent(filter))).isTrue();
    }

    @Test
    void eventMatchingFilterWithOrganisationSet() {
        EventFilterDTO filter = testFilter(organisationDTO("KOK"));
        JobEvent orgSpaceEvent = matchingJobEvent(filter);
        assertThat(new JobEventMatcher(filter).matches(orgSpaceEvent)).isTrue();

        JobEvent rbSpaceEvent = matchingJobEvent(filter);
        rbSpaceEvent.setReferential("rb_kok");
        assertThat(new JobEventMatcher(filter).matches(rbSpaceEvent)).isTrue();
    }

    @Test
    void eventWithoutRefNotNotMatchingFilterWithOrganisationSet() {
        EventFilterDTO filter = testFilter(organisationDTO("KOK"));
        JobEvent event = matchingJobEvent(filter);
        event.setReferential(null);

        assertThat(new JobEventMatcher(filter).matches(event)).isFalse();
    }


    @Test
    void eventWithOtherRefNotNotMatchingFilterWithOrganisationSet() {
        EventFilterDTO filter = testFilter(organisationDTO("KOK"));
        JobEvent event = matchingJobEvent(filter);
        event.setReferential("otherRef");

        assertThat(new JobEventMatcher(filter).matches(event)).isFalse();
    }


    @Test
    void eventWithDifferentJobDomainNotMatchingFilterWithoutOrganisationSet() {
        EventFilterDTO filter = testFilter();
        JobEvent event = matchingJobEvent(filter);
        event.setDomain("otherDomain");

        assertThat(new JobEventMatcher(filter).matches(event)).isFalse();
    }

    @Test
    void eventWithDifferentActionNotMatchingFilterWithoutOrganisationSet() {
        EventFilterDTO filter = testFilter();
        JobEvent event = matchingJobEvent(filter);
        event.setAction("otherAction");

        assertThat(new JobEventMatcher(filter).matches(event)).isFalse();
    }

    @Test
    void eventWithDifferentStateNotMatchingFilterWithoutOrganisationSet() {
        EventFilterDTO filter = testFilter();
        JobEvent event = matchingJobEvent(filter);
        event.setState(JobState.PENDING);

        assertThat(new JobEventMatcher(filter).matches(event)).isFalse();
    }

    @Test
    void allStatesMatchingWildcardAction() {
        EventFilterDTO filter = testFilter();
        filter.setActions(Sets.newHashSet(EventMatcher.ALL_TYPES));
        JobEvent event = matchingJobEvent(filter);

        assertThat(new JobEventMatcher(filter).matches(event)).isTrue();

        event.setAction("randomAction");
        assertThat(new JobEventMatcher(filter).matches(event)).isTrue();
    }

    private JobEvent matchingJobEvent(EventFilterDTO filter) {
        String ref = filter.getOrganisation() == null ? null : filter.getOrganisation().getPrivateCode().toLowerCase();
        return JobEvent.builder()
                .domain(filter.getJobDomain())
                .referential(ref)
                .state(filter.getStates().iterator().next())
                .action(filter.getActions().iterator().next())
                .build();
    }

    private EventFilterDTO testFilter() {
        return testFilter(null);
    }

    private EventFilterDTO testFilter(OrganisationDTO organisationDTO) {
        EventFilterDTO filter = new EventFilterDTO();
        filter.setJobDomain(JobEvent.JobDomain.TIMETABLE.toString());
        filter.setActions(Sets.newHashSet("testAction"));
        filter.setOrganisation(organisationDTO);
        filter.setStates(Sets.newHashSet(JobState.FAILED));
        return filter;
    }

    private OrganisationDTO organisationDTO(String privateCode) {
        OrganisationDTO org = new OrganisationDTO();
        org.setPrivateCode(privateCode);
        return org;
    }
}
