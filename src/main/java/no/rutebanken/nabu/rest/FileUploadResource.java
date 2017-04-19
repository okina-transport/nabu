package no.rutebanken.nabu.rest;

import com.google.cloud.storage.Storage;
import no.rutebanken.nabu.domain.Provider;
import no.rutebanken.nabu.domain.Status;
import no.rutebanken.nabu.jms.JmsSender;
import no.rutebanken.nabu.repository.ProviderRepository;
import no.rutebanken.nabu.repository.StatusRepository;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.rutebanken.helper.gcp.BlobStoreHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;


@Component

@Path("/files")
public class FileUploadResource {

    @Value("${queue.upload.destination.name}")
    private String destinationName;

    @Value("${blobstore.gcs.credential.path}")
    private String credentialPath;

    @Value("${blobstore.gcs.container.name}")
    private String containerName;

    @Value("${blobstore.gcs.project.id}")
    private String projectId;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    JmsSender jmsSender;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadResource.class);

    @POST
    @Path("/{providerId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerId)")
    public Response uploadFile(@PathParam("providerId") final Long providerId, final FormDataMultiPart multiPart) {
        List<FormDataBodyPart> bodyParts = multiPart.getFields("files");
        try {
            for (int i = 0; i < bodyParts.size(); i++) {
                BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyParts.get(i).getEntity();
                String fileName = bodyParts.get(i).getContentDisposition().getFileName();
                String correlationId = UUID.randomUUID().toString();
                saveInBlobStore(bodyPartEntity.getInputStream(), fileName, providerId, correlationId);
            }
            return Response.accepted().build();
        } catch (RuntimeException e) {
            return Response.serverError().build();
        }
    }

    void saveInBlobStore(InputStream inputStream, String fileName, Long providerId, String correlationId) {
        try {
            logger.info("Placing file '" + fileName + "' from provider with id '" + providerId + "' and correlation id '" + correlationId + "' in blob store.");
            Provider provider = providerRepository.getProvider(providerId);
            Storage storage = BlobStoreHelper.getStorage(credentialPath, projectId);
            String referential = provider.chouetteInfo.referential;
            String blobName = "inbound/received/" + referential + "/" + fileName;
            logger.info("Blob created is: " + blobName);
            BlobStoreHelper.uploadBlob(storage, containerName, blobName, inputStream, false);
            logger.info("Notifying queue '" + destinationName + "' about the uploaded file.");
            jmsSender.sendBlobNotificationMessage(destinationName, blobName, fileName, providerId, correlationId);
            logger.info("Done sending.");
            statusRepository.add(new Status(fileName, providerId, null, Status.Action.FILE_TRANSFER, Status.State.STARTED, correlationId, Date.from(Instant.now(Clock.systemDefaultZone())), referential));
        } catch (RuntimeException e) {
            String errorMessage = "Failed to put file '" + fileName + "' in blobstore or notification on queue.";
            logger.warn(errorMessage, e);
            statusRepository.add(new Status(fileName, providerId, null, Status.Action.FILE_TRANSFER, Status.State.FAILED, correlationId, Date.from(Instant.now(Clock.systemDefaultZone())), null));
        }
    }

}
