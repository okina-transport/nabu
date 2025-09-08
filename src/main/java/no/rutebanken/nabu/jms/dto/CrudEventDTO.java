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

package no.rutebanken.nabu.jms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;
import org.wololo.geojson.Geometry;

import java.io.IOException;
import java.time.Instant;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CrudEventDTO {

    private Instant eventTime;

    private String correlationId;

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

    private Geometry geometry;

    private String location;

    private String description;

    private String type;

    public static CrudEventDTO fromString(String string) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(string, CrudEventDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
