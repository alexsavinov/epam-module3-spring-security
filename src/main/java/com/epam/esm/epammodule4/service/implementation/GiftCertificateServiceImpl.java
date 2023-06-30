package com.epam.esm.epammodule4.service.implementation;

import com.epam.esm.epammodule4.exception.GiftCertificateNotFoundException;
import com.epam.esm.epammodule4.model.dto.request.CreateGiftCertificateRequest;
import com.epam.esm.epammodule4.model.dto.request.CreateTagRequest;
import com.epam.esm.epammodule4.model.dto.request.SearchGiftCertificateRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateGiftCertificateRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.repository.*;
import com.epam.esm.epammodule4.repository.specification.GiftCertificateSpecification;
import com.epam.esm.epammodule4.service.GiftCertificateService;
import com.epam.esm.epammodule4.service.TagService;
import com.epam.esm.epammodule4.service.mapper.GiftCertificateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftCertificateServiceImpl implements GiftCertificateService {

    private final GiftCertificateRepository certificateRepository;
    private final PageableGiftCertificateRepository pageableCertificateRepository;
    private final TagService tagService;
    private final GiftCertificateMapper certificateMapper;

    @Override
    public GiftCertificate findById(Long id) {
        log.debug("Looking for a gift certificate with id {}", id);

        GiftCertificate foundCertificate = certificateRepository.findById(id)
                .orElseThrow(() -> new GiftCertificateNotFoundException(
                        "Requested resource not found (id = %s)".formatted(id)
                ));

        log.info("Received a gift certificate with id {}", id);
        return foundCertificate;
    }

    @Override
    public Page<GiftCertificate> findAll(Pageable pageable) {
        log.debug("Retrieving gift certificates. Page request: {}", pageable);

        Page<GiftCertificate> foundCertificates = pageableCertificateRepository.findAll(pageable);

        log.info("Retrieved {} gift certificates of {} total",
                foundCertificates.getSize(),
                foundCertificates.getTotalElements());

        return foundCertificates;
    }

    @Override
    @Transactional
    public GiftCertificate create(CreateGiftCertificateRequest createRequest) {
        log.debug("Creating a new certificate");

        GiftCertificate certificate = certificateMapper.toCertificate(createRequest);
        List<Tag> foundTags = certificate.getTags();

        ofNullable(foundTags).ifPresent(certificateTags ->
                certificateTags.forEach(tag -> {
                    Tag createdTag = tagService.createTagWithCheck(new CreateTagRequest(tag.getName()));
                    tag.setId(createdTag.getId());
                }));

        GiftCertificate createdCertificate = certificateRepository.save(certificate);

        log.info("Created a new gift certificate with id {}", createdCertificate.getId());
        return createdCertificate;
    }

    @Override
    @Transactional
    public GiftCertificate update(UpdateGiftCertificateRequest updateRequest) {
        log.debug("Updating a gift certificate with id {}", updateRequest.getId());

        GiftCertificate foundCertificate = findById(updateRequest.getId());

        ofNullable(updateRequest.getName()).ifPresent(foundCertificate::setName);
        ofNullable(updateRequest.getDescription()).ifPresent(foundCertificate::setDescription);
        ofNullable(updateRequest.getPrice()).ifPresent(foundCertificate::setPrice);
        ofNullable(updateRequest.getDuration()).ifPresent(foundCertificate::setDuration);

        List<CreateTagRequest> updateTags = updateRequest.getTags();

        ofNullable(updateTags).ifPresent(certificateTags -> {
            List<Tag> foundTags = new ArrayList<>(updateTags.size());
            updateTags.forEach(tag -> {
                Tag createdTag = tagService.createTagWithCheck(tag);
                foundTags.add(createdTag);
            });
            foundCertificate.setTags(foundTags);
        });

        GiftCertificate updatedCertificate = certificateRepository.save(foundCertificate);

        log.info("Updated a gift certificate with id {}", updatedCertificate.getId());
        return updatedCertificate;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting gift certificate with id {}", id);

        GiftCertificate foundCertificate = findById(id);

        certificateRepository.delete(foundCertificate);

        log.info("Gift certificate with id {} is deleted", foundCertificate.getId());
    }

    @Override
    public Page<GiftCertificate> findCertificateWithSearchParams(
            Pageable pageable,
            SearchGiftCertificateRequest searchRequest) {
        log.debug("Looking for a certificates by search params");

        Specification<GiftCertificate> specification = new GiftCertificateSpecification(searchRequest);

        Page<GiftCertificate> foundCertificates = pageableCertificateRepository.findAll(specification, pageable);

        log.info("Retrieved {} gift certificates of {} total",
                foundCertificates.getSize(),
                foundCertificates.getTotalElements());

        return foundCertificates;
    }
}
