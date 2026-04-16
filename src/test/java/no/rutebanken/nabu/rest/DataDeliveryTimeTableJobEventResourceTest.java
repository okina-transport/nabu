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


import no.rutebanken.nabu.domain.event.JobEvent;
import no.rutebanken.nabu.domain.event.JobState;
import no.rutebanken.nabu.domain.event.TimeTableAction;
import no.rutebanken.nabu.repository.EventRepository;
import no.rutebanken.nabu.rest.domain.DataDeliveryStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DataDeliveryTimeTableJobEventResourceTest {

    private static final String JOB_DOMAIN = JobEvent.JobDomain.TIMETABLE.toString();

    @Mock
    private EventRepository eventRepository;

    @Test
    void testMapToDataDeliveryJobEventEmptyList() {
        DataDeliveryStatus dataDeliveryJobEvent = new LatestUploadResource(eventRepository).toDataDeliveryStatus(new ArrayList<>());
        assertThat(dataDeliveryJobEvent.getDate()).isNull();
        assertThat(dataDeliveryJobEvent.getState()).isNull();
    }

    @Test
    void testMapToDataDeliveryJobEventSuccess() {
        JobEvent s1 = new JobEvent(JOB_DOMAIN, "file1.zip", 3L, "1", TimeTableAction.FILE_TRANSFER.toString(), JobState.OK, "corr-id-1", Instant.now(), "ost", null, false);
        JobEvent s2 = new JobEvent(JOB_DOMAIN, "file1.zip", 3L, "1", TimeTableAction.BUILD_GRAPH.toString(), JobState.OK, "corr-id-1", Instant.now().plusMillis(1000), "ost", null, false);
        JobEvent s3 = new JobEvent(JOB_DOMAIN, "file1.zip", 3L, "1", TimeTableAction.EXPORT_NETEX.toString(), JobState.PENDING, "corr-id-1", Instant.now().plusMillis(2000), "ost", null, false);
        DataDeliveryStatus dataDeliveryJobEvent = new LatestUploadResource(eventRepository).toDataDeliveryStatus(Arrays.asList(s1, s2, s3));

        assertThat(dataDeliveryJobEvent.getDate().toInstant().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(s1.getEventTime().truncatedTo(ChronoUnit.MILLIS));
        assertThat(dataDeliveryJobEvent.getState()).isEqualTo(DataDeliveryStatus.State.OK);
    }

    @Test
    void testMapToDataDeliveryJobEventInProgress() {
        JobEvent s1 = new JobEvent(JOB_DOMAIN, "file1.zip", 3L, "1", TimeTableAction.FILE_TRANSFER.toString(), JobState.OK, "corr-id-1", Instant.now(), "ost", null, false);
        JobEvent s2 = new JobEvent(JOB_DOMAIN, "file1.zip", 3L, "1", TimeTableAction.BUILD_GRAPH.toString(), JobState.STARTED, "corr-id-1", Instant.now().plusMillis(1000), "ost", null, false);
        JobEvent s3 = new JobEvent(JOB_DOMAIN, "file1.zip", 3L, "1", TimeTableAction.EXPORT_NETEX.toString(), JobState.OK, "corr-id-1", Instant.now().plusMillis(2000), "ost", null, false);
        DataDeliveryStatus dataDeliveryJobEvent = new LatestUploadResource(eventRepository).toDataDeliveryStatus(Arrays.asList(s1, s2, s3));

        assertThat(dataDeliveryJobEvent.getDate().toInstant().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(s1.getEventTime().truncatedTo(ChronoUnit.MILLIS));
        assertThat(dataDeliveryJobEvent.getState()).isEqualTo(DataDeliveryStatus.State.IN_PROGRESS);
    }

    @Test
    void testMapToDataDeliveryJobEventFailed() {
        JobEvent s1 = new JobEvent(JOB_DOMAIN, "file1.zip", 3L, "1", TimeTableAction.FILE_TRANSFER.toString(), JobState.OK, "corr-id-1", Instant.now(), "ost", null, false);
        JobEvent s2 = new JobEvent(JOB_DOMAIN, "file1.zip", 3L, "1", TimeTableAction.FILE_CLASSIFICATION.toString(), JobState.FAILED, "corr-id-1", Instant.now().plusMillis(1000), "ost", null, false);
        DataDeliveryStatus dataDeliveryJobEvent = new LatestUploadResource(eventRepository).toDataDeliveryStatus(Arrays.asList(s1, s2));

        assertThat(dataDeliveryJobEvent.getDate().toInstant().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(s1.getEventTime().truncatedTo(ChronoUnit.MILLIS));
        assertThat(dataDeliveryJobEvent.getState()).isEqualTo(DataDeliveryStatus.State.FAILED);
    }
}
