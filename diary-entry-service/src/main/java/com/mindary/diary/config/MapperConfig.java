package com.mindary.diary.config;

import com.mindary.diary.dto.DiaryDto;
import com.mindary.diary.dto.DiaryImageDto;
import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.models.DiaryImage;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // Converter for DiaryImage to DiaryImageDto
//        modelMapper.typeMap(DiaryImage.class, DiaryImageDto.class)
//                .addMappings(mapper -> {
//                    mapper.map(DiaryImage::getId, DiaryImageDto::setId);
//                    mapper.map(DiaryImage::getUrl, DiaryImageDto::setUrl);
//                    mapper.map(DiaryImage::getCreatedAt, DiaryImageDto::setCreatedAt);
//                    mapper.map(DiaryImage::getUpdatedAt, DiaryImageDto::setUpdatedAt);
//                });
//
//        // Converter for Set<DiaryImage> to Set<DiaryImageDto>
//        Converter<Set<DiaryImage>, Set<DiaryImageDto>> imageSetConverter = context -> {
//            Set<DiaryImage> source = context.getSource();
//            if (source == null) {
//                return null;
//            }
//            return source.stream()
//                    .map(image -> modelMapper.map(image, DiaryImageDto.class))
//                    .collect(Collectors.toSet());
//        };
//
//        // Configure mapping for DiaryEntity to DiaryDto
//        modelMapper.typeMap(DiaryEntity.class, DiaryDto.class)
//                .addMappings(mapper -> {
//                    mapper.using(imageSetConverter)
//                            .map(DiaryEntity::getImages, DiaryDto::setImages);
//                });

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        return modelMapper;
    }
}
