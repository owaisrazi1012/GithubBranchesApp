package com.nisum.unit.service.preset;

import com.nisum.api.preset.domain.dto.*;
import com.nisum.api.preset.domain.entity.*;
import com.nisum.exception.custom.TestingPresetServiceException;
import com.nisum.api.preset.repository.*;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.PresetMapperService;
import com.nisum.api.preset.service.impl.TestingPresetServiceImpl;
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
public class TestingPresetServiceTest {

    @InjectMocks
    TestingPresetServiceImpl testPresetService;

    @Mock
    private TestPresetRepository testPresetRepository;

    @Mock
    private GitPresetRepository gitPresetRepository;

    @Mock
    private JenkinsPresetRepository jenkinsPresetRepository;

    @Mock
    private ExecutionPresetRepository executionPresetRepository;

    @Mock
    private NotificationPresetRepository notificationPresetRepository;

    @Mock
    private PresetMapperService presetMapperService;

    /**
     * Test Cases Tree
     *  1. GetTestPresetsRecordsById
     *      1.1 GetTestPresetsRecordsById -- positive case
     *      1.2 GetTestPresetsRecordsById -- negative case (throw error)
     *
     *  2. GetAllTestPresets (pageable --> page, size)
     *     2.1 GetAllTestPresets -- positive case
     *     2.2 GetAllTestPresets -- if no content return empty map
     *
     *  3. CreateTestPreset
     *     3.1 CreateTestPreset (with no other details)
     *     3.2 CreateTestPreset (with other details)
     *
     *  4. UpdateTestPreset
     *     4.1 UpdateTestPreset -- positive case (no other details)
     *     4.2 UpdateTestPreset -- positive case (with other details)
     *     4.3 UpdateTestPreset -- negative case (id not found)
     *
     *  5. DeleteTestPreset
     *     5.1 DeleteTestPreset -- positive case
     *     5.2 DeleteTestPreset -- negative case (throw error)
     */


