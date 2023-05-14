package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.model.dto.request.CreateTagRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateTagRequest;
import com.epam.esm.epammodule4.model.entity.Tag;

public interface TagService extends PageableTagService{

    Tag findById(Long id);

    Tag create(CreateTagRequest createRequest);

    Tag createTagWithCheck(CreateTagRequest createTagRequest);

    Tag update(UpdateTagRequest updateRequest);

    void delete(Long id);

    Tag findByName(String name);

    Tag getTopUsedTag(Long id);
}
