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
import no.rutebanken.nabu.repository.EventRepository;
import no.rutebanken.nabu.rest.domain.JobStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static no.rutebanken.nabu.domain.event.TimeTableAction.EXPORT;
import static no.rutebanken.nabu.domain.event.TimeTableAction.IMPORT;

public class TimeTableJobEventResourceTest extends BaseIntegrationTest {


    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;
    
    @Test
    public void testGetStatusForProvider() throws Exception {

        List<JobEvent> rawEvents = new ArrayList<>();

        Instant t0 = Instant.now().minusMillis(2000);

        // Job "b" -> OK
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename2", 2l, null, EXPORT.toString(), JobState.PENDING, "b", t0.plusMillis(4), "ost"));
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename2", 2l, "1", EXPORT.toString(), JobState.STARTED, "b", t0.plusMillis(5), "pb"));
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename2", 2l, "1", EXPORT.toString(), JobState.OK, "b", t0.plusMillis(6), "pb"));

        // Job "a" -> FAILED
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename1", 2l, null, IMPORT.toString(), JobState.PENDING, "a", t0.plusMillis(1), "ost"));
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename1", 2l, "2", IMPORT.toString(), JobState.STARTED, "a", t0.plusMillis(2), "ost"));
        rawEvents.add(new JobEvent(JobEvent.JobDomain.TIMETABLE.toString(), "filename1", 2l, "2", IMPORT.toString(), JobState.FAILED, "a", t0.plusMillis(3), "ost"));


        List<JobStatus> listStatus = new TimeTableJobEventResource().convert(rawEvents, null, false);

        Assert.assertNotNull(listStatus);
        Assert.assertEquals(2, listStatus.size());

        JobStatus a = listStatus.get(0);

        Assert.assertEquals("a", a.getCorrelationId());
        Assert.assertEquals(IMPORT.toString(), a.getEvents().get(0).action);
        Assert.assertEquals(JobStatus.State.FAILED, a.getEndStatus());
        Assert.assertEquals(3, a.getEvents().size());
        Assert.assertEquals(Date.from(t0.plusMillis(1)), a.getFirstEvent());
        Assert.assertEquals(Date.from(t0.plusMillis(3)), a.getLastEvent());

        Assert.assertEquals(Long.valueOf(2), a.getEvents().get(1).chouetteJobId);

        JobStatus b = listStatus.get(1);

        Assert.assertEquals("b", b.getCorrelationId());
        Assert.assertEquals(EXPORT.toString(), b.getEvents().get(0).action);
        Assert.assertEquals(JobStatus.State.OK, b.getEndStatus());
        Assert.assertEquals(3, b.getEvents().size());
        Assert.assertEquals(Date.from(t0.plusMillis(4)), b.getFirstEvent());
        Assert.assertEquals(Date.from(t0.plusMillis(6)), b.getLastEvent());

        Assert.assertEquals("ost", b.getEvents().get(0).referential);
        Assert.assertEquals(Long.valueOf(1), b.getEvents().get(1).chouetteJobId);
        Assert.assertEquals("pb", b.getEvents().get(1).referential);
        Assert.assertEquals(Long.valueOf(1), b.getEvents().get(2).chouetteJobId);
        Assert.assertEquals("pb", b.getEvents().get(2).referential);
    }

    @Test
    public void testDelete() {

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
        Assert.assertEquals(12, jobEvents1.size());


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
        Assert.assertEquals(9, jobEvents2.size());

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
        Assert.assertEquals(11, jobEvents3.size());

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
        Assert.assertEquals(8, jobEvents4.size());


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
        Assert.assertEquals(36, jobEvents5.size());

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
        jobEvent.setType(null);

        eventRepository.save(jobEvent);
    }


}