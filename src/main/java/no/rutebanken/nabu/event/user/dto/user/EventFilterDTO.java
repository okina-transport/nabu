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

package no.rutebanken.nabu.event.user.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import no.rutebanken.nabu.domain.event.JobState;
import no.rutebanken.nabu.event.user.dto.organisation.OrganisationDTO;
import no.rutebanken.nabu.event.user.dto.responsibility.EntityClassificationDTO;

import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class EventFilterDTO {

    public enum EventFilterType {JOB, CRUD}


    private EventFilterType type;

    private String organisationRef;

    // Full objects included for ease of use, disregarded in CRUD
    private OrganisationDTO organisation;

    // Job event filter values
    private String jobDomain;

    private Set<String> actions = new HashSet<>();

    private Set<JobState> states = new HashSet<>();

    // Crud event filter values
    private Set<String> administrativeZoneRefs = new HashSet<>();

    private Set<String> entityClassificationRefs = new HashSet<>();

    // Full objects included for ease of use, disregarded in CRUD
    private Set<EntityClassificationDTO> entityClassifications = new HashSet<>();

    public EventFilterDTO(EventFilterType type) {
        this.type = type;
    }

    public EventFilterDTO() {
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventFilterDTO that = (EventFilterDTO) o;

        if (type != that.type) return false;
        if (organisationRef != null ? !organisationRef.equals(that.organisationRef) : that.organisationRef != null)
            return false;
        if (jobDomain != that.jobDomain) return false;
        if (actions != null ? !actions.equals(that.actions) : that.actions != null) return false;
        if (states != null ? !states.equals(that.states) : that.states != null) return false;
        if (administrativeZoneRefs != null ? !administrativeZoneRefs.equals(that.administrativeZoneRefs) : that.administrativeZoneRefs != null)
            return false;
        return entityClassificationRefs != null ? entityClassificationRefs.equals(that.entityClassificationRefs) : that.entityClassificationRefs == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (organisationRef != null ? organisationRef.hashCode() : 0);
        result = 31 * result + (jobDomain != null ? jobDomain.hashCode() : 0);
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        result = 31 * result + (states != null ? states.hashCode() : 0);
        result = 31 * result + (administrativeZoneRefs != null ? administrativeZoneRefs.hashCode() : 0);
        result = 31 * result + (entityClassificationRefs != null ? entityClassificationRefs.hashCode() : 0);
        return result;
    }

}
