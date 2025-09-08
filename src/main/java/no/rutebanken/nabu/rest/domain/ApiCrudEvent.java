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
import no.rutebanken.nabu.domain.event.CrudEvent;

import java.time.Instant;

/**
 * CrudEvent model for API usage.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApiCrudEvent {
    private Instant registeredTime;

    private Instant eventTime;

    private String entityType;

    private String entityClassifier;

    private String action;

    private String externalId;

    private Long version;

    private String name;

    private String changeType;

    private String oldValue;

    private String newValue;

    private String comment;

    private String username;

    public static ApiCrudEvent fromCrudEvent(CrudEvent crudEvent) {
        ApiCrudEvent apiCrudEvent = new ApiCrudEvent();
        apiCrudEvent.eventTime = crudEvent.getEventTime();
        apiCrudEvent.registeredTime = crudEvent.getRegisteredTime();
        apiCrudEvent.entityType = crudEvent.getEntityType();
        apiCrudEvent.entityClassifier = crudEvent.getEntityClassifier();
        apiCrudEvent.action = crudEvent.getAction();
        apiCrudEvent.changeType = crudEvent.getChangeType();
        apiCrudEvent.externalId = crudEvent.getExternalId();
        apiCrudEvent.version = crudEvent.getVersion();
        apiCrudEvent.name = crudEvent.getName();
        apiCrudEvent.oldValue = crudEvent.getOldValue();
        apiCrudEvent.newValue = crudEvent.getNewValue();
        apiCrudEvent.comment = crudEvent.getComment();
        apiCrudEvent.username = crudEvent.getUsername();
        return apiCrudEvent;
    }

}
