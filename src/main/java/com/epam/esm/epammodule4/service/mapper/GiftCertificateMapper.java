package com.epam.esm.epammodule4.service.mapper;

import com.epam.esm.epammodule4.model.dto.GiftCertificateDto;
import com.epam.esm.epammodule4.model.dto.TagDto;
import com.epam.esm.epammodule4.model.dto.request.CreateGiftCertificateRequest;
import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.util.DateUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Optional.ofNullable;

@Component
@AllArgsConstructor
public class GiftCertificateMapper {

    private final DateUtil dateUtil;
    private final ModelMapper modelMapper;

    public GiftCertificate toCertificate(CreateGiftCertificateRequest createRequest) {
        GiftCertificate giftCertificate = GiftCertificate.builder()
                .name(createRequest.getName())
                .description(createRequest.getDescription())
                .duration(createRequest.getDuration())
                .price(createRequest.getPrice())
                .build();

        ofNullable(createRequest.getTags()).ifPresent(tagsDto -> {
            List<Tag> tags = tagsDto.stream().map(tag -> modelMapper.map(tag, Tag.class)).toList();
            giftCertificate.setTags(tags);
        });

        return giftCertificate;
    }

    public GiftCertificateDto toDto(GiftCertificate certificate) {
        GiftCertificateDto certificateDto = GiftCertificateDto.builder()
                .id(certificate.getId())
                .name(certificate.getName())
                .description(certificate.getDescription())
                .duration(certificate.getDuration())
                .price(certificate.getPrice())
                .build();

        ofNullable(certificate.getCreateDate()).ifPresent(createDate ->
            certificateDto.setCreateDate(dateUtil.toIso8601Format(createDate))
        );

        ofNullable(certificate.getLastUpdateDate()).ifPresent(lastUpdateDate ->
            certificateDto.setCreateDate(dateUtil.toIso8601Format(lastUpdateDate))
        );

        ofNullable(certificate.getTags()).ifPresent(tags -> {
            List<TagDto> tagsDto = tags.stream().map(tagDto -> modelMapper.map(tagDto, TagDto.class)).toList();
            certificateDto.setTags(tagsDto);
        });

        return certificateDto;
    }
}
