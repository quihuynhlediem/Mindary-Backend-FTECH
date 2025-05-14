package com.mindary.diary.mappers.impl;

import com.mindary.diary.dto.DiaryImageDto;
import com.mindary.diary.mappers.Mapper;
import com.mindary.diary.models.DiaryImage;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiaryImageMapperImpl implements Mapper<DiaryImage, DiaryImageDto> {
    private final ModelMapper modelMapper;


    @Override
    public DiaryImageDto mapTo(DiaryImage image) {
        return modelMapper.map(image, DiaryImageDto.class);
    }

    @Override
    public DiaryImage mapFrom(DiaryImageDto diaryImageDto) {
        return modelMapper.map(diaryImageDto, DiaryImage.class);
    }
}
