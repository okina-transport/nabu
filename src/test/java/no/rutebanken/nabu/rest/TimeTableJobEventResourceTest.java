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
import no.rutebanken.nabu.domain.event.Event;
import no.rutebanken.nabu.domain.event.JobEvent;
import no.rutebanken.nabu.domain.event.JobState;
import no.rutebanken.nabu.event.EventService;
import no.rutebanken.nabu.provider.ProviderRepository;
import no.rutebanken.nabu.repository.EventRepository;
import no.rutebanken.nabu.rest.domain.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static no.rutebanken.nabu.domain.event.TimeTableAction.EXPORT;
import static no.rutebanken.nabu.domain.event.TimeTableAction.IMPORT;
import static org.assertj.core.api.Assertions.assertThat;

class TimeTableJobEventResourceTest extends BaseIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProviderRepository providerRepository;
    
    @Test
    void testGetStatusForProvider() {

        List<JobEvent> rawEvents = new ArrayList<>();

        Instant t0 = Instant.now().minusMillis(2000);

        // Job "b" -> OK
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename2", 2L, null, EXPORT.toString(), JobState.PENDING, "b", t0.plusMillis(4), "ost", null, false));
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename2", 2L, "1", EXPORT.toString(), JobState.STARTED, "b", t0.plusMillis(5), "pb", null, false));
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename2", 2L, "1", EXPORT.toString(), JobState.OK, "b", t0.plusMillis(6), "pb", null, false));

        // Job "a" -> FAILED
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename1", 2L, null, IMPORT.toString(), JobState.PENDING, "a", t0.plusMillis(1), "ost", null, false));
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename1", 2L, "2", IMPORT.toString(), JobState.STARTED, "a", t0.plusMillis(2), "ost", null, false));
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename1", 2L, "2", IMPORT.toString(), JobState.FAILED, "a", t0.plusMillis(3), "ost", null, false));


        List<JobStatus> listStatus = new TimeTableJobEventResource(eventService, providerRepository).convert(rawEvents, null, false);

        assertThat(listStatus).isNotEmpty().hasSize(2);

        JobStatus a = listStatus.getFirst();

        assertThat(a.getCorrelationId()).isEqualTo("a");
        assertThat(a.getEvents().getFirst().getAction()).isEqualTo(IMPORT.toString());
        assertThat(a.getEndStatus()).isEqualTo(JobStatus.State.FAILED);
        assertThat(a.getEvents()).hasSize(3);
        assertThat(a.getFirstEvent()).isEqualTo(Date.from(t0.plusMillis(1)));
        assertThat(a.getLastEvent()).isEqualTo(Date.from(t0.plusMillis(3)));

        assertThat(a.getEvents().get(1).chouetteJobId).isEqualTo(2);

        JobStatus b = listStatus.get(1);

        assertThat(b.getCorrelationId()).isEqualTo("b");
        assertThat(b.getEvents().getFirst().getAction()).isEqualTo(EXPORT.toString());
        assertThat(b.getEndStatus()).isEqualTo(JobStatus.State.OK);
        assertThat(b.getEvents()).hasSize(3);
        assertThat(b.getFirstEvent()).isEqualTo(Date.from(t0.plusMillis(4)));
        assertThat(b.getLastEvent()).isEqualTo(Date.from(t0.plusMillis(6)));

        assertThat(b.getEvents().getFirst().getReferential()).isEqualTo("ost");
        assertThat(b.getEvents().get(1).getChouetteJobId()).isEqualTo(1);
        assertThat(b.getEvents().get(1).getReferential()).isEqualTo("pb");
        assertThat(b.getEvents().get(2).getChouetteJobId()).isEqualTo(1);
        assertThat(b.getEvents().get(2).getReferential()).isEqualTo("pb");
    }

    @Test
    void testDelete() {

        // 1 orga avec 15 events d'import (keep 10) dont 7 > keepDays

        eventRepository.deleteAll();
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "3", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "4", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "7", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "8", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);

        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "9", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "10", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "11", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "12", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "12", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "14", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "15", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);

        eventService.clearByDaysOrNumberEvents(60, 10);
        List<Event> jobEvents1 = eventRepository.findAll();

        assertThat(jobEvents1).isNotEmpty().hasSize(12);


        // 1 orga avec 9 events (keep 10) dont 2 > du keepDays

        eventRepository.deleteAll();
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "3", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "4", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "7", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);

        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "8", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "9", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);

        eventService.clearByDaysOrNumberEvents(60, 10);
        List<Event> jobEvents2 = eventRepository.findAll();

        assertThat(jobEvents2).isNotEmpty().hasSize(9);

        // 1 orga avec 11 events (keep 10) all < keepDays

        eventRepository.deleteAll();
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "3", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "4", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "7", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "8", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "9", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "10", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "11", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);

        eventService.clearByDaysOrNumberEvents(60, 10);
        List<Event> jobEvents3 = eventRepository.findAll();

        assertThat(jobEvents3).isNotEmpty().hasSize(11);

        // 1 orga avec 8 events (keep 10) all < keepDays

        eventRepository.deleteAll();
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "3", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "4", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "7", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "8", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);


        eventService.clearByDaysOrNumberEvents(60, 10);
        List<Event> jobEvents4 = eventRepository.findAll();

        assertThat(jobEvents4).isNotEmpty().hasSize(8);


        // 3 orga avec 12 events (keep 10) all < keepDays

        eventRepository.deleteAll();
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "3", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "4", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "7", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(61, ChronoUnit.DAYS), "8", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);

        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "9", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "10", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "11", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "12", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "12", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "14", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential1", Instant.now().minus(59, ChronoUnit.DAYS), "15", JobState.OK, IMPORT.toString(), 1L, "fileName1", "externalId1", null);

        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(61, ChronoUnit.DAYS), "3", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(61, ChronoUnit.DAYS), "4", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(61, ChronoUnit.DAYS), "7", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(61, ChronoUnit.DAYS), "8", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);

        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(59, ChronoUnit.DAYS), "9", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(59, ChronoUnit.DAYS), "10", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(59, ChronoUnit.DAYS), "11", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(59, ChronoUnit.DAYS), "12", JobState.PENDING, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(59, ChronoUnit.DAYS), "12", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(59, ChronoUnit.DAYS), "14", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential2", Instant.now().minus(59, ChronoUnit.DAYS), "15", JobState.OK, IMPORT.toString(), 1L, "fileName1", null, null);

        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.PENDING, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(61, ChronoUnit.DAYS), "1", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(61, ChronoUnit.DAYS), "3", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(61, ChronoUnit.DAYS), "4", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.PENDING, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(61, ChronoUnit.DAYS), "5", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(61, ChronoUnit.DAYS), "7", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(61, ChronoUnit.DAYS), "8", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);

        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(59, ChronoUnit.DAYS), "9", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(59, ChronoUnit.DAYS), "10", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(59, ChronoUnit.DAYS), "11", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(59, ChronoUnit.DAYS), "12", JobState.PENDING, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(59, ChronoUnit.DAYS), "12", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(59, ChronoUnit.DAYS), "14", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);
        createJobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "referential3", Instant.now().minus(59, ChronoUnit.DAYS), "15", JobState.OK, IMPORT.toString(), 1L, null, "externalId3", null);


        eventService.clearByDaysOrNumberEvents(60, 10);
        List<Event> jobEvents5 = eventRepository.findAll();

        assertThat(jobEvents5).hasSize(36);
    }

    void createJobEvent(String domain, String referential, Instant instant, String correlationId, JobState state, String action, Long providerId, String name, String externalId, String type){
        JobEvent jobEvent = new JobEvent();
        jobEvent.setDomain(domain);
        jobEvent.setReferential(referential);
        jobEvent.setEventTime(instant);
        jobEvent.setCorrelationId(correlationId);
        jobEvent.setState(state);
        jobEvent.setAction(action);
        jobEvent.setProviderId(providerId);
        jobEvent.setName(name);
        jobEvent.setExternalId(externalId);
        jobEvent.setType(type);

        eventRepository.save(jobEvent);
    }


}