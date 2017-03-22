package no.rutebanken.nabu.organisation.service;

import com.vividsolutions.jts.geom.Polygon;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import no.rutebanken.nabu.organisation.model.CodeSpace;
import no.rutebanken.nabu.organisation.model.organisation.AdministrativeZone;
import no.rutebanken.nabu.organisation.repository.AdministrativeZoneRepository;
import no.rutebanken.nabu.organisation.repository.CodeSpaceRepository;
import org.apache.commons.io.FileUtils;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdministrativeUnitsImporter {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AdministrativeZoneRepository repository;

	@Autowired
	private CodeSpaceRepository codeSpaceRepository;

	public void importKommuner(InputStream geoJSONStream, String codeSpaceId) {
		importAdministrativeUnits(geoJSONStream, "komm", 4, codeSpaceId);
	}

	public void importFylker(InputStream geoJSONStream, String codeSpaceId) {
		importAdministrativeUnits(geoJSONStream, "fylkesnr", 2, codeSpaceId);
	}

	public void importAdministrativeUnits(String codeSpaceId) {
		try {
			importFylker(FileUtils.openInputStream(new File("files/fylker.geojson")), codeSpaceId);
			importKommuner(FileUtils.openInputStream(new File("files/kommuner.geojson")), codeSpaceId);
		} catch (Exception e) {
			throw new RuntimeException("Import of admin units failed:" + e.getMessage());
		}
	}

	public void importAdministrativeUnits(InputStream inputStream, String idProperty, int idLength, String codeSpaceId) {
		CodeSpace codeSpace = codeSpaceRepository.getOneByPublicId(codeSpaceId);
		List<AdministrativeZone> administrativeZones = new ArrayList<>();
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("filtered", "geojson");
			new FeatureJSONFilter(inputStream, tmpFile.getPath(), idProperty, "area").filter();

			FeatureJSON fJson = new FeatureJSON();
			FeatureIterator<SimpleFeature> itr = fJson.streamFeatureCollection(FileUtils.openInputStream(tmpFile));

			while (itr.hasNext()) {
				SimpleFeature feature = itr.next();

				AdministrativeZone administrativeZone = new AdministrativeZone();

				administrativeZone.setCodeSpace(codeSpace);
				administrativeZone.setName(getProperty(feature, "navn"));
				administrativeZone.setPolygon((Polygon) feature.getDefaultGeometry());

				String id = StringUtils.leftPad("" + getProperty(feature, idProperty), idLength, "0");
				administrativeZone.setPrivateCode(id);
				administrativeZones.add(administrativeZone);
			}
			itr.close();
			repository.save(administrativeZones);


		} catch (Exception e) {
			throw new RuntimeException("Import of admin units failed with exception: " + e.getMessage(), e);
		} finally {
			if (tmpFile != null) {
				tmpFile.delete();
			}
		}
	}

	public <T> T getProperty(Feature feature, String propertyName) {
		Property property = feature.getProperty(propertyName);
		if (property == null) {
			return null;
		}
		return (T) property.getValue();
	}
}