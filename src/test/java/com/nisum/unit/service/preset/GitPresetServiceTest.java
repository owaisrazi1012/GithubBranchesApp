package com.nisum.unit.service.preset;

import com.nisum.api.preset.domain.dto.GitPresetRequestDTO;
import com.nisum.api.preset.domain.entity.GitPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import com.nisum.api.preset.repository.GitPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.impl.GitPresetServiceImpl;
import com.nisum.exception.custom.GitPresetServiceException;
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
import static org.mockito.Mockito.*;

@SpringBootTest
public class GitPresetServiceTest {


    @InjectMocks
    GitPresetServiceImpl gitPresetService;

    @Mock
    GitPresetRepository gitPresetRepository;

    @Mock
    TestPresetRepository testPresetRepository;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void shouldGetGitPresetsRecordsById()
    {
        when(gitPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getGitPreset()));
        gitPresetService.getGitPresetDetails(1L);
        verify(gitPresetRepository, times(1)).findById(anyLong());
    }

    @Test
    public void shouldGetAllGitPresets()
    {
        when(gitPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(Arrays.asList(getGitPreset())));
        gitPresetService.getAllGitPresets(0, 1);
        verify(gitPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    @Test
    public void shouldGetEmptyMapWhenAllGitPresetsHavingNoData()
    {
        when(gitPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        gitPresetService.getAllGitPresets(0, 1);
        verify(gitPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }


    @Test
    public void shouldCreateGitPreset()
    {
        when(gitPresetRepository.save(Mockito.any(GitPreset.class))).thenReturn(getGitPreset());
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(mock(TestPreset.class)));
        gitPresetService.createGitPreset(getGitPresetRequestDto());
        verify(gitPresetRepository, times(1)).save(getGitPreset());
    }

    @Test
    public void shouldCreateGitPresetWithTestingPresetDetails()
    {
        GitPresetRequestDTO gitPresetRequestDTO = getGitPresetRequestDto();
        TestPreset testPreset = mock(TestPreset.class);
        GitPreset gitPreset = getGitPreset();
        setObjectValuesForSaveOperation(gitPresetRequestDTO, testPreset, gitPreset);

        when(gitPresetRepository.save(Mockito.any(GitPreset.class))).thenReturn(gitPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));

        gitPresetService.createGitPreset(gitPresetRequestDTO);
        verify(gitPresetRepository, times(1)).save(gitPreset);
    }


    @Test
    public void shouldUpdateGitPreset()
    {
        when(gitPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getGitPreset()));
        when(gitPresetRepository.save(Mockito.any(GitPreset.class))).thenReturn(getGitPreset());
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(mock(TestPreset.class)));
        gitPresetService.updateGitPreset(anyLong(), getGitPresetRequestDto());
        verify(gitPresetRepository, times(1)).save(getGitPreset());
    }

    @Test
    public void shouldUpdateGitPresetWithTestingPresetDetails()
    {
        GitPresetRequestDTO gitPresetRequestDTO = getGitPresetRequestDto();
        TestPreset testPreset = mock(TestPreset.class);
        GitPreset gitPreset = getGitPreset();
        setObjectValuesForSaveOperation(gitPresetRequestDTO, testPreset, gitPreset);

        when(gitPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(gitPreset));
        when(gitPresetRepository.save(Mockito.any(GitPreset.class))).thenReturn(gitPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        gitPresetService.updateGitPreset(anyLong(), gitPresetRequestDTO);
        verify(gitPresetRepository, times(1)).save(gitPreset);
    }

    @Test
    public void shouldDeleteGitPreset()
    {
        doNothing().when(gitPresetRepository).deleteById(anyLong());
        gitPresetService.deleteGitPreset(anyLong());
        verify(gitPresetRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void shouldThrowGitPresetExceptionWhenGetGitPresetsRecordsByIdIsNotFound()
    {
        when(gitPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(GitPresetServiceException.class,
                () -> gitPresetService.getGitPresetDetails(anyLong()));
    }

    @Test
    void shouldThrowGitPresetExceptionWhenGetUpdateGitPresetByIdIsNotFound()
    {
        when(gitPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(GitPresetServiceException.class,
                () -> gitPresetService.updateGitPreset(anyLong(), getGitPresetRequestDto()));
    }

    @Test
    void shouldThrowGitPresetExceptionWhenGetDeleteGitPresetByIdIsNotFound()
    {
        doThrow(EmptyResultDataAccessException.class).when(gitPresetRepository).deleteById(anyLong());
        assertThrows(GitPresetServiceException.class,
                () -> gitPresetService.deleteGitPreset(anyLong()));
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

    private GitPresetRequestDTO getGitPresetRequestDto() {
        return GitPresetRequestDTO.builder()
                .repoUrl("https://gitlab.mynisum.com/orazi/tapdemo")
                .userName("orazi")
                .name("orazi")
                .accessToken("yKUKdVHjnN9W9nH15WvU")
                .branch("devellop")
                .build();
    }

    private void setObjectValuesForSaveOperation(GitPresetRequestDTO gitPresetRequestDTO, TestPreset testPreset, GitPreset gitPreset) {
        Set testPresetSet = new HashSet<>();
        testPresetSet.add(testPreset);
        gitPresetRequestDTO.setTestPresetId(1L);
        gitPreset.setTestPresets(testPresetSet);
    }
}
