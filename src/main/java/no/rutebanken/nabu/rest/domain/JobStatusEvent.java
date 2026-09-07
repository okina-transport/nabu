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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import no.rutebanken.nabu.domain.event.JobEvent;
import no.rutebanken.nabu.rest.domain.JobStatus.State;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class JobStatusEvent {

    private static final String GTFS_FULL_FLEX_TYPE = "GTFS_FULL_FLEX";

    @JsonProperty("state")
    public State state;

    @JsonProperty("date")
    public Date date;

    @JsonProperty("action")
    public String action;

    @JsonProperty("chouetteJobId")
    public Long chouetteJobId;

    @JsonProperty("uttuJobId")
    public Long uttuJobId;

    @JsonProperty("referential")
    public String referential;

    @JsonProperty("type")
    public String type;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("lugStatus")
    public String lugStatus;

    @JsonProperty("flexible")
    public Boolean flexible;

    public static JobStatusEvent createFromJobEvent(JobEvent e) {
        Long externalId = e.getExternalId() == null ? null : Long.parseLong(e.getExternalId());
        boolean fullFlex = GTFS_FULL_FLEX_TYPE.equals(e.getType());
        Long chouetteId = fullFlex ? null : externalId;
        Long uttuId = fullFlex ? externalId : null;
        return new JobStatusEvent(State.valueOf(e.getState().name()), Date.from(e.getEventTime()), e.getAction(),
                chouetteId, uttuId, e.getReferential(), e.getType(), e.getName(), e.getDescription(), e.getLugStatus(), e.isFlexible());
    }
}
