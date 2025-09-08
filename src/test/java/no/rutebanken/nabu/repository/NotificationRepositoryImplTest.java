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

package no.rutebanken.nabu.repository;

import com.google.common.collect.Sets;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import no.rutebanken.nabu.BaseIntegrationTest;
import no.rutebanken.nabu.domain.event.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationRepositoryImplTest extends BaseIntegrationTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void clearAllRemovesAllNotificationsForJobDomain() {
        JobEvent matchingEvent = JobEvent.builder().domain(JobEvent.JobDomain.TIMETABLE).providerId(2L).referential("ost").state(JobState.OK).name("file1.zip").externalId("1").action(TimeTableAction.IMPORT).correlationId("corr-id-1").eventTime(Instant.now()).build();
        JobEvent otherDomainEvent = JobEvent.builder().domain("otherDomain").providerId(2L).referential("ost").state(JobState.OK).name("file1.zip").externalId("1").action(TimeTableAction.IMPORT).correlationId("corr-id-1").eventTime(Instant.now()).build();
        CrudEvent crudEvent = CrudEvent.builder().entityType("type").entityClassifier("classifier").version(1L).externalId("1").action(TimeTableAction.IMPORT).correlationId("corr-id-1").eventTime(Instant.now()).build();
        eventRepository.saveAll(Sets.newHashSet(matchingEvent, otherDomainEvent, crudEvent));

        Notification matchingEventNotification = new Notification("user1", NotificationType.WEB, matchingEvent);
        Notification otherDomainEventNotification = new Notification("user1", NotificationType.WEB, otherDomainEvent);
        Notification crudEventNotification = new Notification("user1", NotificationType.WEB, crudEvent);
        notificationRepository.saveAll(Sets.newHashSet(matchingEventNotification, otherDomainEventNotification, crudEventNotification));


        notificationRepository.clearAll(matchingEvent.getDomain());
        entityManager.clear();

        Notification notification1 = notificationRepository.getReferenceById(matchingEventNotification.getPk());
        Notification notification2 = notificationRepository.getReferenceById(otherDomainEventNotification.getPk());
        Notification notification3 = notificationRepository.getReferenceById(crudEventNotification.getPk());
        assertThatThrownBy(notification1::toString).isInstanceOf(EntityNotFoundException.class);
        assertThat(notification2).isNotNull();
        assertThat(notification3).isNotNull();
    }

    @Test
    void clearRemovesAllNotificationsForJobDomainAndProvider() {
        JobEvent matchingEvent = JobEvent.builder().domain(JobEvent.JobDomain.TIMETABLE).providerId(2L).referential("ost").state(JobState.OK).name("file1.zip").externalId("1").action(TimeTableAction.IMPORT).correlationId("corr-id-1").eventTime(Instant.now()).build();
        JobEvent otherProviderEvent = JobEvent.builder().domain(JobEvent.JobDomain.TIMETABLE).providerId(666L).referential("ost").state(JobState.OK).name("file1.zip").externalId("1").action(TimeTableAction.IMPORT).correlationId("corr-id-1").eventTime(Instant.now()).build();
        JobEvent otherDomainEvent = JobEvent.builder().domain("otherDomain").providerId(2L).referential("ost").state(JobState.OK).name("file1.zip").externalId("1").action(TimeTableAction.IMPORT).correlationId("corr-id-1").eventTime(Instant.now()).build();
        CrudEvent crudEvent = CrudEvent.builder().entityType("type").entityClassifier("classifier").version(1L).externalId("1").action(TimeTableAction.IMPORT).correlationId("corr-id-1").eventTime(Instant.now()).build();
        eventRepository.saveAll(Sets.newHashSet(matchingEvent, otherProviderEvent, otherDomainEvent, crudEvent));

        Notification matchingEventNotification = new Notification("user1", NotificationType.WEB, matchingEvent);
        Notification otherProviderEventNotification = new Notification("user1", NotificationType.WEB, otherProviderEvent);
        Notification otherDomainEventNotification = new Notification("user1", NotificationType.WEB, otherDomainEvent);
        Notification crudEventNotification = new Notification("user1", NotificationType.WEB, crudEvent);
        notificationRepository.saveAll(Sets.newHashSet(matchingEventNotification, otherProviderEventNotification, otherDomainEventNotification, crudEventNotification));


        notificationRepository.clear(matchingEvent.getDomain(), matchingEvent.getProviderId());
        entityManager.clear();

        Notification notification1 = notificationRepository.getReferenceById(matchingEventNotification.getPk());
        Notification notification2 = notificationRepository.getReferenceById(otherProviderEventNotification.getPk());
        Notification notification3 = notificationRepository.getReferenceById(otherDomainEventNotification.getPk());
        Notification notification4 = notificationRepository.getReferenceById(crudEventNotification.getPk());
        assertThatThrownBy(notification1::toString).isInstanceOf(EntityNotFoundException.class);
        assertThat(notification2).isNotNull();
        assertThat(notification3).isNotNull();
        assertThat(notification4).isNotNull();
    }

    @Test
    void findByUserNameAndTypeAndStatus() {
        JobEvent event = JobEvent.builder().domain(JobEvent.JobDomain.TIMETABLE).providerId(2L).referential("ost").state(JobState.OK).name("file1.zip").externalId("1").action(TimeTableAction.IMPORT).correlationId("corr-id-1").eventTime(Instant.now()).build();
        eventRepository.saveAll(Sets.newHashSet(event));

        Notification matchingEventNotification = new Notification("user1", NotificationType.WEB, event);
        Notification otherUserName = new Notification("otherUser", NotificationType.WEB, event);
        Notification otherType = new Notification("user1", NotificationType.EMAIL, event);
        Notification otherStatus = new Notification("user1", NotificationType.WEB, event);
        otherStatus.setStatus(Notification.NotificationStatus.COMPLETE);
        notificationRepository.saveAll(Sets.newHashSet(matchingEventNotification, otherUserName, otherType, otherStatus));


        List<Notification> matchingNotifications = notificationRepository.findByUserNameAndTypeAndStatus("user1", NotificationType.WEB, Notification.NotificationStatus.READY);

        assertThat(matchingNotifications).hasSize(1).containsExactly(matchingEventNotification);
    }

    @Test
    void findByTypeAndStatus() {
        JobEvent event = JobEvent.builder().domain(JobEvent.JobDomain.TIMETABLE).providerId(2L).referential("ost").state(JobState.OK).name("file1.zip").externalId("1").action(TimeTableAction.IMPORT).correlationId("corr-id-1").eventTime(Instant.now()).build();
        eventRepository.saveAll(Sets.newHashSet(event));

        Notification matchingEventNotification = new Notification("user1", NotificationType.WEB, event);
        Notification otherType = new Notification("user1", NotificationType.EMAIL, event);
        Notification otherStatus = new Notification("user1", NotificationType.WEB, event);
        otherStatus.setStatus(Notification.NotificationStatus.COMPLETE);
        notificationRepository.saveAll(Sets.newHashSet(matchingEventNotification, otherType, otherStatus));


        List<Notification> matchingNotifications = notificationRepository.findByTypeAndStatus(NotificationType.WEB, Notification.NotificationStatus.READY);
        assertThat(matchingNotifications).hasSize(1).containsExactly(matchingEventNotification);
    }
}
