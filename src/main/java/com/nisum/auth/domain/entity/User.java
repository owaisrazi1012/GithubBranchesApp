package com.nisum.auth.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nisum.auth.domain.entity.audit.DateAudit;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = { "username" }),
        @UniqueConstraint(columnNames = { "email" }) })
public class User extends DateAudit implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "username")
    @Size(max = 15)
    private String username;

    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(max = 100)
    @Column(name = "password")
    private String password;

    @NotBlank
    @Size(max = 40)
    @Column(name = "email")
    @Email
    private String email;

    @Column(name = "isActive")
    private Boolean isActive;

    @Column(name = "canEdit")
    private Boolean canEdit;

    @Column(name = "canDelete")
    private Boolean canDelete;

    @Column(name = "canCreate")
    private Boolean canCreate;

    @Column(name = "canView")
    private Boolean canView;

    @Column(name = "canBuild")
    private Boolean canBuild;

    @NotBlank
    @Column(name = "createdBy")
    private String createdBy;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles;

    public User(String username, String email, String password ) {
        this.username = username;
        this.email = email;
        this.password = password;
    }


    public List<Role> getRoles() {

        return roles == null ? null : new ArrayList<>(roles);
    }

    public void setRoles(List<Role> roles) {

        if (roles == null) {
            this.roles = null;
        } else {
            this.roles = roles;
        }
    }
}
