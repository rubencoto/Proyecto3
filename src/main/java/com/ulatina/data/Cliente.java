/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ulatina.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
public class Cliente {
    private int id;
    private String identificacion;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;
    private boolean kycVerificado;


    // Nuevos campos para KYC
    private String email;
    private String telefono;
    private String direccion;
    private String paisNacimiento;
    private String nacionalidad;
    private String profesion;
    private String ingresosMensuales;
    private String origenFondos;
    private boolean pep; // Persona Expuesta Políticamente
    private String documentoIdentidad; // Tipo de documento
    private LocalDateTime fechaVerificacionKyc;
    private String estadoKyc; // PENDIENTE, APROBADO, RECHAZADO
    private String observacionesKyc;
    private boolean enListaRestrictiva;
    private String motivoListaRestrictiva;

    public Cliente() {
        this.estadoKyc = "PENDIENTE";
        this.kycVerificado = false;
        this.pep = false;
        this.enListaRestrictiva = false;
    }

    public Cliente(int id){
        this();
        this.id = id;
    }

    // Getters y Setters existentes
    public int getId(){return id;}
    public void setId(int id){this.id=id;}
    public String getIdentificacion(){return identificacion;}
    public void setIdentificacion(String i){this.identificacion=i;}
    public String getNombreCompleto(){return nombreCompleto;}
    public void setNombreCompleto(String n){this.nombreCompleto=n;}
    public LocalDate getFechaNacimiento(){return fechaNacimiento;}
    public void setFechaNacimiento(LocalDate f){this.fechaNacimiento=f;}
    public boolean isKycVerificado(){return kycVerificado;}
    public void setKycVerificado(boolean k){this.kycVerificado=k;}

    // Nuevos Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getPaisNacimiento() { return paisNacimiento; }
    public void setPaisNacimiento(String paisNacimiento) { this.paisNacimiento = paisNacimiento; }

    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

    public String getProfesion() { return profesion; }
    public void setProfesion(String profesion) { this.profesion = profesion; }

    public String getIngresosMensuales() { return ingresosMensuales; }
    public void setIngresosMensuales(String ingresosMensuales) { this.ingresosMensuales = ingresosMensuales; }

    public String getOrigenFondos() { return origenFondos; }
    public void setOrigenFondos(String origenFondos) { this.origenFondos = origenFondos; }

    public boolean isPep() { return pep; }
    public void setPep(boolean pep) { this.pep = pep; }

    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }

    public LocalDateTime getFechaVerificacionKyc() { return fechaVerificacionKyc; }
    public void setFechaVerificacionKyc(LocalDateTime fechaVerificacionKyc) { this.fechaVerificacionKyc = fechaVerificacionKyc; }

    public String getEstadoKyc() { return estadoKyc; }
    public void setEstadoKyc(String estadoKyc) { this.estadoKyc = estadoKyc; }

    public String getObservacionesKyc() { return observacionesKyc; }
    public void setObservacionesKyc(String observacionesKyc) { this.observacionesKyc = observacionesKyc; }

    public boolean isEnListaRestrictiva() { return enListaRestrictiva; }
    public void setEnListaRestrictiva(boolean enListaRestrictiva) { this.enListaRestrictiva = enListaRestrictiva; }

    public String getMotivoListaRestrictiva() { return motivoListaRestrictiva; }
    public void setMotivoListaRestrictiva(String motivoListaRestrictiva) { this.motivoListaRestrictiva = motivoListaRestrictiva; }

}
