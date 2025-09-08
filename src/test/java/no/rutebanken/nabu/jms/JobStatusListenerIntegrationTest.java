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

package no.rutebanken.nabu.jms;

import no.rutebanken.nabu.BaseIntegrationTest;
import no.rutebanken.nabu.domain.SystemJobStatus;
import no.rutebanken.nabu.domain.event.JobEvent;
import no.rutebanken.nabu.domain.event.JobState;
import no.rutebanken.nabu.event.user.UserRepository;
import no.rutebanken.nabu.jms.dto.JobEventDTO;
import no.rutebanken.nabu.repository.EventRepository;
import no.rutebanken.nabu.repository.SystemJobStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JobStatusListenerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JobEventListener eventListener;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SystemJobStatusRepository systemJobStatusRepository;

    @MockitoBean
    private UserRepository userRepositoryMock;


    @BeforeEach
    void setUp() {
        when(userRepositoryMock.findAll()).thenReturn(new ArrayList<>());
    }


    @Test
    void jobEventUpdatesSystemJobStatus() {
        Instant now = Instant.now();
        JobEventDTO firstPendingEvent = createEvent(JobState.PENDING, now);
        eventListener.processMessage(toJson(firstPendingEvent));
        assertSystemJobStatus(firstPendingEvent);

        JobEventDTO firstFailedEvent = createEvent(JobState.FAILED, now.plusMillis(1000));
        eventListener.processMessage(toJson(firstFailedEvent));
        assertSystemJobStatus(firstFailedEvent);

        JobEventDTO secondFailedEvent = createEvent(JobState.FAILED, now.plusMillis(2000));
        eventListener.processMessage(toJson(secondFailedEvent));
        assertSystemJobStatus(secondFailedEvent);


        // Old started event should not affect state
        JobEventDTO secondPendingEvent = createEvent(JobState.PENDING, now.minusMillis(1000));
        eventListener.processMessage(toJson(secondPendingEvent));
        assertSystemJobStatus(firstPendingEvent);

        assertThat(eventRepository.findAll()).hasSize(4);

        JobEvent queryEvent = JobEvent.builder().domain(firstPendingEvent.getDomain()).build();
        queryEvent.setRegisteredTime(null);

        assertThat(eventRepository.findAll(Example.of(queryEvent))).hasSize(4);

    }

    protected void assertSystemJobStatus(JobEventDTO jobEvent) {
        SystemJobStatus systemJobStatus = systemJobStatusRepository.findByJobDomainAndActionAndState(jobEvent.getDomain(),
                jobEvent.getAction(), jobEvent.getState());

        assertThat(jobEvent.getEventTime()).isEqualTo(systemJobStatus.getLastStatusTime());
        assertThat(jobEvent.getState()).isEqualTo(systemJobStatus.getState());
    }

    protected JobEventDTO createEvent(JobState state, Instant time) {
        JobEventDTO jobEvent = new JobEventDTO();
        jobEvent.setEventTime(time);
        jobEvent.setState(state);
        jobEvent.setAction("action");
        jobEvent.setDomain("JobStatusListener");
        return jobEvent;
    }


}
