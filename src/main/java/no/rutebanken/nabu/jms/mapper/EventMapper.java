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

package no.rutebanken.nabu.jms.mapper;

import no.rutebanken.nabu.domain.event.CrudEvent;
import no.rutebanken.nabu.domain.event.JobEvent;
import no.rutebanken.nabu.jms.dto.CrudEventDTO;
import no.rutebanken.nabu.jms.dto.JobEventDTO;
import org.wololo.jts2geojson.GeoJSONReader;

public class EventMapper {

    public CrudEvent toCrudEvent(CrudEventDTO dto) {
        CrudEvent event = new CrudEvent();

        event.setAction(dto.getAction());
        event.setCorrelationId(dto.getCorrelationId());
        event.setEventTime(dto.getEventTime());
        event.setExternalId(dto.getExternalId());
        event.setName(dto.getName());
        event.setChangeType(dto.getChangeType());

        event.setEntityType(dto.getEntityType());
        event.setVersion(dto.getVersion());
        event.setEntityClassifier(dto.getEntityClassifier());
        event.setOldValue(dto.getOldValue());
        event.setNewValue(dto.getNewValue());
        event.setComment(dto.getComment());
        event.setUsername(dto.getUsername());
        event.setDescription(dto.getDescription());
        event.setType(dto.getType());
        event.setLocation(dto.getLocation());

        if (dto.getGeometry() != null) {
            event.setGeometry(new GeoJSONReader().read(dto.getGeometry()));
        }

        return event;
    }

    public JobEvent toJobEvent(JobEventDTO dto) {
        JobEvent event = new JobEvent();
        event.setAction(dto.getAction());
        event.setCorrelationId(dto.getCorrelationId());
        event.setEventTime(dto.getEventTime());
        event.setExternalId(dto.getExternalId());
        event.setName(dto.getName());
        event.setProviderId(dto.getProviderId());
        event.setReferential(dto.getReferential());
        event.setState(dto.getState());
        event.setDomain(dto.getDomain());
        event.setUsername(dto.getUsername());
        event.setDescription(dto.getDescription());
        event.setType(dto.getType());

        return event;
    }
}
