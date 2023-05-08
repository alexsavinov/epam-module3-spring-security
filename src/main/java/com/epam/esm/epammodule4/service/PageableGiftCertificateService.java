package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.dto.request.SearchGiftCertificateRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableGiftCertificateService {

    Page<GiftCertificate> findAll(Pageable pageable);

    Page<GiftCertificate> findCertificateWithSearchParams(Pageable pageable, SearchGiftCertificateRequest searchRequest);
}
