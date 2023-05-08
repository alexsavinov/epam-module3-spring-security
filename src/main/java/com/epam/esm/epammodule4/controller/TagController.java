package com.epam.esm.epammodule4.controller;

import com.epam.esm.epammodule4.model.dto.TagDto;
import com.epam.esm.epammodule4.model.dto.request.CreateTagRequest;
import com.epam.esm.epammodule4.model.dto.request.UpdateTagRequest;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.service.TagService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public TagDto getTagById(@PathVariable Long id) {
        Tag foundTag = tagService.findById(id);
        TagDto tagDto = modelMapper.map(foundTag, TagDto.class);

        tagDto.add(linkTo(methodOn(TagController.class).getTagById(tagDto.getId())).withSelfRel());
        return tagDto;
    }

    @GetMapping
    public Page<TagDto> getTags(Pageable pageable) {
        Page<Tag> foundTags = tagService.findAll(pageable);

        return foundTags.map(tag -> modelMapper.map(tag, TagDto.class));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagDto addTag(@RequestBody CreateTagRequest createRequest) {
        Tag createdTag = tagService.create(createRequest);
        TagDto tagDto = modelMapper.map(createdTag, TagDto.class);

        tagDto.add(linkTo(methodOn(TagController.class).addTag(createRequest)).withSelfRel());
        return tagDto;
    }

    @PatchMapping
    public TagDto updateTag(@RequestBody UpdateTagRequest updateRequest) {
        Tag updatedTag = tagService.update(updateRequest);
        TagDto tagDto = modelMapper.map(updatedTag, TagDto.class);

        tagDto.add(linkTo(methodOn(TagController.class).updateTag(updateRequest)).withSelfRel());
        return tagDto;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTagById(@PathVariable Long id) {
        tagService.delete(id);
    }


    @GetMapping(value = "/top-used-tag")
    public TagDto getTopUsedTag(@RequestParam Long userId) {
        Optional<Tag> foundTag = tagService.getTopUsedTag(userId);

        TagDto tagDto = modelMapper.map(foundTag.get(), TagDto.class);

        tagDto.add(linkTo(methodOn(TagController.class).getTagById(tagDto.getId())).withSelfRel());
        return tagDto;
    }
}
