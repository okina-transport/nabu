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

package no.rutebanken.nabu.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import no.rutebanken.nabu.domain.event.JobEvent;
import no.rutebanken.nabu.domain.event.JobState;

import java.time.Instant;

/**
 * JobEvent model for API usage.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApiJobEvent {

    private Instant registeredTime;

    private Instant eventTime;

    private String correlationId;

    private String domain;

    private String action;

    private JobState state;

    private String externalId;

    private Long providerId;

    private String referential;

    private String name;

    public static ApiJobEvent fromJobEvent(JobEvent jobEvent) {
        ApiJobEvent apiJobEvent = new ApiJobEvent();
        apiJobEvent.eventTime = jobEvent.getEventTime();
        apiJobEvent.registeredTime = jobEvent.getRegisteredTime();
        apiJobEvent.action = jobEvent.getAction();
        apiJobEvent.externalId = jobEvent.getExternalId();
        apiJobEvent.name = jobEvent.getName();
        apiJobEvent.providerId = jobEvent.getProviderId();
        apiJobEvent.referential = jobEvent.getReferential();
        apiJobEvent.state = jobEvent.getState();
        apiJobEvent.domain = jobEvent.getDomain();
        return apiJobEvent;
    }
}

