// com/tufondo/socios/domain/model/valueobjects/ContactoEmergencia.java
package com.tufondo.socios.domain.model.valueobjects;

import lombok.*;

public class ContactoEmergencia {
    private String nombre;
    private String telefono;
    private String parentesco;

    public ContactoEmergencia() {}
    
    // SECURITY: Validación en constructor para prevenir datos inválidos
    public ContactoEmergencia(String nombre, String telefono, String parentesco) {
        if (nombre != null && nombre.length() > 200) {
            throw new IllegalArgumentException("Nombre de contacto de emergencia no puede exceder 200 caracteres");
        }
        if (telefono != null && !telefono.matches("^\\+?[0-9]{7,15}$")) {
            throw new IllegalArgumentException("Teléfono de emergencia inválido: debe tener entre 7 y 15 dígitos");
        }
        if (parentesco != null && parentesco.length() > 50) {
            throw new IllegalArgumentException("Parentesco no puede exceder 50 caracteres");
        }
        this.nombre = nombre;
        this.telefono = telefono;
        this.parentesco = parentesco;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public String getParentesco() { return parentesco; }
    public void setParentesco(String v) { this.parentesco = v; }

    public static ContactoEmergenciaBuilder builder() { return new ContactoEmergenciaBuilder(); }

    // SECURITY: Builder actualizado para usar constructor con validación
    public static class ContactoEmergenciaBuilder {
        private String nombre;
        private String telefono;
        private String parentesco;
        
        public ContactoEmergenciaBuilder nombre(String v) { this.nombre = v; return this; }
        public ContactoEmergenciaBuilder telefono(String v) { this.telefono = v; return this; }
        public ContactoEmergenciaBuilder parentesco(String v) { this.parentesco = v; return this; }
        public ContactoEmergencia build() {
            return new ContactoEmergencia(nombre, telefono, parentesco);
        }
    }
}
