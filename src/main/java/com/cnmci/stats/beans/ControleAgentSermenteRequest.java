package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ControleAgentSermenteRequest {
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate debut;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate fin;
    private long ville;
    private long quartier;
    private int etat;
}
