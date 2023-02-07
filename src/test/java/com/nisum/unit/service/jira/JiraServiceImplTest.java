package com.nisum.unit.service.jira;

import com.nisum.api.preset.domain.entity.JiraPreset;
import com.nisum.jira.domain.dto.Content;
import com.nisum.jira.domain.dto.Description;
import com.nisum.jira.domain.dto.TicketDetail;
import com.nisum.service.impl.JiraServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class JiraServiceImplTest {

    @InjectMocks
    JiraServiceImpl jiraService;

    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    public void init() {
        ReflectionTestUtils.setField(jiraService, "postTicketUrl", "test");
        ReflectionTestUtils.setField(jiraService, "addAttachmentUrl", "test");
        ReflectionTestUtils.setField(jiraService, "addCommentUrl", "test");
        ReflectionTestUtils.setField(jiraService, "getTicketsBySummaryURL", "test");
    }

    @Test
    public void getHttpHeadersTest() {
        JiraPreset jiraPreset = getJiraPreset();
        assertNotNull(jiraService.getHttpHeaders(jiraPreset));
    }

    @Test
    public void createJiraTicketTest() {
        TicketDetail ticketDetail = getTicketDetail();
        JiraPreset jiraPreset = getJiraPreset();
        Mockito.when(restTemplate.exchange(anyString(), any(), any(HttpEntity.class), any(Class.class))).thenReturn(new ResponseEntity(ticketDetail,HttpStatus.CREATED));
        TicketDetail ticketDetail1 = jiraService.createJiraTicket(ticketDetail, jiraPreset);
        assertNull(ticketDetail1.getErrorMessage());
        assertEquals("test", ticketDetail1.getKey());
    }

    @Test
    public void createJiraTicketStatus400BadRequestTest() {
        TicketDetail ticketDetail = getTicketDetail();
        JiraPreset jiraPreset = getJiraPreset();
        Mockito.when(restTemplate.exchange(anyString(), any(), any(HttpEntity.class), any(Class.class))).thenReturn(new ResponseEntity(ticketDetail,HttpStatus.BAD_REQUEST));
        TicketDetail ticketDetail1 = jiraService.createJiraTicket(ticketDetail, jiraPreset);
        assertEquals("400 BAD_REQUEST",ticketDetail1.getErrorMessage());
        assertNull( ticketDetail1.getKey());
    }

    @Test
    public void createJiraTicketCommentTest() {
        TicketDetail ticketDetail = getTicketDetail();
        JiraPreset jiraPreset = getJiraPreset();
        Mockito.when(restTemplate.exchange(anyString(), any(), any(HttpEntity.class), any(Class.class),anyString())).thenReturn(new ResponseEntity(ticketDetail,HttpStatus.OK));
        TicketDetail ticketDetail1 = jiraService.addCommentToTicket(ticketDetail, jiraPreset);
        assertNull(ticketDetail1.getErrorMessage());
        assertEquals( "test",ticketDetail1.getKey());
    }

    public TicketDetail getTicketDetail() {
        TicketDetail ticketDetail = new TicketDetail();
        Description description = new Description();
        Content content = new Content();
        content.setType("test");
        content.setText("test");
        description.setVersion(1);
        description.setType("test");
        ticketDetail.setKey("test");
        ticketDetail.setBody(description);
        ticketDetail.setId("test");
        ticketDetail.setSelf("test");
        return ticketDetail;
    }

    public JiraPreset getJiraPreset() {
        JiraPreset jiraPreset = new JiraPreset();
        jiraPreset.setId(1L);
        jiraPreset.setToken("test");
        jiraPreset.setUserName("test");
        jiraPreset.setUserName("test");
        return jiraPreset;
    }

}
