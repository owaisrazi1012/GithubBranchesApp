package com.nisum.unit.service.preset;

import com.nisum.api.preset.domain.dto.NotificationPresetRequestDTO;
import com.nisum.api.preset.domain.entity.NotificationPreset;
import com.nisum.api.preset.domain.entity.TestPreset;
import com.nisum.exception.custom.NotificationPresetServiceException;
import com.nisum.api.preset.repository.NotificationPresetRepository;
import com.nisum.api.preset.repository.TestPresetRepository;
import com.nisum.api.preset.service.impl.NotificationPresetServiceImpl;
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
public class NotificationPresetServiceTest {

    @InjectMocks
    NotificationPresetServiceImpl notificationPresetService;

    @Mock
    private NotificationPresetRepository notificationPresetRepository;

    @Mock
    private TestPresetRepository testPresetRepository;

    /**
     * Test Cases Tree
     *  1. GetNotificationPresetsRecordsById
     *      1.1 GetNotificationPresetsRecordsById -- positive case
     *      1.2 GetNotificationPresetsRecordsById -- negative case (throw error)
     *
     *  2. GetAllNotificationPresets (pageable --> page, size)
     *     2.1 GetAllNotificationPresets -- positive case
     *     2.2 GetAllNotificationPresets -- if no content return empty map
     *
     *  3. CreateNotificationPreset
     *     3.1 CreateNotificationPreset (with no testPreset)
     *     3.2 CreateNotificationPreset (with testPreset)
     *
     *  4. UpdateNotificationPreset
     *     4.1 UpdateNotificationPreset -- positive case (no test preset)
     *     4.2 UpdateNotificationPreset -- positive case (with test preset)
     *     4.3 UpdateNotificationPreset -- negative case (id not found)
     *
     *  5. DeleteNotificationPreset
     *     5.1 DeleteNotificationPreset -- positive case
     *     5.2 DeleteNotificationPreset -- negative case (throw error)
     */


    /**
     * 1.1 GetNotificationPresetsRecordsById -- positive case
     */
    @Test
    public void shouldGetNotificationPresetRecordsById()
    {
        when(notificationPresetRepository.findById(anyLong())).thenReturn(Optional.of(getNotificationPreset()));
        notificationPresetService.getNotificationPresetDetails(1L);
        verify(notificationPresetRepository, times(1)).findById(anyLong());
    }

    /**
     * 1.2 GetNotificationPresetsRecordsById -- negative case (throw error)
     * ExecutionServiceException will be thrown
     * no content exception is expected this will be wrapped in ExecutionServiceException
     */
    @Test
    void shouldThrowNotificationPresetExceptionWhenGetNotificationPresetRecordsByIdIsNotFound()
    {
        when(notificationPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotificationPresetServiceException.class,
                () -> notificationPresetService.getNotificationPresetDetails(anyLong()));
    }

