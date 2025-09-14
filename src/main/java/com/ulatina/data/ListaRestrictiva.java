/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ulatina.data;

import java.time.LocalDateTime;

public class ListaRestrictiva {
    private int id;
    private String identificacion;
    private String nombreCompleto;
    private String tipoLista; // OFAC, ONU, PEP, INTERNO
    private String motivoInclusion;
    private LocalDateTime fechaInclusion;
    private LocalDateTime fechaActualizacion;
    private boolean activo;
    private String observaciones;

    public ListaRestrictiva() {
        this.activo = true;
        this.fechaInclusion = LocalDateTime.now();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getTipoLista() { return tipoLista; }
    public void setTipoLista(String tipoLista) { this.tipoLista = tipoLista; }

    public String getMotivoInclusion() { return motivoInclusion; }
    public void setMotivoInclusion(String motivoInclusion) { this.motivoInclusion = motivoInclusion; }

    public LocalDateTime getFechaInclusion() { return fechaInclusion; }
    public void setFechaInclusion(LocalDateTime fechaInclusion) { this.fechaInclusion = fechaInclusion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
