package com.epam.esm.epammodule4.repository;

import com.epam.esm.epammodule4.EpamModule4Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EpamModule4Application.class)
class PageableGiftCertificateRepositoryTest {

    @Autowired
    private PageableGiftCertificateRepository pageableCertificateRepository;

    @Test
    void context() {
        assertThat(pageableCertificateRepository).isNotNull();
    }
}