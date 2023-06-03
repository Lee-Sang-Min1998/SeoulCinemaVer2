package com.dbd.seoulcinema.service;

import com.dbd.seoulcinema.repository.MovieRepository;
import com.dbd.seoulcinema.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final MovieRepository movieRepository;
    private final ScheduleRepository scheduleRepository;

    public List<Object[]> getMovieAndSchedules(LocalDate screeningDate){
        return scheduleRepository.findMovieAndSchedule(screeningDate);
    }

    public List<Object[]> getMovieSchedules(Long movieNumber, LocalDate screeningDate){
        return scheduleRepository.findMovieSchedule(movieNumber, screeningDate);
    }
}
