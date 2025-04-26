package org.tutorial.springemailtutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.model.myColumns;
import org.tutorial.springemailtutorial.repository.MyColumnsRepository;

@Service
public class myColumnsService {

    private final MyColumnsRepository myColumnsRepository;

    @Autowired
    public myColumnsService(MyColumnsRepository myColumnsRepository) {
        this.myColumnsRepository = myColumnsRepository;
    }

    public myColumns saveColumn(MyColumnsDto columnDto) {
        myColumns column = new myColumns();
        column.setTitle(columnDto.getTitle());
        return myColumnsRepository.save(column);
    }
}
