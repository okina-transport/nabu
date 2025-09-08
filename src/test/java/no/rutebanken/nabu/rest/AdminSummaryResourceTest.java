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

package no.rutebanken.nabu.rest;


import no.rutebanken.nabu.BaseIntegrationTest;
import no.rutebanken.nabu.domain.SystemJobStatus;
import no.rutebanken.nabu.domain.event.JobState;
import no.rutebanken.nabu.rest.domain.SystemStatusAggregation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminSummaryResourceTest extends BaseIntegrationTest {

    @Autowired
    private AdminSummaryResource resource;

	@Test
	void testConvertToSystemStatusAggregationEmptyCollection() {
		Collection<SystemStatusAggregation> aggregations = resource.convertToSystemStatusAggregation(new ArrayList<>());
        assertThat(aggregations).isEmpty();
	}

	@Test
	void testConvertToSystemStatusAggregation() {
		List<SystemJobStatus> statusList = new ArrayList<>();
		
		Instant now= Instant.now();
		
		statusList.add(new SystemJobStatus("job1", "", JobState.STARTED, now));
		statusList.add(new SystemJobStatus("job1", "", JobState.STARTED,now.plusMillis(2)));
		statusList.add(new SystemJobStatus("job1", "", JobState.STARTED, now.plusMillis(5)));
		statusList.add(new SystemJobStatus("job1", "", JobState.FAILED, now.plusMillis(3)));
		statusList.add(new SystemJobStatus("job2", "", JobState.OK, now.plusMillis(1)));

		Collection<SystemStatusAggregation> aggregations = resource.convertToSystemStatusAggregation(statusList);
        assertThat(aggregations).hasSize(2);

		SystemStatusAggregation agg1 = findAgg(aggregations, "job1");
        assertThat(agg1).isNotNull();
        assertThat(agg1.currentState).isEqualTo(JobState.STARTED);
        assertThat(agg1.currentStateDate).isEqualTo(Date.from(now.plusMillis(5)));
        assertThat(agg1.latestDatePerState).containsEntry(JobState.FAILED, Date.from(now.plusMillis(3)));


		SystemStatusAggregation agg2 = findAgg(aggregations, "job2");
        assertThat(agg2).isNotNull();
        assertThat(agg2.currentState).isEqualTo(JobState.OK);
        assertThat(agg2.currentStateDate).isEqualTo(Date.from(now.plusMillis(1)));
        assertThat(agg2.latestDatePerState).containsEntry(JobState.OK, Date.from(now.plusMillis(1)));

	}

	private SystemStatusAggregation findAgg(Collection<SystemStatusAggregation> aggregations, String entity) {
		for (SystemStatusAggregation agg : aggregations) {
			if (entity.equals(agg.jobDomain)) {
				return agg;
			}
		}
		return null;
	}
}
