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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import no.rutebanken.nabu.domain.event.ActionType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static no.rutebanken.nabu.domain.event.TimeTableAction.DATASPACE_TRANSFER;
import static no.rutebanken.nabu.domain.event.TimeTableAction.EXPORT;
import static no.rutebanken.nabu.domain.event.TimeTableAction.EXPORT_CONCERTO;
import static no.rutebanken.nabu.domain.event.TimeTableAction.EXPORT_NETEX;
import static no.rutebanken.nabu.domain.event.TimeTableAction.FILE_CLASSIFICATION;
import static no.rutebanken.nabu.domain.event.TimeTableAction.FILE_TRANSFER;
import static no.rutebanken.nabu.domain.event.TimeTableAction.IMPORT;
import static no.rutebanken.nabu.domain.event.TimeTableAction.VALIDATION_LEVEL_1;
import static no.rutebanken.nabu.domain.event.TimeTableAction.VALIDATION_LEVEL_2;

@JsonRootName("jobs")
public class JobStatus {

    public enum State {
        PENDING, STARTED, TIMEOUT, FAILED, OK, DUPLICATE, CANCELLED
    }

    @JsonProperty("events")
    private List<JobStatusEvent> events = new ArrayList<JobStatusEvent>();

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("firstEvent")
    private Date firstEvent;

    @JsonProperty("lastEvent")
    private Date lastEvent;

    @JsonProperty("durationMillis")
    private Long durationMillis;

    @JsonProperty("endState")
    private State endStatus;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("providerId")
    private Long providerId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type")
    private String type;

    public List<JobStatusEvent> getEvents() {
        return events;
    }

    public void addEvent(JobStatusEvent event) {
        events.add(event);
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Date getFirstEvent() {
        return firstEvent;
    }

    public void setFirstEvent(Date firstEvent) {
        this.firstEvent = firstEvent;
    }

    public Date getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(Date lastEvent) {
        this.lastEvent = lastEvent;
    }

    public State getEndStatus() {
        return endStatus;
    }

    public void setEndStatus(State endStatus) {
        this.endStatus = endStatus;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(Long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    public ActionType getActionType() {

        if (events.stream().anyMatch(event -> StringUtils.equals(event.action, FILE_TRANSFER.toString())) ||
                events.stream().anyMatch(event -> StringUtils.equals(event.action, FILE_CLASSIFICATION.toString())) ||
                events.stream().anyMatch(event -> StringUtils.equals(event.action, IMPORT.toString()))) {
            return ActionType.IMPORTER;
        } else if (
                (
                        !events.stream().anyMatch(event -> StringUtils.equals(event.action, FILE_TRANSFER.toString())) &&
                        !events.stream().anyMatch(event -> StringUtils.equals(event.action, FILE_CLASSIFICATION.toString())) &&
                        !events.stream().anyMatch(event -> StringUtils.equals(event.action, IMPORT.toString())) &&
                        !events.stream().anyMatch(event -> StringUtils.equals(event.action, VALIDATION_LEVEL_1.toString())) &&
                        !events.stream().anyMatch(event -> StringUtils.equals(event.action, DATASPACE_TRANSFER.toString())) &&
                        !events.stream().anyMatch(event -> StringUtils.equals(event.action, VALIDATION_LEVEL_2.toString()))
                ) &&
                        events.stream().anyMatch(event -> StringUtils.equals(event.action, EXPORT_NETEX.toString())) ||
                        events.stream().anyMatch(event -> StringUtils.equals(event.action, EXPORT.toString())) ||
                        events.stream().anyMatch(event -> StringUtils.equals(event.action, EXPORT_CONCERTO.toString()))
        ) {
            return ActionType.EXPORTER;
        }
        return ActionType.VALIDATOR;
    }
}
