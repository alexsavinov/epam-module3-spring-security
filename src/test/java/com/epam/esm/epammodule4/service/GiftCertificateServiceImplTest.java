package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.exception.GiftCertificateNotFoundException;
import com.epam.esm.epammodule4.model.dto.request.CreateGiftCertificateRequest;
import com.epam.esm.epammodule4.model.dto.request.CreateTagRequest;
import com.epam.esm.epammodule4.model.dto.request.SearchGiftCertificateRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateGiftCertificateRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.repository.GiftCertificateRepository;
import com.epam.esm.epammodule4.repository.PageableGiftCertificateRepository;
import com.epam.esm.epammodule4.service.implementation.GiftCertificateServiceImpl;
import com.epam.esm.epammodule4.service.implementation.TagServiceImpl;
import com.epam.esm.epammodule4.service.mapper.GiftCertificateMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftCertificateServiceImplTest {

    private static final Long CERTIFICATE_ID = 1L;
    private static final Long TAG_ID = 1L;
    @InjectMocks
    private GiftCertificateServiceImpl subject;
    @Mock
    private GiftCertificateRepository certificateRepository;
    @Mock
    private PageableGiftCertificateRepository pageableCertificateRepository;
    @Mock
    private TagServiceImpl tagService;
    @Mock
    private GiftCertificateMapper giftCertificateMapper;
    @Mock
    private CreateGiftCertificateRequest createRequest;
    @Mock
    private UpdateGiftCertificateRequest updateRequest;


    @Test
    void findById() {
        GiftCertificate expectedGiftCertificate = new GiftCertificate();

        when(certificateRepository.findById(any(Long.class))).thenReturn(Optional.of(expectedGiftCertificate));

        GiftCertificate actualGiftCertificate = subject.findById(CERTIFICATE_ID);

        verify(certificateRepository).findById(CERTIFICATE_ID);
        verifyNoMoreInteractions(certificateRepository);

        assertThat(actualGiftCertificate).isEqualTo(expectedGiftCertificate);
    }

    @Test
    void findById_whenGiftCertificateIsNotFoundById_throwsGiftCertificateNotFoundException() {
        String expectedMessage = "Requested resource not found (id = %s)".formatted(CERTIFICATE_ID);

        when(certificateRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        GiftCertificateNotFoundException exception = assertThrows(GiftCertificateNotFoundException.class,
                () -> subject.findById(CERTIFICATE_ID));

        verify(certificateRepository).findById(CERTIFICATE_ID);
        verifyNoMoreInteractions(certificateRepository);

        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void findAll() {
        List<GiftCertificate> expectedCertificates = List.of(new GiftCertificate());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<GiftCertificate> pageableExpectedCertificates =
                new PageImpl(expectedCertificates, pageable, expectedCertificates.size());

        when(pageableCertificateRepository.findAll(any(Pageable.class))).thenReturn(pageableExpectedCertificates);

        Page<GiftCertificate> actualGiftCertificates = subject.findAll(pageable);

        verify(pageableCertificateRepository).findAll(pageable);
        verifyNoMoreInteractions(pageableCertificateRepository);

        assertThat(actualGiftCertificates).isEqualTo(pageableExpectedCertificates);
    }

    @Test
    void create() {
        GiftCertificate newCertificate = new GiftCertificate();
        GiftCertificate createdCertificate = new GiftCertificate();

        when(giftCertificateMapper.toCertificate(any(CreateGiftCertificateRequest.class))).thenReturn(newCertificate);
        when(certificateRepository.save(any(GiftCertificate.class))).thenReturn(createdCertificate);

        GiftCertificate actualGiftCertificate = subject.create(createRequest);

        verify(certificateRepository).save(newCertificate);
        verify(giftCertificateMapper).toCertificate(createRequest);
        verifyNoMoreInteractions(certificateRepository);

        assertThat(actualGiftCertificate).isEqualTo(createdCertificate);
    }

    @Test
    void create_whenTagsNotFound_createTags() {
        Tag createdTag = Tag.builder().id(TAG_ID).name("myTag").build();
        List<Tag> createdTags = List.of(createdTag);

        GiftCertificate expectedCertificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("name1")
                .description("description1")
                .price(11.22)
                .duration(5)
                .tags(createdTags)
                .build();
        GiftCertificate newCertificate = GiftCertificate.builder()
                .name("name1")
                .description("description1")
                .price(11.22)
                .duration(5)
                .tags(createdTags)
                .build();

        when(giftCertificateMapper.toCertificate(any(CreateGiftCertificateRequest.class))).thenReturn(newCertificate);
        when(certificateRepository.save(any(GiftCertificate.class))).thenReturn(expectedCertificate);
        when(tagService.createTagWithCheck(any(CreateTagRequest.class))).thenReturn(createdTag);

        GiftCertificate actualGiftCertificate = subject.create(createRequest);

        verify(giftCertificateMapper).toCertificate(createRequest);
        verify(certificateRepository).save(newCertificate);
        verifyNoMoreInteractions(certificateRepository);

        assertThat(actualGiftCertificate).isEqualTo(expectedCertificate);
    }

    @Test
    void update() {
        GiftCertificate updateCertificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("name1")
                .description("description1")
                .price(11.22)
                .duration(5)
                .build();
        GiftCertificate expectedCertificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("name1")
                .description("description1")
                .price(11.22)
                .duration(5)
                .build();

        when(updateRequest.getName()).thenReturn("name1");
        when(updateRequest.getDescription()).thenReturn("description1");
        when(updateRequest.getPrice()).thenReturn(11.22);
        when(updateRequest.getDuration()).thenReturn(5);
        when(certificateRepository.findById(any(Long.class))).thenReturn(Optional.of(updateCertificate));
        when(certificateRepository.save(any(GiftCertificate.class))).thenReturn(expectedCertificate);

        GiftCertificate actualGiftCertificate = subject.update(updateRequest);

        verify(certificateRepository).save(updateCertificate);
        verifyNoMoreInteractions(certificateRepository);

        assertThat(actualGiftCertificate).isEqualTo(expectedCertificate);
    }

    @Test
    void delete() {
        GiftCertificate deleteGiftCertificate = GiftCertificate.builder()
                .id(CERTIFICATE_ID)
                .name("name1")
                .description("description1")
                .price(11.22)
                .duration(5)
                .build();

        when(certificateRepository.findById(any(Long.class))).thenReturn(Optional.of(deleteGiftCertificate));

        subject.delete(CERTIFICATE_ID);

        verify(certificateRepository).delete(deleteGiftCertificate);
        verifyNoMoreInteractions(certificateRepository);
    }

    @Test
    void findCertificateWithSearchParams() {
        List<GiftCertificate> expectedCertificates = List.of(new GiftCertificate());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<GiftCertificate> pageableExpectedCertificates =
                new PageImpl(expectedCertificates, pageable, expectedCertificates.size());


        SearchGiftCertificateRequest searchCertificateRequest = SearchGiftCertificateRequest.builder()
                .name("cert1")
                .description("description1")
                .tags(List.of("tag1"))
                .build();

        when(pageableCertificateRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageableExpectedCertificates);

        Page<GiftCertificate> actualGiftCertificates =
                subject.findCertificateWithSearchParams(pageable, searchCertificateRequest);

        verifyNoMoreInteractions(pageableCertificateRepository);

        assertThat(actualGiftCertificates).isEqualTo(pageableExpectedCertificates);
    }
}