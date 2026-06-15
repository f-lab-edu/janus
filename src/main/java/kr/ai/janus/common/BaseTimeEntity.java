package kr.ai.janus.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@MappedSuperclass
public abstract class BaseTimeEntity extends BaseCreatedEntity {

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
