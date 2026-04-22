// com/tufondo/socios/domain/model/valueobjects/Direccion.java
package com.tufondo.socios.domain.model.valueobjects;

import lombok.*;

public class Direccion {
    private String calle;
    private String numero;
    private String ciudad;
    private String departamento;
    private String codigoPostal;
    private String pais;

    public Direccion() {}
    
    // SECURITY: Validación en constructor para prevenir datos inválidos
    public Direccion(String calle, String numero, String ciudad, String departamento, String codigoPostal, String pais) {
        if (calle != null && calle.length() > 200) {
            throw new IllegalArgumentException("Calle no puede exceder 200 caracteres");
        }
        if (numero != null && numero.length() > 20) {
            throw new IllegalArgumentException("Número no puede exceder 20 caracteres");
        }
        if (ciudad != null && ciudad.length() > 100) {
            throw new IllegalArgumentException("Ciudad no puede exceder 100 caracteres");
        }
        if (departamento != null && departamento.length() > 100) {
            throw new IllegalArgumentException("Departamento no puede exceder 100 caracteres");
        }
        if (codigoPostal != null && !codigoPostal.matches("^[0-9]{4,10}$")) {
            throw new IllegalArgumentException("Código postal inválido: debe tener entre 4 y 10 dígitos");
        }
        if (pais != null && pais.length() > 100) {
            throw new IllegalArgumentException("País no puede exceder 100 caracteres");
        }
        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
        this.departamento = departamento;
        this.codigoPostal = codigoPostal;
        this.pais = pais;
    }

    public String getCalle() { return calle; }
    public void setCalle(String v) { this.calle = v; }
    public String getNumero() { return numero; }
    public void setNumero(String v) { this.numero = v; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String v) { this.ciudad = v; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String v) { this.departamento = v; }
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String v) { this.codigoPostal = v; }
    public String getPais() { return pais; }
    public void setPais(String v) { this.pais = v; }

    public static DireccionBuilder builder() { return new DireccionBuilder(); }

    // SECURITY: Builder actualizado para usar constructor con validación
    public static class DireccionBuilder {
        private String calle;
        private String numero;
        private String ciudad;
        private String departamento;
        private String codigoPostal;
        private String pais;
        
        public DireccionBuilder calle(String v) { this.calle = v; return this; }
        public DireccionBuilder numero(String v) { this.numero = v; return this; }
        public DireccionBuilder ciudad(String v) { this.ciudad = v; return this; }
        public DireccionBuilder departamento(String v) { this.departamento = v; return this; }
        public DireccionBuilder codigoPostal(String v) { this.codigoPostal = v; return this; }
        public DireccionBuilder pais(String v) { this.pais = v; return this; }
        public Direccion build() {
            return new Direccion(calle, numero, ciudad, departamento, codigoPostal, pais);
        }
    }
}
