package com.nisum.unit.service.preset;

import com.nisum.api.preset.domain.dto.*;
import com.nisum.api.preset.domain.entity.ExecutionPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import com.nisum.exception.custom.ExecutionServiceException;
import com.nisum.api.preset.repository.ExecutionPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.impl.ExecutionPresetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ExecutionPresetServiceTest {

    @InjectMocks
    ExecutionPresetServiceImpl executionPresetService;

    @Mock
    ExecutionPresetRepository executionPresetRepository;

    @Mock
    TestPresetRepository testPresetRepository;

    @BeforeEach
    public void setUp() {
    }

    /**
     * Test Cases Tree
     *  1. GetExecutionPresetRecordsById
     *      1.1 GetExecutionPresetRecordsById -- positive case
     *      1.2 GetExecutionPresetRecordsById -- negative case (throw error)
     *
     *  2. GetAllExecutionPresets (pageable --> page, size)
     *     2.1 GetAllExecutionPresets -- positive case
     *     2.2 GetAllExecutionPresets -- if no content return empty map
     *
     *  3. GetAllExecutionPreset
     *     3.1 GetAllExecutionPreset -- positive case
     *     3.2 GetAllExecutionPreset -- negative case (no content -- return empty array)
     *
     *  4. CreateExecutionPreset
     *     4.1 CreateExecutionPreset (with no testPreset and no maven)
     *     4.2 CreateExecutionPreset (with testPreset and no maven)
     *     4.3 CreateExecutionPreset (with testPreset and maven) -- having issues
     *
     *  5. UpdateExecutionPreset
     *     5.1 UpdateExecutionPreset -- positive case (no preset and no maven)
     *     5.2 UpdateExecutionPreset -- positive case (with preset, no maven)
     *     5.3 UpdateExecutionPreset -- positive case (no preset, with maven)
     *     5.4 UpdateExecutionPreset -- positive case (with preset and maven)
     *     5.5 UpdateExecutionPreset -- positive case (Db contains maven config, but user request won't)
     *     5.6 UpdateExecutionPreset -- positive case (user request contains maven config but DB won't)
     *     5.7 UpdateExecutionPreset -- duplicate keys (throw error)
     *
     *  6. DeleteExecutionPreset
     *     6.1 DeleteExecutionPreset -- positive case
     *     6.2 DeleteExecutionPreset -- negative case (throw error)
     */


    /**
     * 1.1 GetExecutionPresetRecordsById -- positive case
     */
    @Test
    public void shouldGetExecutionPresetRecordsById()
    {
        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(getExecutionPreset()));
        executionPresetService.getExecutionPresetById(1L);
        verify(executionPresetRepository, times(1)).findById(anyLong());
    }

    /**
     * 1.2 GetExecutionPresetRecordsById -- negative case (throw error)
     * ExecutionServiceException will be thrown
     * no content exception is expected this will be wrapped in ExecutionServiceException
     */
    @Test
    void shouldThrowGitPresetExceptionWhenGetExecutionPresetRecordsByIdIsNotFound()
    {
        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ExecutionServiceException.class,
                () -> executionPresetService.getExecutionPresetById(anyLong()));
    }

    /**
     * 2.1 GetAllExecutionPresets -- positive case
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetAllExecutionPresets()
    {
        when(executionPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(Arrays.asList(getExecutionPreset())));
        executionPresetService.getAllExecutionPresets(0, 1);
        verify(executionPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 2.2 GetAllExecutionPresets -- if no content return empty map
     * empty map will be return
     *
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetEmptyMapWhenAllExecutionPresetHavingNoData()
    {
        when(executionPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        executionPresetService.getAllExecutionPresets(0, 1);
        verify(executionPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 3.1 GetAllExecutionPreset -- positive case
     */
    @Test
    public void shouldGetAllExecutionPreset(){
        when(executionPresetRepository.findAll()).thenReturn(Arrays.asList(getExecutionPreset()));
        executionPresetService.getExecutionPreset();
        verify(executionPresetRepository, times(1)).findAll();
    }

    /**
     * 3.2 GetAllExecutionPreset -- negative case (no content -- return empty array)
     * no conent then return empty array
     */
    @Test
    public void shouldGetExecutionPresetHavingNoContent(){
        when(executionPresetRepository.findAll()).thenReturn(new ArrayList<>());
        executionPresetService.getExecutionPreset();
        verify(executionPresetRepository, times(1)).findAll();
    }

    /**
     * 4.1 CreateExecutionPreset (with no testPreset and no maven)
     * with no testPreset
     * with no maven
     */
    @Test
    public void shouldCreateExecutionPreset()
    {
        when(executionPresetRepository.save(Mockito.any(ExecutionPreset.class))).thenReturn(getExecutionPreset());
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(mock(TestPreset.class)));
        executionPresetService.save(getExecutionPresetRequestDto());
        verify(executionPresetRepository, times(1)).save(getExecutionPreset());
    }

    /**
     * 4.2 CreateExecutionPreset (with testPreset and no maven)
     * with testPreset
     * without maven
     */
    @Test
    public void shouldCreateExecutionPresetWithTestPresetWithOutMaven()
    {
        ExecutionPresetRequestDTO executionPresetRequestDTO = getExecutionPresetRequestDto();
        ExecutionPresetResponseDTO executionPresetResponseDTO = getExecutionPresetResponseDto();
        TestPreset testPreset = mock(TestPreset.class);
        ExecutionPreset executionPreset = getExecutionPreset();
        setTestPresetValuesForSaveOperation(executionPresetRequestDTO,executionPreset, testPreset);

        when(executionPresetRepository.save(Mockito.any(ExecutionPreset.class))).thenReturn(executionPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        executionPresetService.save(executionPresetRequestDTO);
        verify(executionPresetRepository, times(1)).save(executionPreset);
    }

    /**
     * 4.3 CreateExecutionPreset (with testPreset and maven)
     * with testPreset
     * with maven
     * having issue, have creating cycles
     */
    @Test
    public void  shouldCreateExecutionPresetWithTestPresetWithMaven()
    {
        ExecutionPresetRequestDTO executionPresetRequestDTO = getExecutionPresetRequestDto();
        ExecutionPresetResponseDTO executionPresetResponseDTO = getExecutionPresetResponseDto();
        TestPreset testPreset = getTestPreset();
        ExecutionPreset executionPreset = getExecutionPreset();
        setTestPresetValuesForSaveOperation(executionPresetRequestDTO, executionPreset, testPreset);
        setMavenValuesForSaveOperation(executionPresetRequestDTO, executionPresetResponseDTO, executionPreset);

        Set testPresetResponseSet = new HashSet<>();
        testPresetResponseSet.add(getTestPresetResponseDTO());
        executionPresetResponseDTO.setTestPresets(testPresetResponseSet);


        when(executionPresetRepository.save(Mockito.any(ExecutionPreset.class))).thenReturn(executionPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));

        assertEquals(executionPresetService.save(executionPresetRequestDTO), executionPresetResponseDTO);
    }

    /**
     * 5.1 UpdateExecutionPreset -- positive case (no preset and no maven)
     * no test preset
     * no maven
     */

    @Test
    public void shouldUpdateExecutionPresetWithNoTestPresetNoMaven()
    {
        ExecutionPresetRequestDTO executionPresetRequestDTO = getExecutionPresetRequestDto();
        ExecutionPreset executionPreset = getExecutionPreset();

        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(executionPreset));
        when(executionPresetRepository.save(Mockito.any(ExecutionPreset.class))).thenReturn(executionPreset);

        executionPresetService.updateExecutionPreset(1l,  executionPresetRequestDTO);
        verify(executionPresetRepository, times(1)).save(executionPreset);
    }

    /**
     *  5.2 UpdateExecutionPreset -- positive case (with preset, no maven)
     *  with test preset
     *  with no maven
     */
    @Test
    public void shouldUpdateExecutionPresetWithTestPresetWithNoMaven()
    {
        ExecutionPresetRequestDTO executionPresetRequestDTO = getExecutionPresetRequestDto();
        ExecutionPresetResponseDTO executionPresetResponseDTO = getExecutionPresetResponseDto();
        TestPreset testPreset = mock(TestPreset.class);
        ExecutionPreset executionPreset = getExecutionPreset();
        setTestPresetValuesForSaveOperation(executionPresetRequestDTO, executionPreset, testPreset);

        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(executionPreset));
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        when(executionPresetRepository.save(Mockito.any(ExecutionPreset.class))).thenReturn(executionPreset);

        executionPresetService.updateExecutionPreset(1l,  executionPresetRequestDTO);
        verify(executionPresetRepository, times(1)).save(executionPreset);
    }

    /**
     *  5.3 UpdateExecutionPreset -- positive case (no preset, with maven)
     *  no test preset
     *  with maven
     */
    @Test
    public void shouldUpdateExecutionPresetWithNoTestPresetWithMaven()
    {
        ExecutionPresetRequestDTO executionPresetRequestDTO = getExecutionPresetRequestDto();
        ExecutionPresetResponseDTO executionPresetResponseDTO = getExecutionPresetResponseDto();
        ExecutionPreset executionPreset = getExecutionPreset();
        setMavenValuesForSaveOperation(executionPresetRequestDTO, executionPresetResponseDTO, executionPreset);


        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(executionPreset));
        when(executionPresetRepository.save(Mockito.any(ExecutionPreset.class))).thenReturn(executionPreset);

        executionPresetService.updateExecutionPreset(1l,  executionPresetRequestDTO);
        verify(executionPresetRepository, times(1)).save(executionPreset);
    }

    /**
     *  5.4 UpdateExecutionPreset -- positive case (with preset and maven)
     *  with test preset
     *  with maven
     */

    @Test
    public void shouldUpdateExecutionPresetWithTestPresetWithMaven()
    {
        ExecutionPresetRequestDTO executionPresetRequestDTO = getExecutionPresetRequestDto();
        ExecutionPresetResponseDTO executionPresetResponseDTO = getExecutionPresetResponseDto();
        TestPreset testPreset = mock(TestPreset.class);
        ExecutionPreset executionPreset = getExecutionPreset();
        setTestPresetValuesForSaveOperation(executionPresetRequestDTO, executionPreset, testPreset);
        setMavenValuesForSaveOperation(executionPresetRequestDTO, executionPresetResponseDTO, executionPreset);


        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(executionPreset));
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        when(executionPresetRepository.save(Mockito.any(ExecutionPreset.class))).thenReturn(executionPreset);

        executionPresetService.updateExecutionPreset(1l,  executionPresetRequestDTO);
        verify(executionPresetRepository, times(1)).save(executionPreset);
    }

    /**
     *  5.5 UpdateExecutionPreset -- positive case (Db contains maven config, but user request won't)
     *  with test preset
     *  with maven
     */

    @Test
    public void shouldUpdateExecutionPresetWithTestPresetAndWithMavenConfigInDb()
    {
        ExecutionPresetRequestDTO executionPresetRequestDTO = getExecutionPresetRequestDto();
        ExecutionPresetResponseDTO executionPresetResponseDTO = getExecutionPresetResponseDto();
        TestPreset testPreset = mock(TestPreset.class);
        ExecutionPreset executionPreset = getExecutionPreset();
        setTestPresetValuesForSaveOperation(executionPresetRequestDTO, executionPreset, testPreset);
        setMavenValuesOnlyInExecutionPreset(executionPreset);


        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(executionPreset));
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        when(executionPresetRepository.save(Mockito.any(ExecutionPreset.class))).thenReturn(executionPreset);

        executionPresetService.updateExecutionPreset(1l,  executionPresetRequestDTO);
        verify(executionPresetRepository, times(1)).save(executionPreset);
    }

    /**
     *  5.6 UpdateExecutionPreset -- positive case (user request contains maven config but DB won't)
     *  with test preset
     *  with maven
     */

    @Test
    public void shouldUpdateExecutionPresetWithTestPresetAndWithMavenConfigInUserRequest()
    {
        ExecutionPresetRequestDTO executionPresetRequestDTO = getExecutionPresetRequestDto();
        ExecutionPresetResponseDTO executionPresetResponseDTO = getExecutionPresetResponseDto();
        TestPreset testPreset = mock(TestPreset.class);
        ExecutionPreset executionPreset = getExecutionPreset();
        setTestPresetValuesForSaveOperation(executionPresetRequestDTO, executionPreset, testPreset);
        setMavenValuesOnlyInExecutionPresetRequestDto(executionPresetRequestDTO);


        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(executionPreset));
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        when(executionPresetRepository.save(Mockito.any(ExecutionPreset.class))).thenReturn(executionPreset);

        executionPresetService.updateExecutionPreset(1l,  executionPresetRequestDTO);
        verify(executionPresetRepository, times(1)).save(executionPreset);
    }

    /**
     *  5.7 UpdateExecutionPreset -- duplicate keys (throw error)
     *  throw error
     */
    @Test
    public void shouldUpdateExecutionPresetWithMavenDuplicateKeys() {
        ExecutionPresetRequestDTO executionPresetRequestDTO = getExecutionPresetRequestDto();
        ExecutionPreset executionPreset = getExecutionPreset();
        setMavenValuesForUpdateDuplicateKeys(executionPresetRequestDTO, executionPreset);

        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(executionPreset));

        assertThrows(ExecutionServiceException.class,
                () -> executionPresetService.updateExecutionPreset(1l, executionPresetRequestDTO));
    }

    /**
     * 6.1 DeleteExecutionPreset -- positive case
     */
    @Test
    public void shouldDeleteGitPreset()
    {
        doNothing().when(executionPresetRepository).deleteById(anyLong());
        executionPresetService.deleteExecutionPreset(anyLong());
        verify(executionPresetRepository, times(1)).deleteById(anyLong());
    }

    /**
     * 6.2 DeleteExecutionPreset -- negative case (throw error)
     */
    @Test
    void shouldThrowGitPresetExceptionWhenGetDeleteGitPresetByIdIsNotFound()
    {
        doThrow(EmptyResultDataAccessException.class).when(executionPresetRepository).deleteById(anyLong());
        assertThrows(ExecutionServiceException.class,
                () -> executionPresetService.deleteExecutionPreset(anyLong()));
    }


    private ExecutionPreset getExecutionPreset(){
        return ExecutionPreset.builder()
                .name("Testing Execution Preset Name")
                .mavenConfigParams(new HashSet<>())
//                .testPresets()
                .build();
    }

    private ExecutionPresetRequestDTO getExecutionPresetRequestDto(){
        return ExecutionPresetRequestDTO.builder()
                .name("Testing Execution Preset Name")
                .mavenConfigParams(new HashSet<>())
//                .testPresets()
                .build();
    }

    private ExecutionPresetResponseDTO getExecutionPresetResponseDto(){
        return ExecutionPresetResponseDTO.builder()
                .name("Testing Execution Preset Name")
                .mavenConfigParams(new HashSet<>())
//                .testPresets()
                .build();
    }

    private void setTestPresetValuesForSaveOperation(ExecutionPresetRequestDTO executionPresetRequestDTO,
                                                     ExecutionPreset executionPreset,
                                                     TestPreset testPreset) {
        Set testPresetSet = new HashSet<>();
        testPresetSet.add(testPreset);
        executionPresetRequestDTO.setTestPresetId(1L);
        executionPreset.setTestPresets(testPresetSet);
    }

    private void setMavenValuesOnlyInExecutionPresetRequestDto(ExecutionPresetRequestDTO executionPresetRequestDTO){
        Set<MavenConfigParamRequestDTO> mavenConfigParamRequestDTOHashSet
                = new HashSet<>();
        mavenConfigParamRequestDTOHashSet.add(getMavenConfigParamRequestDTO());

        executionPresetRequestDTO.setMavenConfigParams(mavenConfigParamRequestDTOHashSet);
    }

    private void setMavenValuesOnlyInExecutionPreset(ExecutionPreset executionPreset){
        Set<MavenConfigParamRequestDTO> mavenConfigParamRequestDTOHashSet
                = new HashSet<>();
        mavenConfigParamRequestDTOHashSet.add(getMavenConfigParamRequestDTO());

        executionPreset.setMavenConfigParams(mavenConfigParamRequestDTOHashSet);
    }


    private void setMavenValuesForSaveOperation(ExecutionPresetRequestDTO executionPresetRequestDTO,
                                                ExecutionPresetResponseDTO executionPresetResponseDTO,
                                                 ExecutionPreset executionPreset){
        Set<MavenConfigParamRequestDTO> mavenConfigParamRequestDTOHashSet = new HashSet<>();
        mavenConfigParamRequestDTOHashSet.add(getMavenConfigParamRequestDTO());
        executionPreset.setMavenConfigParams(mavenConfigParamRequestDTOHashSet);

        executionPresetRequestDTO.setMavenConfigParams(mavenConfigParamRequestDTOHashSet);

        Set<MavenConfigParamResponseDTO> mavenConfigParamResponseDTOHashSet = new HashSet<>();
        mavenConfigParamResponseDTOHashSet.add(getMavenConfigParamResponseDTO());

        executionPresetResponseDTO.setMavenConfigParams(mavenConfigParamResponseDTOHashSet);
    }

    private void setMavenValuesForUpdateDuplicateKeys(ExecutionPresetRequestDTO executionPresetRequestDTO,
                                                      ExecutionPreset executionPreset){
        Set<MavenConfigParamRequestDTO> mavenConfigParamRequestDTOHashSet
                = new HashSet<>();

        mavenConfigParamRequestDTOHashSet.add(getMavenConfigParamRequestDTO());
        MavenConfigParamRequestDTO mavenConfigParamRequestDtoSecond = getMavenConfigParamRequestDTO();
        mavenConfigParamRequestDtoSecond.setId(2L);
        mavenConfigParamRequestDTOHashSet.add(mavenConfigParamRequestDtoSecond);

        executionPreset.setMavenConfigParams(mavenConfigParamRequestDTOHashSet);
        executionPresetRequestDTO.setMavenConfigParams(mavenConfigParamRequestDTOHashSet);
    }


    private MavenConfigParamRequestDTO getMavenConfigParamRequestDTO() {
        MavenConfigParamRequestDTO mavenConfigParamRequestDTO = new MavenConfigParamRequestDTO();
        mavenConfigParamRequestDTO.setId(1L);
        mavenConfigParamRequestDTO.setKey("key");
        mavenConfigParamRequestDTO.setValue("value");

        return mavenConfigParamRequestDTO;
    }

    private MavenConfigParamResponseDTO getMavenConfigParamResponseDTO() {
        MavenConfigParamResponseDTO mavenConfigParamResponseDTO = new MavenConfigParamResponseDTO();
        mavenConfigParamResponseDTO.setKey("key");
        mavenConfigParamResponseDTO.setValue("value");

        return mavenConfigParamResponseDTO;
    }

    private TestPreset getTestPreset(){
        return TestPreset.builder()
                .name("test preset name")
                .build();
    }

    private TestPresetResponseDTO getTestPresetResponseDTO(){
        return TestPresetResponseDTO.builder()
                .name("test preset name")
                .build();
    }


}
