package com.rydvrse.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public abstract class AbstractVersionedEntity extends AbstractTimestampedEntity {

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion = 0L;

    public Long getRowVersion() {
        return rowVersion;
    }
}
