package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.controller.advice.ApplicationControllerAdvice;
import com.epam.esm.epammodule4.exception.TagAlreadyExistsException;
import com.epam.esm.epammodule4.exception.TagNotFoundException;
import com.epam.esm.epammodule4.model.dto.TagDto;
import com.epam.esm.epammodule4.model.dto.request.CreateTagRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateTagRequest;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    private static final Long TAG_ID = 1L;
    private static final Long USER_ID = 1L;
    @InjectMocks
    private TagController subject;
    @Mock
    private TagService tagService;
    @Mock
    private ModelMapper modelMapper;
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
    void getTagById() throws Exception {
        Tag expectedTag = new Tag();
        TagDto tagDto = new TagDto(TAG_ID, "myTag");

        when(tagService.findById(any(Long.class))).thenReturn(expectedTag);
        when(modelMapper.map(any(Tag.class), any(Class.class))).thenReturn(tagDto);

        mockMvc.perform(
                        get("/tags/{id}", TAG_ID)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tagDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(tagDto.getName()));

        verify(tagService).findById(TAG_ID);
        verify(modelMapper).map(expectedTag, TagDto.class);
        verifyNoMoreInteractions(tagService, modelMapper);
    }

    @Test
    void getTagById_whenTagNotFoundExceptionIsThrows_returns404() throws Exception {
        String errorMessage = "Tag not found";

        when(tagService.findById(any(Long.class))).thenThrow(new TagNotFoundException(errorMessage));

        mockMvc.perform(
                        get("/tags/{id}", TAG_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));

        verify(tagService).findById(TAG_ID);
        verifyNoInteractions(modelMapper);
        verifyNoMoreInteractions(tagService);
    }

    @Test
    void getAllTags() throws Exception {
        Tag expectedTag = Tag.builder().id(TAG_ID).name("myTag").build();

        List<Tag> expectedTags = List.of(expectedTag);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<Tag> pageableExpectedTags = new PageImpl(expectedTags, pageable, expectedTags.size());

        when(tagService.findAll(any(Pageable.class))).thenReturn(pageableExpectedTags);

        mockMvc.perform(
                        get("/tags")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "name,asc")
                )
                .andExpect(status().isOk());

        verify(tagService).findAll(pageable);
        verifyNoMoreInteractions(tagService);
    }

    @Test
    void addTag() throws Exception {
        Tag createdTag = new Tag();
        TagDto tagDto = new TagDto(TAG_ID, "myTag");

        when(tagService.create(any(CreateTagRequest.class))).thenReturn(createdTag);
        when(modelMapper.map(any(Tag.class), any(Class.class))).thenReturn(tagDto);

        RequestBuilder requestBuilder = post("/tags")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createdTag))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tagDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(tagDto.getName()));

        verify(modelMapper).map(createdTag, TagDto.class);
        verifyNoMoreInteractions(tagService, modelMapper);
    }

    @Test
    void addTag_whenTagAlreadyExistsExceptionIsThrows_returns409() throws Exception {
        CreateTagRequest createTagRequest = new CreateTagRequest("tag1");

        String errorMessage = "Tag already exists";

        when(tagService.create(any(CreateTagRequest.class)))
                .thenThrow(new TagAlreadyExistsException(errorMessage));

        RequestBuilder requestBuilder = post("/tags")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createTagRequest))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));

        verifyNoInteractions(modelMapper);
        verifyNoMoreInteractions(tagService);
    }

    @Test
    void updateTag() throws Exception {
        Tag uodateTag = new Tag();
        TagDto tagDto = new TagDto(TAG_ID, "myTag");

        when(tagService.update(any(UpdateTagRequest.class))).thenReturn(uodateTag);
        when(modelMapper.map(any(Tag.class), any(Class.class))).thenReturn(tagDto);

        RequestBuilder requestBuilder = patch("/tags")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(uodateTag))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tagDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(tagDto.getName()));

        verify(modelMapper).map(uodateTag, TagDto.class);
        verifyNoMoreInteractions(tagService, modelMapper);
    }

    @Test
    void deleteTagById() throws Exception {
        RequestBuilder requestBuilder = delete("/tags/{id}", TAG_ID);

        mockMvc.perform(requestBuilder).andExpect(status().isNoContent());

        verify(tagService).delete(TAG_ID);
        verifyNoMoreInteractions(tagService);
    }

    @Test
    void getTopUsedTag() throws Exception {
        Tag expectedTag = new Tag();
        TagDto tagDto = new TagDto(TAG_ID, "Tag");

        when(tagService.getTopUsedTag(any(Long.class))).thenReturn(expectedTag);
        when(modelMapper.map(any(Tag.class), any(Class.class))).thenReturn(tagDto);

        mockMvc.perform(
                        get("/tags/top-used-tag")
                                .param("userId", String.valueOf(USER_ID))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tagDto.getId().toString()))
                .andExpect(jsonPath("$.name").value(tagDto.getName()));

        verify(tagService).getTopUsedTag(TAG_ID);
        verify(modelMapper).map(expectedTag, TagDto.class);
        verifyNoMoreInteractions(tagService, modelMapper);
    }
}