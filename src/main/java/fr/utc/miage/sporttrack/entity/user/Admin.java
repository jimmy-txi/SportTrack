package fr.utc.miage.sporttrack.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * JPA entity representing an administrator user within the SportTrack application.
 *
 * <p>An admin inherits all common user properties from {@link User} and is
 * mapped to the {@code admins} database table. Administrators have elevated
 * privileges for managing sports, badges, and other system-wide resources.</p>
 *
 * @author SportTrack Team
 */
@Entity
@Table(name = "admins")
public class Admin extends User {
}