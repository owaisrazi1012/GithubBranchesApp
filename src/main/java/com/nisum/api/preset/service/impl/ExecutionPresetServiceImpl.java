package com.nisum.api.preset.service.impl;

import com.nisum.api.preset.domain.dto.ExecutionPresetRequestDTO;
import com.nisum.api.preset.domain.dto.ExecutionPresetResponseDTO;
import com.nisum.api.preset.domain.dto.MavenConfigParamRequestDTO;
import com.nisum.api.preset.domain.entity.ExecutionPreset;
import com.nisum.api.preset.domain.entity.MavenConfigParam;
import com.nisum.exception.custom.ExecutionServiceException;
import com.nisum.api.preset.repository.ExecutionPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.ExecutionPresetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nisum.util.Constants.NO_RECORD_FOUND;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
@Profile({"dev", "qa"})
public class ExecutionPresetServiceImpl implements ExecutionPresetService {

   @Autowired
   ExecutionPresetRepository executionPresetRepository;

   @Autowired
   TestPresetRepository testPresetRepository;

   @Override
   public List<ExecutionPresetResponseDTO> getExecutionPreset() {
      List<ExecutionPreset> executionPresets = (List<ExecutionPreset>) executionPresetRepository.findAll();
      List<ExecutionPresetResponseDTO> executionPresetResponseDTOList = new ArrayList<>();
      for (ExecutionPreset executionPreset : executionPresets) {
         executionPresetResponseDTOList.add(new ExecutionPresetResponseDTO(executionPreset));
      }
      return executionPresetResponseDTOList;
   }

   @Override
   public ExecutionPresetResponseDTO getExecutionPresetById(Long id) {
      Optional<ExecutionPreset> executionPreset = executionPresetRepository.findById(id);
      return executionPreset.map(ExecutionPresetResponseDTO::new)
              .orElseThrow(new ExecutionServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable()));
   }
   @Transactional
   @Override
   public ExecutionPresetResponseDTO save(ExecutionPresetRequestDTO executionPresetRequest) {
      ExecutionPreset executionPreset = ExecutionPreset.builder()
              .name((executionPresetRequest.getName()))
              .mavenConfigParams(new HashSet<>())
              .testPresets(executionPresetRequest.getTestPresetId() != null ?
                      Collections.singleton(testPresetRepository.findById(executionPresetRequest.getTestPresetId()).get()) : null)
              .build();
      for (MavenConfigParamRequestDTO mcp : executionPresetRequest.getMavenConfigParams()) {
         executionPreset.addMavenConfigParam(new MavenConfigParam(mcp.getKey(), mcp.getValue()));
      }
      return new ExecutionPresetResponseDTO(executionPresetRepository.save(executionPreset));
   }

   @Override
   public Map<String, Object> getAllExecutionPresets(int page, int size) {
      Page<ExecutionPreset> executionPresetPage =
              executionPresetRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
      return getExecutionPresetsResponseMap(size, executionPresetPage);
   }

   @Override
   public ExecutionPresetResponseDTO updateExecutionPreset(Long id, ExecutionPresetRequestDTO executionPresetRequest) {
      ExecutionPreset executionPreset = executionPresetRepository.findById(id)
              .orElseThrow(new ExecutionServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable()));

      executionPreset.setName(executionPresetRequest.getName());

      List<String> userRequestedKeys = executionPresetRequest.getMavenConfigParams().stream().map(MavenConfigParamRequestDTO::getKey).collect(Collectors.toList());
      if(!executionPresetRequest.getMavenConfigParams().stream()
              .filter(d -> Collections.frequency(userRequestedKeys, d.getKey()) > 1)
              .collect(Collectors.toSet()).isEmpty()){
         throw new ExecutionServiceException(CONFLICT.value(), "Duplicate Keys are not allowed" , new Throwable());
      }

      Map<Long, MavenConfigParamRequestDTO> userRequestMap=
              executionPresetRequest.getMavenConfigParams().stream()
                      .collect(Collectors.toMap(MavenConfigParamRequestDTO::getId, MavenConfigParamRequestDTO::getMavenConfigParamRequestDTO));

      Map<Long, MavenConfigParam> dbSavedMap=
              executionPreset.getMavenConfigParams().stream()
                      .collect(Collectors.toMap(MavenConfigParam::getId, MavenConfigParam::getMavenConfigParam));

      userRequestMap.forEach((key, value) -> {
         String mavenKey = value.getKey();
         String mavenValue = value.getValue();
         if(dbSavedMap.get(key) != null){
            MavenConfigParam mavenConfigParam = dbSavedMap.get(key);
            mavenConfigParam.setMavenKey(mavenKey);
            mavenConfigParam.setMavenValue(mavenValue);
         } else {
            executionPreset.addMavenConfigParam(new MavenConfigParam(mavenKey, mavenValue));
         }
      });

      dbSavedMap.forEach((key, value) -> {
         if(userRequestMap.get(key) == null)
            executionPreset.removeMavenConfigParam(dbSavedMap.get(key));
      });

      if (executionPresetRequest.getTestPresetId() != null) {
         executionPreset.setTestPresets(Collections.singleton(testPresetRepository.findById(executionPresetRequest.getTestPresetId()).get()));
      }
      
      return new ExecutionPresetResponseDTO(executionPresetRepository.save(executionPreset));
   }

   @Override
   public void deleteExecutionPreset(Long id) {
      try {
         executionPresetRepository.deleteById(id);
      } catch (EmptyResultDataAccessException ex) {
         throw new ExecutionServiceException(NOT_ACCEPTABLE.value(),NO_RECORD_FOUND,new Throwable());
      }
   }

   private Map<String, Object> getExecutionPresetsResponseMap(int size, Page<ExecutionPreset> executionPresetPage) {
      Map<String, Object> response = new HashMap<>();

      if (executionPresetPage.hasContent()) {
         response.put("executionPresets",
                 executionPresetPage.getContent()
                         .stream()
                         .map(ExecutionPresetResponseDTO::new)
                         .collect(Collectors.toCollection(LinkedHashSet::new)));
         response.put("currentPage", executionPresetPage.getNumber());
         response.put("limit", size);
         response.put("totalRecords", executionPresetPage.getTotalElements());
         response.put("totalPages", executionPresetPage.getTotalPages());
      } else { // populate empty map
         response.put("executionPresets", new HashSet<>());
         response.put("currentPage", 0);
         response.put("limit", size);
         response.put("totalRecords", 0l);
         response.put("totalPages", 0);
      }
      return response;
   }

}