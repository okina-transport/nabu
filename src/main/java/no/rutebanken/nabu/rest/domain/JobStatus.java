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
import lombok.Getter;
import lombok.Setter;
import no.rutebanken.nabu.domain.event.ActionType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static no.rutebanken.nabu.domain.event.TimeTableAction.*;

@JsonRootName("jobs")
@Getter
@Setter
public class JobStatus {

    public enum State {
        PENDING, STARTED, TIMEOUT, FAILED, OK, DUPLICATE, CANCELLED
    }

    @JsonProperty("events")
    private List<JobStatusEvent> events = new ArrayList<>();

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

    @JsonProperty("flexible")
    private Boolean flexible;

    public void addEvent(JobStatusEvent event) {
        events.add(event);
    }

    @JsonIgnore
    public ActionType getActionType() {

        if (events.stream().anyMatch(event -> StringUtils.equals(event.action, FILE_TRANSFER.toString())) ||
                events.stream().anyMatch(event -> StringUtils.equals(event.action, FILE_CLASSIFICATION.toString())) ||
                events.stream().anyMatch(event -> StringUtils.equals(event.action, IMPORT.toString()))) {
            return ActionType.IMPORTER;
        } else if (
                        events.stream().anyMatch(event -> StringUtils.equals(event.action, EXPORT_NETEX.toString())) ||
                        events.stream().anyMatch(event -> StringUtils.equals(event.action, EXPORT.toString())) ||
                        events.stream().anyMatch(event -> StringUtils.equals(event.action, EXPORT_CONCERTO.toString()))
        ) {
            return ActionType.EXPORTER;
        }
        return ActionType.VALIDATOR;
    }
}
