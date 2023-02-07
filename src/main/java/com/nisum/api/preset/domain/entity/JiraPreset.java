package com.nisum.api.preset.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "jira_preset")
public class JiraPreset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    private String userName;

    private String token;

    private String projectId;

    @JsonManagedReference
    @OneToMany(mappedBy = "jiraPreset", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<TestPreset> testPresets = new HashSet<>();

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", token='" + token + '\'' +
                ", projectId='" + projectId + '\'' +
                '}';
    }
}
