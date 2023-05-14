package com.epam.esm.epammodule4.service.implementation;

import com.epam.esm.epammodule4.exception.TagAlreadyExistsException;
import com.epam.esm.epammodule4.exception.TagNotFoundException;
import com.epam.esm.epammodule4.model.dto.request.CreateTagRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateTagRequest;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.repository.TagRepository;
import com.epam.esm.epammodule4.service.TagService;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.epam.esm.epammodule4.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    public final EntityManager entityManager;

    @Override
    public Tag findById(Long id) {
        log.debug("Looking for a tag with id {}", id);

        Tag foundTag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Requested resource not found (id = %s)"
                        .formatted(id)
                ));

        log.info("Retrieved a tag with id {}", id);
        return foundTag;
    }

    @Override
    public Page<Tag> findAll(Pageable pageable) {
        log.debug("Retrieving tags. Page request: {}", pageable);

        Page<Tag> foundTags = tagRepository.findAll(pageable);

        log.info("Retrieved {} tags of {} total", foundTags.getSize(), foundTags.getTotalElements());
        return foundTags;
    }

    @Override
    public Tag create(CreateTagRequest createTagRequest) {
        log.debug("Creating a new tag");

        Tag newTag = modelMapper.map(createTagRequest, Tag.class);
        Tag createdTag;

        try {
            createdTag = tagRepository.save(newTag);

            log.info("Created a new tag with id {}", createdTag.getId());
            return createdTag;
        } catch (DataIntegrityViolationException ex) {
            throw new TagAlreadyExistsException(
                    "Requested resource already exists (name = %s)".formatted(createTagRequest.getName()));
        }
    }

    @Override
    @Transactional
    public Tag update(UpdateTagRequest updateRequest) {
        log.debug("Updating a tag with id {}", updateRequest.getId());

        Tag foundTag = findById(updateRequest.getId());

        foundTag.setName(updateRequest.getName());

        Tag updatedTag = tagRepository.save(foundTag);

        log.info("Updated a tag with id {}", updatedTag.getId());
        return updatedTag;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting tag with id {}", id);

        Tag foundTag = findById(id);

        tagRepository.delete(foundTag);

        log.info("Tag with id {} is deleted", foundTag.getId());
    }

    @Override
    public Tag findByName(String name) {
        log.debug("Looking for a tag with name {}", name);

        Optional<Tag> foundTag = tagRepository.findFirstByName(name);

        foundTag.orElseThrow(() -> new TagNotFoundException(
                "Requested resource not found (name = %s)".formatted(name)
        ));

        log.info("Found a tag with name {}", name);
        return foundTag.get();
    }

    @Override
    public Tag createTagWithCheck(CreateTagRequest createTagRequest) {
        String newTagName = createTagRequest.getName();

        log.debug("Looking for a tag with name {}", newTagName);

        Optional<Tag> foundTag = tagRepository.findFirstByName(newTagName);

        if (foundTag.isPresent()) {
            log.debug("Found a tag with name {}", newTagName);
        } else {
            Tag newTag = modelMapper.map(createTagRequest, Tag.class);

            foundTag = Optional.of(tagRepository.save(newTag));
            log.info("Created a new tag with name {}", newTagName);
        }

        return foundTag.get();
    }

    @Override
    @Transactional
    public Tag getTopUsedTag(Long userId) {
        log.debug("Looking for top used tag by user's id {}", userId);

        User user = userService.findById(userId);

        /* one way - native query*/
        Tag foundTag = tagRepository.getTopUsedTag(user.getId())
                .orElseThrow(() -> new TagNotFoundException("Requested resource not found (userId = %s)"
                        .formatted(user.getId())
                ));

        /* another way - HQL query */
//        String query = "select t from Tag t where t in " +
//                "(select c.tags from GiftCertificate c " +
//                "join Order o on c.id = o.giftCertificate.id where o.user.id=:id) " +
//                "group by t.id order by count(t.id) desc";
//        TypedQuery<Tag> typedQuery = entityManager.createQuery(query, Tag.class);

//        typedQuery.setMaxResults(1);
//        typedQuery.setParameter("id", userId);

//        Tag foundTag = typedQuery.getSingleResult();

        log.info("Found top used tag by user's id name {}", userId);
        return foundTag;
    }
}
