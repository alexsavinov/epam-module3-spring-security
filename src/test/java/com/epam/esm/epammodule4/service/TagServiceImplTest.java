package com.epam.esm.epammodule4.service;

import com.epam.esm.epammodule4.exception.TagAlreadyExistsException;
import com.epam.esm.epammodule4.exception.TagNotFoundException;
import com.epam.esm.epammodule4.model.dto.request.CreateTagRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateTagRequest;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.repository.TagRepository;
import com.epam.esm.epammodule4.service.implementation.TagServiceImpl;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.epam.esm.epammodule4.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    private static final Long TAG_ID = 1L;
    private static final Long USER_ID = 1L;
    @InjectMocks
    private TagServiceImpl subject;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<Tag> typedQuery;
    @Mock
    private CreateTagRequest createRequest;
    @Mock
    private UpdateTagRequest updateRequest;

    @Test
    void findById() {
        Tag expectedTag = new Tag();

        when(tagRepository.findById(any(Long.class))).thenReturn(Optional.of(expectedTag));

        Tag actualTag = subject.findById(TAG_ID);

        verify(tagRepository).findById(TAG_ID);
        verifyNoMoreInteractions(tagRepository);

        assertThat(actualTag).isEqualTo(expectedTag);
    }

    @Test
    void findById_whenTagIsNotFoundById_throwsTagNotFoundException() {
        when(tagRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> subject.findById(TAG_ID));

        verify(tagRepository).findById(TAG_ID);
        verifyNoMoreInteractions(tagRepository);

        String expectedMessage = "Requested resource not found (id = %s)".formatted(TAG_ID);
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void findAll() {
        List<Tag> expectedTags = List.of(new Tag());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<Tag> pageableExpectedTags = new PageImpl(expectedTags, pageable, expectedTags.size());

        when(tagRepository.findAll(any(Pageable.class))).thenReturn(pageableExpectedTags);

        Page<Tag> actualTags = subject.findAll(pageable);

        verify(tagRepository).findAll(pageable);
        verifyNoMoreInteractions(tagRepository);

        assertThat(actualTags).isEqualTo(pageableExpectedTags);
    }

    @Test
    void create() {
        Tag createdTag = Tag.builder().id(TAG_ID).name("myTag").build();
        Tag expectedTag = Tag.builder().id(TAG_ID).name("myTag").build();

        when(modelMapper.map(any(CreateTagRequest.class), any(Class.class))).thenReturn(createdTag);
        when(tagRepository.save(any(Tag.class))).thenReturn(expectedTag);

        Tag actualTag = subject.create(createRequest);

        verify(modelMapper).map(createRequest, Tag.class);
        verify(tagRepository).save(createdTag);
        verifyNoMoreInteractions(tagRepository, modelMapper);

        assertThat(actualTag).isEqualTo(expectedTag);
    }

    @Test
    void create_whenTagWithNameExists_throwsTagAlreadyExistsException() {
        Tag newTag = Tag.builder().id(TAG_ID).name("myTag").build();

        when(modelMapper.map(any(CreateTagRequest.class), any(Class.class))).thenReturn(newTag);
        when(tagRepository.save(any(Tag.class)))
                .thenThrow(new DataIntegrityViolationException("Tag already exists"));

        TagAlreadyExistsException exception = assertThrows(TagAlreadyExistsException.class,
                () -> subject.create(createRequest));

        verify(tagRepository).save(newTag);
        verify(modelMapper).map(createRequest, Tag.class);
        verifyNoMoreInteractions(tagRepository, modelMapper);

        assertThat(exception.getMessage()).isEqualTo("Requested resource already exists (name = null)");
    }

    @Test
    void update() {
        Tag updateTag = Tag.builder().id(TAG_ID).name("myTag").build();
        Tag expectedTag = Tag.builder().id(TAG_ID).name("myTag").build();

        when(tagRepository.findById(any(Long.class))).thenReturn(Optional.of(updateTag));
        when(updateRequest.getName()).thenReturn("tag1");
        when(tagRepository.save(any(Tag.class))).thenReturn(expectedTag);

        Tag actualTag = subject.update(updateRequest);

        verify(tagRepository).save(updateTag);
        verifyNoMoreInteractions(tagRepository);

        assertThat(actualTag).isEqualTo(expectedTag);
    }

    @Test
    void delete() {
        Tag deleteTag = Tag.builder().id(TAG_ID).name("myTag").build();

        when(tagRepository.findById(any(Long.class))).thenReturn(Optional.of(deleteTag));

        subject.delete(TAG_ID);

        verify(tagRepository).delete(deleteTag);
        verifyNoMoreInteractions(tagRepository);
    }

    @Test
    void findByName() {
        Tag expectedTag = new Tag();

        when(tagRepository.findFirstByName(any(String.class))).thenReturn(Optional.of(expectedTag));

        Tag actualTag = subject.findByName("tag1");

        verify(tagRepository).findFirstByName(any(String.class));
        verifyNoMoreInteractions(tagRepository);

        assertThat(actualTag).isEqualTo(expectedTag);
    }

    @Test
    void findByName_whenTagWithNameNotExists_throwsTagNotFoundException() {
        Tag searchTag = Tag.builder().id(TAG_ID).name("myTag").build();

        when(tagRepository.findFirstByName(any(String.class))).thenReturn(Optional.empty());

        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> subject.findByName(searchTag.getName()));

        verify(tagRepository).findFirstByName(any(String.class));
        verifyNoMoreInteractions(tagRepository);

        String expectedMessage = "Requested resource not found (name = %s)".formatted(searchTag.getName());
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void createTagWithCheck_whenTagNotFound_returnsNewTag() {
        Tag newTag = Tag.builder().id(TAG_ID).name("myTag").build();
        Tag expectedTag = Tag.builder().id(TAG_ID).name("myTag").build();

        when(createRequest.getName()).thenReturn(newTag.getName());
        when(modelMapper.map(any(CreateTagRequest.class), any(Class.class))).thenReturn(newTag);
        when(tagRepository.save(any(Tag.class))).thenReturn(expectedTag);

        Tag actualTag = subject.createTagWithCheck(createRequest);

        verify(tagRepository).findFirstByName(newTag.getName());
        verify(modelMapper).map(createRequest, Tag.class);
        verify(tagRepository).save(newTag);
        verifyNoMoreInteractions(tagRepository);

        assertThat(actualTag).isEqualTo(expectedTag);
    }


    @Test
    void createTagWithCheck_whenTagFound_returnsCurrentTag() {
        Tag newTag = Tag.builder().id(TAG_ID).name("myTag").build();
        Tag expectedTag = Tag.builder().id(TAG_ID).name("myTag").build();

        when(createRequest.getName()).thenReturn(newTag.getName());
        when(tagRepository.findFirstByName(any(String.class))).thenReturn(Optional.of(expectedTag));

        Tag actualTag = subject.createTagWithCheck(createRequest);

        verify(tagRepository).findFirstByName(newTag.getName());
        verifyNoMoreInteractions(tagRepository);

        assertThat(actualTag).isEqualTo(expectedTag);
    }

    @Test
    void getTopUsedTag() {
        Tag expectedTag = Tag.builder().id(TAG_ID).name("myTag").build();
        User user = User.builder().id(USER_ID).build();

        when(userService.findById(any(Long.class))).thenReturn(user);
        when(tagRepository.getTopUsedTag(any(Long.class))).thenReturn(Optional.ofNullable(expectedTag));

        Tag actualTag = subject.getTopUsedTag(TAG_ID);

        assertThat(actualTag).isEqualTo(expectedTag);
    }

    @Test
    @Disabled
    void getTopUsedTagHQLQuery() {
        Tag expectedTag = Tag.builder().id(TAG_ID).name("myTag").build();
        User user = User.builder().id(USER_ID).build();

        when(userService.findById(any(Long.class))).thenReturn(user);
        when(entityManager.createQuery(anyString(), any(Class.class))).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(expectedTag);

        Tag actualTag = subject.getTopUsedTag(TAG_ID);

        assertThat(actualTag).isEqualTo(expectedTag);
    }


    @Test
    @Disabled
    void getTopUsedTagHQLQuery_whenTagNotFound_thenThrowsTagNotFoundException() {
        User user = User.builder().id(USER_ID).build();

        when(userService.findById(any(Long.class))).thenReturn(user);

        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> subject.getTopUsedTag(TAG_ID));

        String expectedMessage = "Requested resource not found (userId = %s)".formatted(user.getId());
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    @Disabled
    void getTopUsedTag_withRepository() {
        Tag expectedTag = Tag.builder().id(TAG_ID).name("myTag").build();

        when(entityManager.createQuery(any(String.class))).thenReturn(typedQuery);
        when(tagRepository.getTopUsedTag(any(Long.class))).thenReturn(Optional.of(expectedTag));

        Tag actualTag = subject.getTopUsedTag(TAG_ID);

        verify(tagRepository).getTopUsedTag(TAG_ID);
        verifyNoMoreInteractions(tagRepository);

        assertThat(actualTag).isEqualTo(expectedTag);
    }
}