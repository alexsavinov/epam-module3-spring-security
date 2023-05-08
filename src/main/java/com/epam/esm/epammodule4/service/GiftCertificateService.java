package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.dto.request.CreateGiftCertificateRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateGiftCertificateRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;

public interface GiftCertificateService extends PageableGiftCertificateService {

    GiftCertificate findById(Long id);

    GiftCertificate create(CreateGiftCertificateRequest createRequest);

    GiftCertificate update(UpdateGiftCertificateRequest updateRequest);

    void delete(Long id);
}
