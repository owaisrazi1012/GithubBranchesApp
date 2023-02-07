package com.nisum.unit.service.preset;

import com.nisum.api.preset.domain.dto.JiraPresetRequestDTO;
import com.nisum.api.preset.domain.entity.JiraPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import com.nisum.api.preset.repository.JiraPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.impl.JiraPresetServiceImpl;
import com.nisum.exception.custom.JiraPresetServiceException;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class JiraPresetServiceTest {

    @InjectMocks
    JiraPresetServiceImpl jiraPresetService;

    @Mock
    JiraPresetRepository jiraPresetRepository;

    @Mock
    TestPresetRepository testPresetRepository;

    @BeforeEach
    public void setUp() {
    }

    /**
     * Test Cases Tree
     *  1. GetAllJiraPresets (pageable --> page, size)
     *     2.1 GetAllJiraPresets -- positive case
     *     2.2 GetAllJiraPresets -- if no content return empty map
     *
     *  2. GetJiraPresetDetails
     *      1.1 GetJiraPresetDetails -- positive case
     *      1.2 GetJiraPresetDetails -- negative case (throw error)
     *
     *  3. CreateJiraPreset
     *      4.1 CreateJiraPreset (with no testPreset)
     *      4.2 CreateJiraPreset (with testPreset)
     *
     *  4. UpdateGitPreset
     *      4.1 UpdateGitPreset (with no testPreset)
     *      4.2 UpdateGitPreset (with testPreset)
     *
     *  5. DeleteJiraPreset
     *      5.1 DeleteJiraPreset
     *      5.2 DeleteJiraPreset -- negative case (throw error)
     */


    /**
     * 1.1 GetAllJiraPresets -- positive case
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetAllJiraPresets() {
        when(jiraPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(Arrays.asList(getJiraPreset())));
        jiraPresetService.getAllJiraPresets(0, 1);
        verify(jiraPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 1.2 GetAllJiraPresets -- if no content return empty map
     * empty map will be return
     *
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetEmptyMapWhenAllJiraPresetHavingNoData() {
        when(jiraPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        jiraPresetService.getAllJiraPresets(0, 1);
        verify(jiraPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 2.1 GetJiraPresetDetails -- positive case
     */
    @Test
    public void shouldGetJiraPresetDetails() {
        when(jiraPresetRepository.findById(anyLong())).thenReturn(Optional.of(getJiraPreset()));
        jiraPresetService.getJiraPresetDetails(1L);
        verify(jiraPresetRepository, times(1)).findById(anyLong());
    }

    /**
     * 2.2 GetJiraPresetDetails -- negative case (throw error)
     * JiraPresetServiceException will be thrown
     * no content exception is expected this will be wrapped in JiraPresetServiceException
     */
    @Test
    void shouldThrowJiraPresetExceptionWhenGetJiraPresetDetailsIsNotFound() {
        when(jiraPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(JiraPresetServiceException.class,
                () -> jiraPresetService.getJiraPresetDetails(anyLong()));
    }

    /**
     * 3.1 CreateJiraPreset (with no testPreset)
     * with no testPreset
     */
    @Test
    public void shouldCreateJiraPreset() {
        when(jiraPresetRepository.save(Mockito.any(JiraPreset.class))).thenReturn(getJiraPreset());
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(mock(TestPreset.class)));
        jiraPresetService.createJiraPreset(getJiraPresetRequestDto());
        verify(jiraPresetRepository, times(1)).save(getJiraPreset());
    }

    /**
     * 3.2 CreateJiraPreset (with testPreset)
     * with testPreset
     */
    @Test
    public void shouldCreateJiraPresetWithTestingPresetDetails() {
        JiraPresetRequestDTO jiraPresetRequestDTO = getJiraPresetRequestDto();
        TestPreset testPreset = mock(TestPreset.class);
        JiraPreset jiraPreset = getJiraPreset();
        setObjectValuesForSaveOperation(jiraPresetRequestDTO, testPreset, jiraPreset);

        when(jiraPresetRepository.save(Mockito.any(JiraPreset.class))).thenReturn(jiraPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));

        jiraPresetService.createJiraPreset(jiraPresetRequestDTO);
        verify(jiraPresetRepository, times(1)).save(jiraPreset);
    }

    /**
     * 4.1 UpdateGitPreset (with no testPreset)
     * no test preset
     */
    @Test
    public void shouldUpdateJiraPreset() {
        when(jiraPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getJiraPreset()));
        when(jiraPresetRepository.save(Mockito.any(JiraPreset.class))).thenReturn(getJiraPreset());
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(mock(TestPreset.class)));
        jiraPresetService.updateJiraPreset(anyLong(), getJiraPresetRequestDto());
        verify(jiraPresetRepository, times(1)).save(getJiraPreset());
    }

    /**
     * 4.2 UpdateGitPreset (with testPreset)
     * with test preset
     */
    @Test
    public void shouldUpdateJiraPresetWithTestingPresetDetails() {
        JiraPresetRequestDTO jiraPresetRequestDTO = getJiraPresetRequestDto();
        TestPreset testPreset = mock(TestPreset.class);
        JiraPreset jiraPreset = getJiraPreset();
        setObjectValuesForSaveOperation(jiraPresetRequestDTO, testPreset, jiraPreset);

        when(jiraPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(jiraPreset));
        when(jiraPresetRepository.save(Mockito.any(JiraPreset.class))).thenReturn(jiraPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        jiraPresetService.updateJiraPreset(anyLong(), jiraPresetRequestDTO);
        verify(jiraPresetRepository, times(1)).save(jiraPreset);
    }

    /**
     * 5.1 DeleteJiraPreset
     */
    @Test
    public void shouldDeleteJiraPreset() {
        doNothing().when(jiraPresetRepository).deleteById(anyLong());
        jiraPresetService.deleteJiraPreset(anyLong());
        verify(jiraPresetRepository, times(1)).deleteById(anyLong());
    }

    /**
     * 5.2 DeleteJiraPreset -- negative case (throw error)
     */
    @Test
    void shouldThrowJiraPresetExceptionWhenJiraPresetIsNotDelete() {
        doThrow(EmptyResultDataAccessException.class).when(jiraPresetRepository).deleteById(anyLong());
        assertThrows(JiraPresetServiceException.class,
                () -> jiraPresetService.deleteJiraPreset(anyLong()));
    }

    private JiraPreset getJiraPreset() {
        return JiraPreset.builder()
                .userName("Username of Jira Preset")
                .token("token")
                .projectId("project-id")
                .build();
    }

    private JiraPresetRequestDTO getJiraPresetRequestDto() {
        return JiraPresetRequestDTO.builder()
                .userName("Username of Jira Preset")
                .token("token")
                .projectId("project-id")
                .build();

    }

    private void setObjectValuesForSaveOperation(JiraPresetRequestDTO jiraPresetRequestDTO, TestPreset testPreset, JiraPreset jiraPreset) {
        Set testPresetSet = new HashSet<>();
        testPresetSet.add(testPreset);
        jiraPresetRequestDTO.setTestPresetId(1L);
        jiraPreset.setTestPresets(testPresetSet);
    }

}
