package no.rutebanken.nabu.organization.model.user;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import no.rutebanken.nabu.organization.model.VersionedEntity;
import no.rutebanken.nabu.organization.model.organization.Organisation;
import no.rutebanken.nabu.organization.model.responsibility.ResponsibilitySet;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(uniqueConstraints = {
		                           @UniqueConstraint(columnNames = {"privateCode", "entityVersion"})
})
public class User extends VersionedEntity {

	@NotNull
	private String username;

	@OneToOne
	private ContactDetails contactDetails;

	@NotNull
	@ManyToOne
	private Organisation organisation;

	@OneToMany
	private Set<Notification> notifications;

	@ManyToMany
	private Set<ResponsibilitySet> responsibilitySets;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public ContactDetails getContactDetails() {
		return contactDetails;
	}

	public void setContactDetails(ContactDetails contactDetails) {
		this.contactDetails = contactDetails;
	}

	public Set<Notification> getNotifications() {
		return notifications;
	}

	public void setNotifications(Set<Notification> notifications) {
		this.notifications = notifications;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public Set<ResponsibilitySet> getResponsibilitySets() {
		return responsibilitySets;
	}

	public void setResponsibilitySets(Set<ResponsibilitySet> responsibilitySets) {
		this.responsibilitySets = responsibilitySets;
	}

	@Override
	public String getId() {
		return Joiner.on(":").join(getType(), getPrivateCode());
	}

}