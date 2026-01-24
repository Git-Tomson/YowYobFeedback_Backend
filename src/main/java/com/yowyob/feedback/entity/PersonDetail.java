package com.yowyob.feedback.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("person_detail")
public class PersonDetail implements Persistable<UUID> {
    
    @Id
    @Column("person_id")
    private UUID personId;
    
    @Column("occupation")
    private String occupation;

    @Transient
    private boolean isNew = true;

    // Constructors
    public PersonDetail() {}

    public PersonDetail(UUID personId, String occupation) {
        this.personId = personId;
        this.occupation = occupation;
    }

    // Getters and Setters
    public void setPersonId(UUID personId) { this.personId = personId; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public void setNew(boolean isNew) { this.isNew = isNew; }

    @Override
    public UUID getId() {
        return personId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}