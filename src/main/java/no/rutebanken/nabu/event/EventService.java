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

package no.rutebanken.nabu.event;

import no.rutebanken.nabu.domain.event.Event;
import no.rutebanken.nabu.domain.event.JobEvent;
import no.rutebanken.nabu.domain.event.JobState;
import no.rutebanken.nabu.domain.event.TimeTableAction;
import no.rutebanken.nabu.repository.EventRepository;
import no.rutebanken.nabu.repository.NotificationRepository;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private List<EventHandler> eventHandlers;


    public List<JobEvent> findTimetableJobEvents(List<Long> providerIds, Instant from, Instant to, List<String> actions,
                                                        List<JobState> states, List<String> externalIds, List<String> fileNames) {
        return eventRepository.findTimetableJobEvents(providerIds, from, to, actions, states, externalIds, fileNames);
    }

    public List<JobEvent> findExports(String exportType,List<Long> providerIds, int maxExportResults) {
        return eventRepository.getExports(exportType, providerIds,maxExportResults);
    }



    public void addEvent(Event event) {
        eventRepository.save(event);

        eventHandlers.forEach(handler -> handler.onEvent(event));
    }


    public void clearAll(String domain) {
        notificationRepository.clearAll(domain);
        eventRepository.clearAll(domain);
    }


    public void clear(String domain, Long providerId) {
        notificationRepository.clear(domain, providerId);
        eventRepository.clear(domain, providerId);
    }

    public void clearByDaysOrNumberEvents(int keepDays, int keepJobsPerReferential) {
        Map<String, List<JobEvent>> jobEventsMap = new HashMap<>();
        jobEventsMap.put(TimeTableAction.FILE_ANALYZE.toString(), eventRepository.getJobEventsByActionAndType(TimeTableAction.FILE_ANALYZE.toString(), null));
        jobEventsMap.put(TimeTableAction.IMPORT.toString(), eventRepository.getJobEventsByActionAndType(TimeTableAction.IMPORT.toString(), null));
        jobEventsMap.put(TimeTableAction.VALIDATION_LEVEL_1.toString(), eventRepository.getJobEventsByActionAndType(TimeTableAction.VALIDATION_LEVEL_1.toString(), null));
        jobEventsMap.put(TimeTableAction.DATASPACE_TRANSFER.toString(), eventRepository.getJobEventsByActionAndType(TimeTableAction.DATASPACE_TRANSFER.toString(), null));
        jobEventsMap.put(TimeTableAction.VALIDATION_LEVEL_2.toString(), eventRepository.getJobEventsByActionAndType(TimeTableAction.VALIDATION_LEVEL_2.toString(), null));
        jobEventsMap.put(TimeTableAction.EXPORT.toString(), eventRepository.getJobEventsByActionAndType(TimeTableAction.EXPORT.toString(), "gtfs"));
        jobEventsMap.put(TimeTableAction.EXPORT.toString(), eventRepository.getJobEventsByActionAndType(TimeTableAction.EXPORT.toString(), "neptune"));
        jobEventsMap.put(TimeTableAction.EXPORT_NETEX.toString(), eventRepository.getJobEventsByActionAndType(TimeTableAction.EXPORT_NETEX.toString(), null));

        List<Long> idsEventToDelete = new ArrayList<>();

        for (List<JobEvent> jobs : jobEventsMap.values()) {
            getOldJobEvents(jobs, keepDays, keepJobsPerReferential, idsEventToDelete);
        }

        eventRepository.deleteAllByPk(idsEventToDelete);

        logger.info("Removed old events. Count: " + idsEventToDelete.size());
    }

    private void getOldJobEvents(List<JobEvent> jobs, int keepDays, int keepJobsPerReferential, List<Long> idsEventToDelete) {
        Map<String, Map<String, List<JobEvent>>> jobsEventMapGrouping = jobs
                .stream()
                .collect(Collectors.groupingBy(JobEvent::getReferential,
                        Collectors.groupingBy(JobEvent::getCorrelationId)));


        for (Map<String, List<JobEvent>> jobsEventMap : jobsEventMapGrouping.values()) {
            if (jobsEventMap.values().size() > keepJobsPerReferential) {
                int numberJobToDeleteGroupingByCorrelationId = jobsEventMap.values().size() - keepJobsPerReferential;
                for(List<JobEvent> jobEvents : jobsEventMap.values()){
                    LocalDateTime ageLimit = LocalDateTime.now().minusDays(keepDays);
                    List<Long> deleteJobsEvent = jobEvents.stream()
                            .filter(job -> job.getEventTime() != null && job.getEventTime().isBefore(ageLimit.toDate().toInstant()))
                            .map(JobEvent::getPk)
                            .collect(Collectors.toList());

                    if (!deleteJobsEvent.isEmpty()) {
                        idsEventToDelete.addAll(deleteJobsEvent);
                        numberJobToDeleteGroupingByCorrelationId--;
                    }
                    if(numberJobToDeleteGroupingByCorrelationId == 0){
                        break;
                    }
                }
            }
        }
    }
}
