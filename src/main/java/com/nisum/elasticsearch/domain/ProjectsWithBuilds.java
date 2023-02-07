package com.nisum.elasticsearch.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class ProjectsWithBuilds {
    Map<String, Set<String>> builds = new HashMap<>();
    Set<String> projects = new HashSet<>();
}