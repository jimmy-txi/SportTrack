package fr.utc.miage.sporttrack.dto;

/**
 * Base DTO for forms that carry an optional identifier.
 */
public abstract class AbstractIdFormDTO {

    private Integer id = 0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
