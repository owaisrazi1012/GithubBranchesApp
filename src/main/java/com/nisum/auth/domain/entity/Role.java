package com.nisum.auth.domain.entity;

import com.nisum.auth.domain.enums.RoleName;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "roles")
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(name = "name")
    private RoleName name;


    public Role(RoleName name) {
        this.name = name;
    }
}