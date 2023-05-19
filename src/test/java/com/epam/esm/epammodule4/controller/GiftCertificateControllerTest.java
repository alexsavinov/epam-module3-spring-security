package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.controller.advice.ApplicationControllerAdvice;
import com.epam.esm.epammodule4.exception.GiftCertificateNotFoundException;
import com.epam.esm.epammodule4.model.dto.*;
import com.epam.esm.epammodule4.model.dto.request.CreateGiftCertificateRequest;
import com.epam.esm.epammodule4.model.dto.request.SearchGiftCertificateRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateGiftCertificateRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.service.GiftCertificateService;
import com.epam.esm.epammodule4.service.mapper.GiftCertificateMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GiftCertificateControllerTest {

    private static final Long CERTIFICATE_ID = 1L;
    private static final Long TAG_ID = 1L;
    @InjectMocks
    private GiftCertificateController subject;
    @Mock
    private GiftCertificateService certificateService;
    @Mock
    private GiftCertificateMapper certificateMapper;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subject)
                .setControllerAdvice(new ApplicationControllerAdvice())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getGiftCertificateById() throws Exception {
        GiftCertificate foundCertificate = new GiftCertificate();

        List<Tag> expectedTags = new ArrayList<>();
        expectedTags.add(Tag.builder().id(TAG_ID).name("tag1").build());

        List<TagDto> expectedTagsDto = new ArrayList<>();
        expectedTagsDto.add(new TagDto(TAG_ID, "tag1"));

        GiftCertificateDto certificateDto = GiftCertificateDto.builder()
                .tags(expectedTagsDto)
                .id(CERTIFICATE_ID)
                .name("myGiftCertificate")
                .build();

        when(certificateService.findById(any(Long.class))).thenReturn(foundCertificate);
        when(certificateMapper.toDto(any(GiftCertificate.class))).thenReturn(certificateDto);

        mockMvc.perform(
                        get("/certificates/{id}", CERTIFICATE_ID)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(certificateDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(certificateDto.getName()))
                .andExpect(jsonPath("$.description").value(certificateDto.getDescription()))
                .andExpect(jsonPath("$.price").value(certificateDto.getPrice()))
                .andExpect(jsonPath("$.duration").value(certificateDto.getDuration()));

        verify(certificateService).findById(CERTIFICATE_ID);
        verify(certificateMapper).toDto(foundCertificate);
        verifyNoMoreInteractions(certificateService, certificateMapper);
    }

    @Test
    void getGiftCertificateById_whenGiftCertificateNotFoundExceptionIsThrows_returns404() throws Exception {
        when(certificateService.findById(any(Long.class)))
                .thenThrow(new GiftCertificateNotFoundException("GiftCertificate not found"));

        mockMvc.perform(
                        get("/certificates/{id}", CERTIFICATE_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("GiftCertificate not found"));

        verify(certificateService).findById(CERTIFICATE_ID);
        verifyNoInteractions(certificateMapper);
        verifyNoMoreInteractions(certificateService);
    }

    @Test
    void getAllGiftCertificates() throws Exception {
        List<GiftCertificate> expectedCertificates = List.of(new GiftCertificate());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<GiftCertificate> pageableCertificates =
                new PageImpl(expectedCertificates, pageable, expectedCertificates.size());

        when(certificateService.findAll(any(Pageable.class))).thenReturn(pageableCertificates);

        mockMvc.perform(
                        get("/certificates")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "name,asc")
                )
                .andExpect(status().isOk());

        verify(certificateService).findAll(pageable);
        verifyNoMoreInteractions(certificateService);
    }

    @Test
    void addGiftCertificate() throws Exception {
        GiftCertificate expectedCertificate = new GiftCertificate();
        GiftCertificateDto certificateDto = GiftCertificateDto.builder()
                .id(CERTIFICATE_ID)
                .name("myGiftCertificate")
                .build();

        when(certificateService.create(any(CreateGiftCertificateRequest.class))).thenReturn(expectedCertificate);
        when(certificateMapper.toDto(any(GiftCertificate.class))).thenReturn(certificateDto);

        RequestBuilder requestBuilder = post("/certificates")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(expectedCertificate))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(certificateDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(certificateDto.getName()))
                .andExpect(jsonPath("$.description").value(certificateDto.getDescription()))
                .andExpect(jsonPath("$.price").value(certificateDto.getPrice()))
                .andExpect(jsonPath("$.duration").value(certificateDto.getDuration()));

        verify(certificateMapper).toDto(expectedCertificate);
        verifyNoMoreInteractions(certificateService, certificateMapper);
    }

    @Test
    void updateGiftCertificate() throws Exception {
        GiftCertificate expectedCertificate = new GiftCertificate();
        GiftCertificateDto certificateDto = GiftCertificateDto.builder()
                .id(CERTIFICATE_ID)
                .name("myGiftCertificate")
                .build();

        when(certificateService.update(any(UpdateGiftCertificateRequest.class))).thenReturn(expectedCertificate);
        when(certificateMapper.toDto(any(GiftCertificate.class))).thenReturn(certificateDto);

        RequestBuilder requestBuilder = patch("/certificates")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(expectedCertificate))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(certificateDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(certificateDto.getName()))
                .andExpect(jsonPath("$.description").value(certificateDto.getDescription()))
                .andExpect(jsonPath("$.price").value(certificateDto.getPrice()))
                .andExpect(jsonPath("$.duration").value(certificateDto.getDuration()));

        verify(certificateMapper).toDto(expectedCertificate);
        verifyNoMoreInteractions(certificateService, certificateMapper);
    }

    @Test
    void deleteGiftCertificateById() throws Exception {
        RequestBuilder requestBuilder = delete("/certificates/{id}", CERTIFICATE_ID);

        mockMvc.perform(requestBuilder).andExpect(status().isNoContent());

        verify(certificateService).delete(CERTIFICATE_ID);
        verifyNoMoreInteractions(certificateService);
    }

    @Test
    void searchCertificatesWithSearchParams() throws Exception {
        GiftCertificate expectedCertificate = new GiftCertificate();
        List<GiftCertificate> expectedCertificates = List.of(expectedCertificate);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<GiftCertificate> pageableCertificates =
                new PageImpl(expectedCertificates, pageable, expectedCertificates.size());

        when(certificateService.findCertificateWithSearchParams(
                any(Pageable.class),
                any(SearchGiftCertificateRequest.class)
        )).thenReturn(pageableCertificates);

        mockMvc.perform(
                        get("/certificates/search")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "name,asc")
                )
                .andExpect(status().isOk());

        verify(certificateMapper).toDto(expectedCertificate);
        verifyNoMoreInteractions(certificateService, certificateMapper);
    }
}