package com.maheer9272.LedgerCore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponseDto {
    private LocalDateTime timeStamp;
    private int statusCode;
    private String error;
    private String message;
    private String path;
}