    /**
     * 1.1 GetTestPresetsRecordsById -- positive case
     */
    @Test
    public void shouldGetTestPresetRecordsById()
    {
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(getTestPreset()));
        testPresetService.getTestPresetDetails(1L);
        verify(testPresetRepository, times(1)).findById(anyLong());
    }

    /**
     * 1.2 GetTestPresetsRecordsById -- negative case (throw error)
     * ExecutionServiceException will be thrown
     * no content exception is expected this will be wrapped in ExecutionServiceException
     */
    @Test
    void shouldThrowTestPresetExceptionWhenGetTestPresetRecordsByIdIsNotFound()
    {
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(TestingPresetServiceException.class,
                () -> testPresetService.getTestPresetDetails(anyLong()));
    }

    /**
     *  2.1 GetAllTestPresets -- positive case
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetAllTestPresets()
    {
        when(testPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(Arrays.asList(getTestPreset())));
        testPresetService.getAllTestPresets(0, 1);
        verify(testPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 2.2 GetAllTestPresets -- if no content return empty map
     * empty map will be return
     *
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetEmptyMapWhenAllTestPresetHavingNoData()
    {
        when(testPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        testPresetService.getAllTestPresets(0, 1);
        verify(testPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 3.1 CreateTestPreset (with no other details)
     * with no testPreset
     */
    @Test
    public void shouldCreateTestPreset()
    {
        TestPreset testPreset = getTestPreset();
        TestPresetResponseDTO testPresetResponseDTO = getTestPresetResponseDTO();

        when(testPresetRepository.save(any(TestPreset.class))).thenReturn(testPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));

        when(presetMapperService.mapTestPresetToDTO(testPreset)).thenReturn(testPresetResponseDTO);

        testPresetService.createTestPreset(getTestPresetRequestDto());
        assertEquals(testPresetService.createTestPreset(getTestPresetRequestDto()), testPresetResponseDTO);
    }

    /**
     * 3.2 CreateTestPreset (with other details)
     * with testPreset
     */
    @Test
    public void shouldCreateTestPresetWithOtherDetails()
    {

        TestPreset testPreset = getTestPreset();
        testPreset.setGitPreset(getGitPreset());
        testPreset.setJenkinsPreset(getJenkinsPreset());
        testPreset.setExecutionPreset(getExecutionPreset());
        testPreset.setNotificationPreset(getNotificationPreset());

        TestPresetRequestDTO testPresetRequestDTO = getTestPresetRequestDto();
        testPresetRequestDTO.setGitPresetId(1L);
        testPresetRequestDTO.setJenkinsPresetId(1L);
        testPresetRequestDTO.setExecutionPresetId(1L);
        testPresetRequestDTO.setNotificationPresetId(1L);
        testPresetRequestDTO.setExecutionPresetId(1L);

        TestPresetResponseDTO testPresetResponseDTO = getTestPresetResponseDTO();
        testPresetResponseDTO.setGitPreset(getGitPresetResponseDTO());
        testPresetResponseDTO.setJenkinsPreset(getJenkinsPresetResponseDTO());
        testPresetResponseDTO.setExecutionPreset(getExecutionPresetResponseDTO());
        testPresetResponseDTO.setNotificationPreset(getNotificationPresetResponseDTO());
        testPresetResponseDTO.setExecutionPreset(getExecutionPresetResponseDTO());


        when(testPresetRepository.save(any(TestPreset.class))).thenReturn(testPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));

        when(gitPresetRepository.findById(anyLong())).thenReturn(Optional.of(getGitPreset()));
        when(jenkinsPresetRepository.findById(anyLong())).thenReturn(Optional.of(getJenkinsPreset()));
        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(getExecutionPreset()));
        when(notificationPresetRepository.findById(anyLong())).thenReturn(Optional.of(getNotificationPreset()));

        when(presetMapperService.mapTestPresetToDTO(any(TestPreset.class))).thenReturn(testPresetResponseDTO);

        assertEquals(testPresetService.createTestPreset(testPresetRequestDTO), testPresetResponseDTO);
    }

    /**
     * 4.1 UpdateTestPreset -- positive case (no other details)
     * no test preset
     */
    @Test
    public void shouldUpdateTestPreset()
    {
        TestPreset testPreset = getTestPreset();
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testPreset));
        when(testPresetRepository.save(Mockito.any(TestPreset.class))).thenReturn(testPreset);
        testPresetService.updateTestPreset(anyLong(), getTestPresetRequestDto());
        verify(testPresetRepository, times(1)).save(testPreset);
    }

    /**
     * 4.2 UpdateTestPreset -- positive case (with other details)
     * with test preset
     */
    @Test
    public void shouldUpdateTestPresetWithOtherDetails()
    {
        TestPreset testPreset = getTestPreset();
        testPreset.setGitPreset(getGitPreset());
        testPreset.setJenkinsPreset(getJenkinsPreset());
        testPreset.setExecutionPreset(getExecutionPreset());
        testPreset.setNotificationPreset(getNotificationPreset());

        TestPresetRequestDTO testPresetRequestDTO = getTestPresetRequestDto();
        testPresetRequestDTO.setGitPresetId(1L);
        testPresetRequestDTO.setJenkinsPresetId(1L);
        testPresetRequestDTO.setExecutionPresetId(1L);
        testPresetRequestDTO.setNotificationPresetId(1L);
        testPresetRequestDTO.setExecutionPresetId(1L);

        TestPresetResponseDTO testPresetResponseDTO = getTestPresetResponseDTO();
        testPresetResponseDTO.setGitPreset(getGitPresetResponseDTO());
        testPresetResponseDTO.setJenkinsPreset(getJenkinsPresetResponseDTO());
        testPresetResponseDTO.setExecutionPreset(getExecutionPresetResponseDTO());
        testPresetResponseDTO.setNotificationPreset(getNotificationPresetResponseDTO());
        testPresetResponseDTO.setExecutionPreset(getExecutionPresetResponseDTO());

        when(testPresetRepository.save(any(TestPreset.class))).thenReturn(testPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));

        when(gitPresetRepository.findById(anyLong())).thenReturn(Optional.of(getGitPreset()));
        when(jenkinsPresetRepository.findById(anyLong())).thenReturn(Optional.of(getJenkinsPreset()));
        when(executionPresetRepository.findById(anyLong())).thenReturn(Optional.of(getExecutionPreset()));
        when(notificationPresetRepository.findById(anyLong())).thenReturn(Optional.of(getNotificationPreset()));

        testPresetService.updateTestPreset(anyLong(), testPresetRequestDTO);
        verify(testPresetRepository, times(1)).save(testPreset);
    }

    /**
     *  4.3 UpdateTestPreset -- negative case (id not found)
     * give TestPresetServiceException by find by id
     */
    @Test
    void shouldThrowTestPresetExceptionWhenGetUpdateTestPresetByIdIsNotFound()
    {
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(TestingPresetServiceException.class,
                () -> testPresetService.updateTestPreset(anyLong(), getTestPresetRequestDto()));
    }

    /**
     * 5.1 DeleteTestPreset -- positive case
     */
    @Test
    public void shouldDeleteTestPreset()
    {
        doNothing().when(testPresetRepository).deleteById(anyLong());
        testPresetService.deleteTestPreset(anyLong());
        verify(testPresetRepository, times(1)).deleteById(anyLong());
    }

    /**
     * 5.2 DeleteTestPreset -- negative case (throw error)
     */
    @Test
    void shouldThrowTestPresetExceptionWhenGetDeleteTestPresetByIdIsNotFound()
    {
        doThrow(EmptyResultDataAccessException.class).when(testPresetRepository).deleteById(anyLong());
        assertThrows(TestingPresetServiceException.class,
                () -> testPresetService.deleteTestPreset(anyLong()));
    }


    private TestPreset getTestPreset(){
        return TestPreset.builder()
                .name("Name of Jenkins Preset")
                .parallelBuilds(2)
                .build();
    }

    private TestPresetRequestDTO getTestPresetRequestDto(){
        return TestPresetRequestDTO.builder()
                .name("Name of Jenkins Preset")
                .parallelBuilds(2)
                .build();
    }


    private ExecutionPreset getExecutionPreset(){
        return ExecutionPreset.builder()
                .name("Testing Execution Preset Name")
                .mavenConfigParams(new HashSet<>())
//                .testPresets()
                .build();
    }

    private GitPreset getGitPreset() {
        return GitPreset.builder()
                .repoUrl("https://gitlab.mynisum.com/orazi/tapdemo")
                .userName("orazi")
                .name("orazi")
                .accessToken("yKUKdVHjnN9W9nH15WvU")
                .branch("devellop")
                .build();
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

    private NotificationPreset getNotificationPreset(){
        return NotificationPreset.builder()
                .name("Name of Jenkins Preset")
                .presetConfig("Preset Config of Jenkins Preset")
                .recipients("Recipients of Jenkins Preset")
                .build();
    }

    private TestPresetResponseDTO getTestPresetResponseDTO(){
        return TestPresetResponseDTO.builder()
                .name("Test Preset Name")
                .tags("Tag Preset Name")
                .parallelBuilds(1)
//                .testPresets()
                .build();
    }

    private ExecutionPresetResponseDTO getExecutionPresetResponseDTO(){
        return ExecutionPresetResponseDTO.builder()
                .name("Testing Execution Preset Name")
                .mavenConfigParams(new HashSet<>())
//                .testPresets()
                .build();
    }

    private GitPresetResponseDTO getGitPresetResponseDTO() {
        return GitPresetResponseDTO.builder()
                .repoUrl("https://gitlab.mynisum.com/orazi/tapdemo")
                .userName("orazi")
                .name("orazi")
                .accessToken("yKUKdVHjnN9W9nH15WvU")
                .branch("devellop")
                .build();
    }

    private JenkinsPresetResponseDTO getJenkinsPresetResponseDTO(){
        return JenkinsPresetResponseDTO.builder()
                .name("Name of Jenkins Preset")
                .url("URL of Jenkins Preset")
                .password("Password of Jenkins Preset")
                .credentialId("Credential Id of Jenkins Preset")
                .slave("Slave of Jenkins Preset")
                .build();
    }

    private NotificationPresetResponseDTO getNotificationPresetResponseDTO(){
        return NotificationPresetResponseDTO.builder()
                .name("Name of Jenkins Preset")
                .presetConfig("Preset Config of Jenkins Preset")
                .recipients("Recipients of Jenkins Preset")
                .build();
    }

}