    /**
     *  2.1 GetAllNotificationPresets -- positive case
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetAllNotificationPresets()
    {
        when(notificationPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(Arrays.asList(getNotificationPreset())));
        notificationPresetService.getAllNotificationPresets(0, 1);
        verify(notificationPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 2.2 GetAllNotificationPresets -- if no content return empty map
     * empty map will be return
     *
     * don't use anyInt because it might be negative
     * page and size must be positive integer
     */
    @Test
    public void shouldGetEmptyMapWhenAllNotificationPresetHavingNoData()
    {
        when(notificationPresetRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending())))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        notificationPresetService.getAllNotificationPresets(0, 1);
        verify(notificationPresetRepository, times(1))
                .findAll(PageRequest.of(0, 1, Sort.by("id").descending()));
    }

    /**
     * 3.1 CreateNotificationPreset (with no testPreset)
     * with no testPreset
     */
    @Test
    public void shouldCreateNotificationPreset()
    {
        when(notificationPresetRepository.save(Mockito.any(NotificationPreset.class))).thenReturn(getNotificationPreset());
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(mock(TestPreset.class)));
        notificationPresetService.createNotificationPreset(getNotificationPresetRequestDto());
        verify(notificationPresetRepository, times(1)).save(getNotificationPreset());
    }

    /**
     * 3.2 CreateNotificationPreset (with testPreset)
     * with testPreset
     */
    @Test
    public void shouldCreateNotificationPresetWithTestPreset()
    {
        NotificationPresetRequestDTO notificationPresetRequestDTO = getNotificationPresetRequestDto();
        TestPreset testPreset = mock(TestPreset.class);
        NotificationPreset notificationPreset = getNotificationPreset();
        setTestPresetValuesInObjects(notificationPresetRequestDTO, notificationPreset, testPreset);

        when(notificationPresetRepository.save(Mockito.any(NotificationPreset.class))).thenReturn(notificationPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        notificationPresetService.createNotificationPreset(notificationPresetRequestDTO);
        verify(notificationPresetRepository, times(1)).save(notificationPreset);
    }

    /**
     * 4.1 UpdateNotificationPreset -- positive case (no test preset)
     * no test preset
     */
    @Test
    public void shouldUpdateNotificationPreset()
    {
        when(notificationPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(getNotificationPreset()));
        when(notificationPresetRepository.save(Mockito.any(NotificationPreset.class))).thenReturn(getNotificationPreset());
        notificationPresetService.updateNotificationPreset(anyLong(), getNotificationPresetRequestDto());
        verify(notificationPresetRepository, times(1)).save(getNotificationPreset());
    }

    /**
     * 4.2 UpdateNotificationPreset -- positive case (with test preset)
     * with test preset
     */
    @Test
    public void shouldUpdateNotificationPresetWithTestingPresetDetails()
    {
        NotificationPresetRequestDTO notificationPresetRequestDTO = getNotificationPresetRequestDto();
        TestPreset testPreset = mock(TestPreset.class);
        NotificationPreset notificationPreset = getNotificationPreset();
        setTestPresetValuesInObjects(notificationPresetRequestDTO, notificationPreset, testPreset);

        when(notificationPresetRepository.findById(anyLong())).thenReturn(Optional.ofNullable(notificationPreset));
        when(notificationPresetRepository.save(Mockito.any(NotificationPreset.class))).thenReturn(notificationPreset);
        when(testPresetRepository.findById(anyLong())).thenReturn(Optional.of(testPreset));
        notificationPresetService.updateNotificationPreset(anyLong(), notificationPresetRequestDTO);
        verify(notificationPresetRepository, times(1)).save(notificationPreset);
    }

    /**
     * 4.3 UpdateNotificationPreset -- negative case (id not found)
     * give NotificationPresetServiceException by find by id
     */
    @Test
    void shouldThrowNotificationPresetExceptionWhenGetUpdateNotificationPresetByIdIsNotFound()
    {
        when(notificationPresetRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotificationPresetServiceException.class,
                () -> notificationPresetService.updateNotificationPreset(anyLong(), getNotificationPresetRequestDto()));
    }

    /**
     * 5.1 DeleteNotificationPreset -- positive case
     */
    @Test
    public void shouldDeleteNotificationPreset()
    {
        doNothing().when(notificationPresetRepository).deleteById(anyLong());
        notificationPresetService.deleteNotificationPreset(anyLong());
        verify(notificationPresetRepository, times(1)).deleteById(anyLong());
    }

    /**
     * 5.2 DeleteNotificationPreset -- negative case (throw error)
     */
    @Test
    void shouldThrowNotificationPresetExceptionWhenGetDeleteNotificationPresetByIdIsNotFound()
    {
        doThrow(EmptyResultDataAccessException.class).when(notificationPresetRepository).deleteById(anyLong());
        assertThrows(NotificationPresetServiceException.class,
                () -> notificationPresetService.deleteNotificationPreset(anyLong()));
    }


    private NotificationPreset getNotificationPreset(){
        return NotificationPreset.builder()
                .name("Name of Jenkins Preset")
                .presetConfig("Preset Config of Jenkins Preset")
                .recipients("Recipients of Jenkins Preset")
                .build();
    }

    private NotificationPresetRequestDTO getNotificationPresetRequestDto(){
        return NotificationPresetRequestDTO.builder()
                .name("Name of Jenkins Preset")
                .presetConfig("Preset Config of Jenkins Preset")
                .recipients("Recipients of Jenkins Preset")
                .build();
    }

    private void setTestPresetValuesInObjects(NotificationPresetRequestDTO notificationPresetRequestDTO,
                                              NotificationPreset notificationPreset,
                                              TestPreset testPreset
    ) {
        Set<TestPreset> testPresetSet = new HashSet<>();
        testPreset.setId(1L);
        testPresetSet.add(testPreset);

        notificationPresetRequestDTO.setTestPresetId(1L);
        notificationPreset.setTestPresets(new HashSet<TestPreset>(testPresetSet));


    }


}
