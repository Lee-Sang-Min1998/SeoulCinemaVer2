package com.dbd.seoulcinema.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViewSpecificTicketDao {

    private String movieImg;

    private String movieName;

    private LocalDateTime screeningStartTime;

    private LocalDateTime screeningEndTime;

    private LocalDateTime screeningDate;

    private String theaterFloor;

}
