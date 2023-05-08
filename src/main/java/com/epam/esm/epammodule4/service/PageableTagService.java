package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableTagService {

    Page<Tag> findAll(Pageable pageable);
}
