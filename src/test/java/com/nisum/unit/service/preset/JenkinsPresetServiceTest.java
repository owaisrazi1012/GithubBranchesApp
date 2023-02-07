package com.nisum.unit.service.preset;

import com.nisum.api.preset.domain.dto.JenkinsPresetRequestDTO;
import com.nisum.api.preset.domain.entity.JenkinsPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import com.nisum.exception.custom.JenkinsPresetServiceException;
import com.nisum.api.preset.repository.JenkinsPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.impl.JenkinsPresetServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class JenkinsPresetServiceTest {

    @InjectMocks
    JenkinsPresetServiceImpl jenkinsPresetService;

    @Mock
    private JenkinsPresetRepository jenkinsPresetRepository;

    @Mock
    private TestPresetRepository testPresetRepository;

    /**
     * Test Cases Tree
     *  1. GetJenkinsPresetsRecordsById
     *      1.1 GetJenkinsPresetsRecordsById -- positive case
     *      1.2 GetJenkinsPresetsRecordsById -- negative case (throw error)
     *
     *  2. GetAllJenkinsPresets (pageable --> page, size)
     *     2.1 GetAllJenkinsPresets -- positive case
     *     2.2 GetAllJenkinsPresets -- if no content return empty map
     *
     *  3. CreateJenkinsPreset
     *     3.1 CreateJenkinsPreset (with no testPreset)
     *     3.2 CreateJenkinsPreset (with testPreset)
     *
     *  4. UpdateJenkinsPreset
     *     4.1 UpdateJenkinsPreset -- positive case (no test preset)
     *     4.2 UpdateJenkinsPreset -- positive case (with test preset)
     *     4.3 UpdateJenkinsPreset -- negative case (id not found)
     *
     *  5. DeleteJenkinsPreset
     *     5.1 DeleteJenkinsPreset -- positive case
     *     5.2 DeleteJenkinsPreset -- negative case (throw error)
     */


    /**
     * 1.1 GetJenkinsPresetsRecordsById -- positive case
     */
    @Test
    public void shouldGetJenkinsPresetRecordsById()
    {
        when(jenkinsPresetRepository.findById(anyLong())).thenReturn(Optional.of(getJenkinsPreset()));
        jenkinsPresetService.getJenkinsPresetDetails(1L);
        verify(jenkinsPresetRepository, times(1)).findById(anyLong());
    }

    /**
     * 1.2 GetJenkinsPresetsRecordsById -- negative case (throw error)
     * ExecutionServiceException will be thrown
     * no content exception is expected this will be wrapped in ExecutionServiceException
     */
    @Test
    void shouldThrowJenkinsPresetExceptionWhenGetJenkinsPresetRecordsByIdIsNotFound()
    {
        when(jenkinsPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(JenkinsPresetServiceException.class,
                () -> jenkinsPresetService.getJenkinsPresetDetails(anyLong()));
    }

    /**
     *  2.1 GetAllJenkinsPresets -- positive case
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetAllJenkinsPresets()
    {
        when(jenkinsPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(Arrays.asList(getJenkinsPreset())));
        jenkinsPresetService.getAllJenkinsPresets(0, 1);
        verify(jenkinsPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 2.2 GetAllJenkinsPresets -- if no content return empty map
     * empty map will be return
     *
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetEmptyMapWhenAllJenkinsPresetHavingNoData()
    {
        when(jenkinsPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        jenkinsPresetService.getAllJenkinsPresets(0, 1);
        verify(jenkinsPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 3.1 CreateJenkinsPreset (with no testPreset)
     * with no testPreset
     */
    @Test
    public void shouldCreateJenkinsPreset()
    {
        when(jenkinsPresetRepository.save(Mockito.any(JenkinsPreset.class))).thenReturn(getJenkinsPreset());
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(mock(TestPreset.class)));
        jenkinsPresetService.createJenkinsPreset(getJenkinsPresetRequestDto());
        verify(jenkinsPresetRepository, times(1)).save(getJenkinsPreset());
    }

    /**
     * 3.2 CreateJenkinsPreset (with testPreset)
     * with testPreset
     */
    @Test
    public void shouldCreateJenkinsPresetWithTestPreset()
    {
        JenkinsPresetRequestDTO jenkinsPresetRequestDTO = getJenkinsPresetRequestDto();
        TestPreset testPreset = mock(TestPreset.class);
        JenkinsPreset jenkinsPreset = getJenkinsPreset();
        setTestPresetValuesInObjects(jenkinsPresetRequestDTO, jenkinsPreset, testPreset);

        when(jenkinsPresetRepository.save(Mockito.any(JenkinsPreset.class))).thenReturn(jenkinsPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        jenkinsPresetService.createJenkinsPreset(jenkinsPresetRequestDTO);
        verify(jenkinsPresetRepository, times(1)).save(jenkinsPreset);
    }

    /**
     * 4.1 UpdateJenkinsPreset -- positive case (no test preset)
     * no test preset
     */
    @Test
    public void shouldUpdateJenkinsPreset()
    {
        when(jenkinsPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getJenkinsPreset()));
        when(jenkinsPresetRepository.save(Mockito.any(JenkinsPreset.class))).thenReturn(getJenkinsPreset());
        jenkinsPresetService.updateJenkinsPreset(anyLong(), getJenkinsPresetRequestDto());
        verify(jenkinsPresetRepository, times(1)).save(getJenkinsPreset());
    }

    /**
     * 4.2 UpdateJenkinsPreset -- positive case (with test preset)
     * with test preset
     */
    @Test
    public void shouldUpdateJenkinsPresetWithTestingPresetDetails()
    {
        JenkinsPresetRequestDTO jenkinsPresetRequestDTO = getJenkinsPresetRequestDto();
        TestPreset testPreset = mock(TestPreset.class);
        JenkinsPreset jenkinsPreset = getJenkinsPreset();
        setTestPresetValuesInObjects(jenkinsPresetRequestDTO, jenkinsPreset, testPreset);

        when(jenkinsPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(jenkinsPreset));
        when(jenkinsPresetRepository.save(Mockito.any(JenkinsPreset.class))).thenReturn(jenkinsPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        jenkinsPresetService.updateJenkinsPreset(anyLong(), jenkinsPresetRequestDTO);
        verify(jenkinsPresetRepository, times(1)).save(jenkinsPreset);
    }

    /**
     * 4.3 UpdateJenkinsPreset -- negative case (id not found)
     * give JenkinsPresetServiceException by find by id
     */
    @Test
    void shouldThrowJenkinsPresetExceptionWhenGetUpdateJenkinsPresetByIdIsNotFound()
    {
        when(jenkinsPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(JenkinsPresetServiceException.class,
                () -> jenkinsPresetService.updateJenkinsPreset(anyLong(), getJenkinsPresetRequestDto()));
    }

    /**
     * 5.1 DeleteJenkinsPreset -- positive case
     */
    @Test
    public void shouldDeleteJenkinsPreset()
    {
        doNothing().when(jenkinsPresetRepository).deleteById(anyLong());
        jenkinsPresetService.deleteJenkinsPreset(anyLong());
        verify(jenkinsPresetRepository, times(1)).deleteById(anyLong());
    }

    /**
     * 5.2 DeleteJenkinsPreset -- negative case (throw error)
     */
    @Test
    void shouldThrowJenkinsPresetExceptionWhenGetDeleteJenkinsPresetByIdIsNotFound()
    {
        doThrow(EmptyResultDataAccessException.class).when(jenkinsPresetRepository).deleteById(anyLong());
        assertThrows(JenkinsPresetServiceException.class,
                () -> jenkinsPresetService.deleteJenkinsPreset(anyLong()));
    }


    private JenkinsPreset getJenkinsPreset(){
        return JenkinsPreset.builder()
                .name("Name of Jenkins Preset")
                .url("URL of Jenkins Preset")
                .password("Password of Jenkins Preset")
                .credentialId("Credential Id of Jenkins Preset")
                .slave("Slave of Jenkins Preset")
                .build();
    }

    private JenkinsPresetRequestDTO getJenkinsPresetRequestDto(){
        return JenkinsPresetRequestDTO.builder()
                .name("Name of Jenkins Preset")
                .url("URL of Jenkins Preset")
                .password("Password of Jenkins Preset")
                .credentialId("Credential Id of Jenkins Preset")
                .slave("Slave of Jenkins Preset")
                .build();
    }

    private void setTestPresetValuesInObjects(JenkinsPresetRequestDTO jenkinsPresetRequestDTO,
                                              JenkinsPreset jenkinsPreset,
                                              TestPreset testPreset
                                              ) {
        Set<TestPreset> testPresetSet = new HashSet<>();
        testPreset.setId(1L);
        testPresetSet.add(testPreset);

        jenkinsPresetRequestDTO.setTestPresetId(1L);
        jenkinsPreset.setTestPresets(new HashSet<TestPreset>(testPresetSet));


    }
}