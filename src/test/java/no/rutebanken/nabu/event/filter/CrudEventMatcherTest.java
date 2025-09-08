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

package no.rutebanken.nabu.event.filter;

import no.rutebanken.nabu.domain.event.CrudEvent;
import no.rutebanken.nabu.event.user.AdministrativeZoneRepository;
import no.rutebanken.nabu.event.user.dto.TypeDTO;
import no.rutebanken.nabu.event.user.dto.responsibility.EntityClassificationDTO;
import no.rutebanken.nabu.event.user.dto.user.EventFilterDTO;
import no.rutebanken.nabu.event.user.model.AdministrativeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrudEventMatcherTest {

    private static final String STOP_ENTITY_TYPE = "StopPlace";

    private static final String BUS_ENTITY_CLASSIFICATION = "onstreetBus";

    @Mock
    private AdministrativeZoneRepository administrativeZoneRepositoryMock;


    @Test
    void eventMatchingFilterWithoutAdminZonesSpecificType() {
        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier(BUS_ENTITY_CLASSIFICATION).build();
        assertThat(new CrudEventMatcher(administrativeZoneRepositoryMock, filter(BUS_ENTITY_CLASSIFICATION)).matches(event)).isTrue();
    }

    @Test
    void eventMatchingFilterWithoutAdminZonesWildcardType() {
        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier("whatever").build();
        assertThat(new CrudEventMatcher(administrativeZoneRepositoryMock, filter(EventMatcher.ALL_TYPES)).matches(event)).isTrue();
    }


    @Test
    void eventInAdminZoneMatching() {
        AdministrativeZone zone = adminZone();
        EventFilterDTO filterWithAdminZone = filter(BUS_ENTITY_CLASSIFICATION);
        filterWithAdminZone.getAdministrativeZoneRefs().add(zone.getId());

        when(administrativeZoneRepositoryMock.getAdministrativeZone(zone.getId())).thenReturn(zone);

        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier(BUS_ENTITY_CLASSIFICATION).geometry(zone.getPolygon().getCentroid()).build();

        assertThat(new CrudEventMatcher(administrativeZoneRepositoryMock, filterWithAdminZone).matches(event)).isTrue();
    }

    @Test
    void eventOutsideAdminZoneNotMatching() {
        AdministrativeZone zone = adminZone();
        EventFilterDTO filterWithAdminZone = filter(BUS_ENTITY_CLASSIFICATION);
        filterWithAdminZone.getAdministrativeZoneRefs().add(zone.getId());

        when(administrativeZoneRepositoryMock.getAdministrativeZone(zone.getId())).thenReturn(zone);

        Point pointOutside = new GeometryFactory().createPoint(new Coordinate(-50, -50));

        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier(BUS_ENTITY_CLASSIFICATION).geometry(pointOutside).build();

        assertThat(new CrudEventMatcher(administrativeZoneRepositoryMock, filterWithAdminZone).matches(event)).isFalse();
    }

    @Test
    void eventWrongTypeNotMatchingFilter() {
        CrudEvent event = CrudEvent.builder().entityType("NotMatchingType").entityClassifier(BUS_ENTITY_CLASSIFICATION).build();

        assertThat(new CrudEventMatcher(administrativeZoneRepositoryMock, filter(EventMatcher.ALL_TYPES)).matches(event)).isFalse();
    }

    @Test
    void eventWrongClassificationNotMatchingFilter() {
        CrudEvent event = CrudEvent.builder().entityType(STOP_ENTITY_TYPE).entityClassifier("onstreetTram").build();

        assertThat(new CrudEventMatcher(administrativeZoneRepositoryMock, filter(BUS_ENTITY_CLASSIFICATION)).matches(event)).isFalse();
    }

    private EventFilterDTO filter(String stopPlaceTypeClassificationCode) {
        EntityClassificationDTO entityTypeStopPlaceClassification = new EntityClassificationDTO();
        entityTypeStopPlaceClassification.setPrivateCode(STOP_ENTITY_TYPE);
        entityTypeStopPlaceClassification.setEntityType(new TypeDTO());
        entityTypeStopPlaceClassification.getEntityType().setPrivateCode("EntityType");


        EntityClassificationDTO stopPlaceTypeClassification = new EntityClassificationDTO();
        stopPlaceTypeClassification.setPrivateCode(stopPlaceTypeClassificationCode);
        stopPlaceTypeClassification.setEntityType(new TypeDTO());
        stopPlaceTypeClassification.getEntityType().setPrivateCode("StopPlaceType");

        EventFilterDTO eventFilter = new EventFilterDTO(EventFilterDTO.EventFilterType.CRUD);
        eventFilter.getEntityClassifications().add(entityTypeStopPlaceClassification);
        eventFilter.getEntityClassifications().add(stopPlaceTypeClassification);
        return eventFilter;
    }


    private AdministrativeZone adminZone() {

        GeometryFactory fact = new GeometryFactory();
        LinearRing linear = new GeometryFactory().createLinearRing(new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 0)});
        return new AdministrativeZone("test", "name", new Polygon(linear, null, fact));
    }
}
