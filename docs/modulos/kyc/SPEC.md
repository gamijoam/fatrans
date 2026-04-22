# Módulo KYC Simplificado - Especificación Técnica

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.2  
**Fecha:** 2026-04-14  
**Estado:** ✅ IMPLEMENTADO (Todos los endpoints operativos)  
**Complejidad:** Media  
**Tiempo estimado:** 4 semanas

---

## Resumen

Este documento contiene las especificaciones técnicas del módulo KYC (Know Your Customer) en su versión **simplificada**, diseñada para permitir la operación inicial del fondo de ahorro sin dependencias de APIs externas (SAIME/SENIAT).

La arquitectura está diseñada para ser **compatible con futuras integraciones** de verificación con servicios oficiales venezolanos, siguiendo el principio de "empezar simple, escalar sin límites".

---

## 1. Objetivos del Módulo

### 1.1 Objetivo Principal
Permitir la verificación de identidad de socios nuevos del fondo de ahorro, cumpliendo con los requisitos regulatorios básicos de LOPDP y generando confianza para operaciones iniciales.

### 1.2 Objetivos Secundarios
- Crear una base de documentos digitales seguros
- Establecer workflow de revisión manual para casos no automatizados
- Generar historial de verificaciones para auditoría
- Diseñar arquitectura extensible para integraciones futuras (SAIME, SENIAT)

### 1.3Scope Inicial (KYC Simplificado)
- ✅ Registro de documentos de identidad
- ✅ Validación de formato y expiración de documentos
- ✅ Selfie de verificación (sin liveness detection avanzado)
- ✅ Cola de revisión manual para analistas
- ✅ Notificaciones de resultado
- ✅ Historial de verificaciones
- ✅ Renovación de KYC expirado

### 1.4 Fuera del Scope Inicial
- ❌ Integración con SAIME (pendiente documentación oficial)
- ❌ Integración con SENIAT (pendiente acceso a API)
- ❌ Liveness detection (requiere infraestructura adicional)
- ❌ Verificación biométrica facial avanzada

---

## 2. Arquitectura del Sistema

### 2.1 Arquitectura General

```
┌─────────────────────────────────────────────────────────────────┐
│                        CAPAS DEL SISTEMA                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              PRESENTATION LAYER                         │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │    │
│  │  │   KYC       │  │   Analista  │  │   Admin     │     │    │
│  │  │  Controller │  │  Controller │  │  Controller │     │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              APPLICATION LAYER                          │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │    │
│  │  │   Iniciar   │  │   Revisar   │  │   Notificar │     │    │
│  │  │   KYC UC    │  │   KYC UC    │  │   UC        │     │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │    │
│  │  │   Subir     │  │   Aprobar   │  │   Consultar │     │    │
│  │  │   Docs UC   │  │   UC        │  │   Estado UC │     │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                 DOMAIN LAYER                            │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │    │
│  │  │ Verificacion│  │  Documento  │  │  Resultado  │     │    │
│  │  │  KYC        │  │  Identidad  │  │  Verificacion│    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │    │
│  │                                                         │    │
│  │  ┌─────────────────────────────────────────────────┐   │    │
│  │  │         INTERFACES DE REPOSITORIO                │   │    │
│  │  │  (Para extensiones futuras SAIME/SENIAT)        │   │    │
│  │  └─────────────────────────────────────────────────┘   │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │             INFRASTRUCTURE LAYER                        │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │    │
│  │  │   JPA       │  │  Storage    │  │   Email     │     │    │
│  │  │  Adapters   │  │  Service    │  │  Service    │     │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Diseño Extensible para Integraciones Futuras

```
┌─────────────────────────────────────────────────────────────────┐
│                   ARQUITECTURA EXTENSIBLE                       │
│              (Compatible con SAIME/SENIAT futuro)              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌───────────────────────────────────────────────────────┐    │
│   │              DOMAIN -Puertos (Ports)                  │    │
│   │                                                       │    │
│   │   interface IdentidadVerificatorPort {               │    │
│   │       ResultadoVerificacion verificar(Cedula datos); │    │
│   │   }                                                   │    │
│   │                                                       │    │
│   │   interface RifVerificatorPort {                     │    │
│   │       ResultadoVerificacion verificar(RIF datos);     │    │
│   │   }                                                   │    │
│   │                                                       │    │
│   │   interface DocumentValidatorPort {                  │    │
│   │       ResultadoValidacion validar(Documento doc);     │    │
│   │   }                                                   │    │
│   └───────────────────────────────────────────────────────┘    │
│                              │                                  │
│                              ▼                                  │
│   ┌───────────────────────────────────────────────────────┐    │
│   │         APPLICATION -Casos de Uso                    │    │
│   │                                                       │    │
│   │   // Caso de uso usa puerto, no implementación       │    │
│   │   class VerificarIdentidadUseCase {                 │    │
│   │       IdentidadVerificatorPort verificator;          │    │
│   │                                               // ← Inyección  │
│   │       Resultado ejecutar(DatosVerificacion datos) {  │    │
│   │           // Delegar al puerto (implementación pluggable)│    │
│   │           return verificator.verificar(datos.cedula); │    │
│   │       }                                               │    │
│   │   }                                                   │    │
│   └───────────────────────────────────────────────────────┘    │
│                              │                                  │
│                              ▼                                  │
│   ┌───────────────────────────────────────────────────────┐    │
│   │         INFRASTRUCTURE - Adaptadores (Adapters)      │    │
│   │                                                       │    │
│   │   // Implementación SIMPLIFICADA (actual)             │    │
│   │   class LocalIdentidadVerificatorAdapter              │    │
│   │       implements IdentidadVerificatorPort {           │    │
│   │       ResultadoVerificacion verificar(Cedula datos) { │    │
│   │           // Validación básica local                   │    │
│   │           return validarFormato(datos);               │    │
│   │       }                                               │    │
│   │   }                                                   │    │
│   │                                                       │    │
│   │   // Implementación FUTURA para SAIME                 │    │
│   │   class SaimeVerificatorAdapter                       │    │
│   │       implements IdentidadVerificatorPort {           │    │
│   │       ResultadoVerificacion verificar(Cedula datos) { │    │
│   │           // Llamada a API SAIME real                │    │
│   │           return saimeClient.verificar(datos);       │    │
│   │       }                                               │    │
│   │   }                                                   │    │
│   │                                                       │    │
│   │   // Implementación FUTURA para SENIAT                │    │
│   │   class SeniatVerificatorAdapter                      │    │
│   │       implements RifVerificatorPort {                 │    │
│   │       // Llamada a API SENIAT                         │    │
│   │   }                                                   │    │
│   └───────────────────────────────────────────────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**Nota:** Este patrón de arquitectura (Ports & Adapters / Hexagonal) permite agregar integraciones SAIME/SENIAT sin modificar la lógica de negocio existente.

---

## 3. Modelo de Dominio

### 3.1 Entidades Principales

```java
// ================================================================
// 3.1.1 VerificacionKYC - Entidad Principal
// ================================================================
package com.tufondo.kyc.domain.model;

import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VerificacionKYC {
    
    private UUID id;
    private UUID socioId;
    private NivelVerificacion nivel;
    private EstadoVerificacion estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCompletado;
    private LocalDateTime fechaExpiracion;
    private String datosVerificacionAutomatica;  // JSON
    private String revisadoPor;
    private LocalDateTime fechaRevision;
    private String comentariosRevision;
    private String motivoRechazo;
    private List<DocumentoIdentidad> documentos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public VerificacionKYC() {
        this.documentos = new ArrayList<>();
    }
    
    // Métodos de negocio
    public void agregarDocumento(DocumentoIdentidad documento) {
        this.documentos.add(documento);
    }
    
    public boolean estaPendiente() {
        return this.estado == EstadoVerificacion.PENDIENTE;
    }
    
    public boolean puedeSerRevisada() {
        return this.estado == EstadoVerificacion.EN_REVISION;
    }
    
    public boolean estaAprobada() {
        return this.estado == EstadoVerificacion.APROBADO;
    }
    
    public boolean estaExpirada() {
        return this.estado == EstadoVerificacion.EXPIRADO 
            || (this.fechaExpiracion != null && this.fechaExpiracion.isBefore(LocalDateTime.now()));
    }
    
    public boolean puedeRenovarse() {
        return this.estado == EstadoVerificacion.EXPIRADO 
            || this.estado == EstadoVerificacion.RECHAZADO;
    }
    
    public boolean tieneDocumentosCompletos() {
        return this.documentos.stream()
            .allMatch(doc -> doc.estaValido());
    }
    
    public int getDocumentosRequeridos() {
        return this.nivel.getCantidadDocumentosRequeridos();
    }
    
    public int getDocumentosValidos() {
        return (int) this.documentos.stream()
            .filter(doc -> doc.estaValido())
            .count();
    }
    
    public boolean tieneDocumentosPendientes() {
        return this.documentos.stream()
            .anyMatch(doc -> doc.estaPendiente());
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID socioId) { this.socioId = socioId; }
    public NivelVerificacion getNivel() { return nivel; }
    public void setNivel(NivelVerificacion nivel) { this.nivel = nivel; }
    public EstadoVerificacion getEstado() { return estado; }
    public void setEstado(EstadoVerificacion estado) { this.estado = estado; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaCompletado() { return fechaCompletado; }
    public void setFechaCompletado(LocalDateTime fechaCompletado) { this.fechaCompletado = fechaCompletado; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public String getDatosVerificacionAutomatica() { return datosVerificacionAutomatica; }
    public void setDatosVerificacionAutomatica(String datos) { this.datosVerificacionAutomatica = datos; }
    public String getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(String revisadoPor) { this.revisadoPor = revisadoPor; }
    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }
    public String getComentariosRevision() { return comentariosRevision; }
    public void setComentariosRevision(String comentarios) { this.comentariosRevision = comentarios; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivo) { this.motivoRechazo = motivo; }
    public List<DocumentoIdentidad> getDocumentos() { return documentos; }
    public void setDocumentos(List<DocumentoIdentidad> documentos) { this.documentos = documentos; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Builder
    public static VerificacionKYCBuilder builder() { 
        return new VerificacionKYCBuilder(); 
    }
    
    public static class VerificacionKYCBuilder {
        private VerificacionKYC v = new VerificacionKYC();
        public VerificacionKYCBuilder id(UUID id) { v.id = id; return this; }
        public VerificacionKYCBuilder socioId(UUID socioId) { v.socioId = socioId; return this; }
        public VerificacionKYCBuilder nivel(NivelVerificacion nivel) { v.nivel = nivel; return this; }
        public VerificacionKYCBuilder estado(EstadoVerificacion estado) { v.estado = estado; return this; }
        public VerificacionKYCBuilder fechaInicio(LocalDateTime fechaInicio) { v.fechaInicio = fechaInicio; return this; }
        public VerificacionKYCBuilder fechaCompletado(LocalDateTime fechaCompletado) { v.fechaCompletado = fechaCompletado; return this; }
        public VerificacionKYCBuilder fechaExpiracion(LocalDateTime fechaExpiracion) { v.fechaExpiracion = fechaExpiracion; return this; }
        public VerificacionKYCBuilder datosVerificacionAutomatica(String datos) { v.datosVerificacionAutomatica = datos; return this; }
        public VerificacionKYCBuilder revisadoPor(String revisadoPor) { v.revisadoPor = revisadoPor; return this; }
        public VerificacionKYCBuilder fechaRevision(LocalDateTime fechaRevision) { v.fechaRevision = fechaRevision; return this; }
        public VerificacionKYCBuilder comentariosRevision(String comentarios) { v.comentariosRevision = comentarios; return this; }
        public VerificacionKYCBuilder motivoRechazo(String motivo) { v.motivoRechazo = motivo; return this; }
        public VerificacionKYCBuilder documentos(List<DocumentoIdentidad> docs) { v.documentos = docs; return this; }
        public VerificacionKYCBuilder createdAt(LocalDateTime createdAt) { v.createdAt = createdAt; return this; }
        public VerificacionKYCBuilder updatedAt(LocalDateTime updatedAt) { v.updatedAt = updatedAt; return this; }
        public VerificacionKYC build() { return v; }
    }
}
```

```java
// ================================================================
// 3.1.2 DocumentoIdentidad
// ================================================================
package com.tufondo.kyc.domain.model;

import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentoIdentidad {
    
    private UUID id;
    private UUID verificacionId;
    private UUID socioId;
    private TipoDocumentoKYC tipoDocumento;
    private String urlAlmacenamiento;
    private String nombreOriginal;
    private Long tamanoBytes;
    private String mimeType;
    private String hashArchivo;  // SHA-256
    private LocalDateTime fechaSubida;
    private LocalDate fechaExpiracionDocumento;  // null si no expira
    private EstadoDocumento estado;
    private String motivoRechazo;
    private String metadatosValidacion;  // JSON con resultados de validación
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public DocumentoIdentidad() {}
    
    // Métodos de negocio
    public boolean estaValido() {
        return this.estado == EstadoDocumento.VALIDADO;
    }
    
    public boolean estaPendiente() {
        return this.estado == EstadoDocumento.PENDIENTE;
    }
    
    public boolean estaRechazado() {
        return this.estado == EstadoDocumento.RECHAZADO;
    }
    
    public boolean estaExpirado() {
        if (this.fechaExpiracionDocumento == null) {
            return false;
        }
        return this.fechaExpiracionDocumento.isBefore(LocalDate.now());
    }
    
    public boolean puedeSerEliminado() {
        // Solo puede eliminarse si está Pendiente y no ha sido procesado
        return this.estado == EstadoDocumento.PENDIENTE 
            && this.verificacionId == null;
    }
    
    public void marcarComoRechazado(String motivo) {
        this.estado = EstadoDocumento.RECHAZADO;
        this.motivoRechazo = motivo;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void marcarComoValidado() {
        this.estado = EstadoDocumento.VALIDADO;
        this.motivoRechazo = null;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getVerificacionId() { return verificacionId; }
    public void setVerificacionId(UUID verificacionId) { this.verificacionId = verificacionId; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID socioId) { this.socioId = socioId; }
    public TipoDocumentoKYC getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumentoKYC tipo) { this.tipoDocumento = tipo; }
    public String getUrlAlmacenamiento() { return urlAlmacenamiento; }
    public void setUrlAlmacenamiento(String url) { this.urlAlmacenamiento = url; }
    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombre) { this.nombreOriginal = nombre; }
    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamano) { this.tamanoBytes = tamano; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mime) { this.mimeType = mime; }
    public String getHashArchivo() { return hashArchivo; }
    public void setHashArchivo(String hash) { this.hashArchivo = hash; }
    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fecha) { this.fechaSubida = fecha; }
    public LocalDate getFechaExpiracionDocumento() { return fechaExpiracionDocumento; }
    public void setFechaExpiracionDocumento(LocalDate fecha) { this.fechaExpiracionDocumento = fecha; }
    public EstadoDocumento getEstado() { return estado; }
    public void setEstado(EstadoDocumento estado) { this.estado = estado; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivo) { this.motivoRechazo = motivo; }
    public String getMetadatosValidacion() { return metadatosValidacion; }
    public void setMetadatosValidacion(String metadatos) { this.metadatosValidacion = metadatos; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String obs) { this.observaciones = obs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public static DocumentoIdentidadBuilder builder() { 
        return new DocumentoIdentidadBuilder(); 
    }
    
    public static class DocumentoIdentidadBuilder {
        private DocumentoIdentidad d = new DocumentoIdentidad();
        public DocumentoIdentidadBuilder id(UUID id) { d.id = id; return this; }
        public DocumentoIdentidadBuilder verificacionId(UUID verificacionId) { d.verificacionId = verificacionId; return this; }
        public DocumentoIdentidadBuilder socioId(UUID socioId) { d.socioId = socioId; return this; }
        public DocumentoIdentidadBuilder tipoDocumento(TipoDocumentoKYC tipo) { d.tipoDocumento = tipo; return this; }
        public DocumentoIdentidadBuilder urlAlmacenamiento(String url) { d.urlAlmacenamiento = url; return this; }
        public DocumentoIdentidadBuilder nombreOriginal(String nombre) { d.nombreOriginal = nombre; return this; }
        public DocumentoIdentidadBuilder tamanoBytes(Long tamano) { d.tamanoBytes = tamano; return this; }
        public DocumentoIdentidadBuilder mimeType(String mime) { d.mimeType = mime; return this; }
        public DocumentoIdentidadBuilder hashArchivo(String hash) { d.hashArchivo = hash; return this; }
        public DocumentoIdentidadBuilder fechaSubida(LocalDateTime fecha) { d.fechaSubida = fecha; return this; }
        public DocumentoIdentidadBuilder fechaExpiracionDocumento(LocalDate fecha) { d.fechaExpiracionDocumento = fecha; return this; }
        public DocumentoIdentidadBuilder estado(EstadoDocumento estado) { d.estado = estado; return this; }
        public DocumentoIdentidadBuilder motivoRechazo(String motivo) { d.motivoRechazo = motivo; return this; }
        public DocumentoIdentidadBuilder metadatosValidacion(String meta) { d.metadatosValidacion = meta; return this; }
        public DocumentoIdentidadBuilder observaciones(String obs) { d.observaciones = obs; return this; }
        public DocumentoIdentidadBuilder createdAt(LocalDateTime createdAt) { d.createdAt = createdAt; return this; }
        public DocumentoIdentidadBuilder updatedAt(LocalDateTime updatedAt) { d.updatedAt = updatedAt; return this; }
        public DocumentoIdentidad build() { return d; }
    }
}
```

```java
// ================================================================
// 3.1.3 ConsentimientoKYC - Para cumplimiento LOPDP
// ================================================================
package com.tufondo.kyc.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ConsentimientoKYC {
    
    private UUID id;
    private UUID socioId;
    private String tipoConsentimiento;  // "KYC_BASICO", "KYC_MEDIO", "KYC_COMPLETO"
    private boolean aceptado;
    private LocalDateTime fechaConsentimiento;
    private String ipCliente;
    private String userAgent;
    private String versionPolitica;  // Para rastrear qué versión de política aceptó
    
    public ConsentimientoKYC() {}
    
    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID socioId) { this.socioId = socioId; }
    public String getTipoConsentimiento() { return tipoConsentimiento; }
    public void setTipoConsentimiento(String tipo) { this.tipoConsentimiento = tipo; }
    public boolean isAceptado() { return aceptado; }
    public void setAceptado(boolean aceptado) { this.aceptado = aceptado; }
    public LocalDateTime getFechaConsentimiento() { return fechaConsentimiento; }
    public void setFechaConsentimiento(LocalDateTime fecha) { this.fechaConsentimiento = fecha; }
    public String getIpCliente() { return ipCliente; }
    public void setIpCliente(String ip) { this.ipCliente = ip; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getVersionPolitica() { return versionPolitica; }
    public void setVersionPolitica(String version) { this.versionPolitica = version; }
    
    public static ConsentimientoKYCBuilder builder() { 
        return new ConsentimientoKYCBuilder(); 
    }
    
    public static class ConsentimientoKYCBuilder {
        private ConsentimientoKYC c = new ConsentimientoKYC();
        public ConsentimientoKYCBuilder id(UUID id) { c.id = id; return this; }
        public ConsentimientoKYCBuilder socioId(UUID socioId) { c.socioId = socioId; return this; }
        public ConsentimientoKYCBuilder tipoConsentimiento(String tipo) { c.tipoConsentimiento = tipo; return this; }
        public ConsentimientoKYCBuilder aceptado(boolean aceptado) { c.aceptado = aceptado; return this; }
        public ConsentimientoKYCBuilder fechaConsentimiento(LocalDateTime fecha) { c.fechaConsentimiento = fecha; return this; }
        public ConsentimientoKYCBuilder ipCliente(String ip) { c.ipCliente = ip; return this; }
        public ConsentimientoKYCBuilder userAgent(String ua) { c.userAgent = ua; return this; }
        public ConsentimientoKYCBuilder versionPolitica(String version) { c.versionPolitica = version; return this; }
        public ConsentimientoKYC build() { return c; }
    }
}
```

---

### 3.2 Enumeraciones

```java
// ================================================================
// 3.2.1 NivelVerificacion
// ================================================================
package com.tufondo.kyc.domain.model.enums;

public enum NivelVerificacion {
    BASICO("KYC Básico", 4),      // 4 documentos requeridos
    MEDIO("KYC Medio", 6),       // 6 documentos requeridos
    COMPLETO("KYC Completo", 8); // 8 documentos requeridos
    
    private final String descripcion;
    private final int cantidadDocumentosRequeridos;
    
    NivelVerificacion(String descripcion, int cantidadDocs) {
        this.descripcion = descripcion;
        this.cantidadDocumentosRequeridos = cantidadDocs;
    }
    
    public String getDescripcion() { return descripcion; }
    public int getCantidadDocumentosRequeridos() { return cantidadDocumentosRequeridos; }
}
```

```java
// ================================================================
// 3.2.2 EstadoVerificacion
// ================================================================
package com.tufondo.kyc.domain.model.enums;

public enum EstadoVerificacion {
    PENDIENTE("Documentos enviados, esperando validación"),
    EN_REVISION("En revisión por analista"),
    APROBADO("Verificación exitosa"),
    RECHAZADO("Rechazado"),
    REENVIADO("Documentos reenviados después de rechazo"),
    EXPIRADO("Verificación expirada, requiere renovación"),
    CANCELADO("Cancelado por el usuario");
    
    private final String descripcion;
    
    EstadoVerificacion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() { return descripcion; }
    
    public boolean esEditable() {
        return this == PENDIENTE || this == RECHAZADO || this == REENVIADO;
    }
}
```

```java
// ================================================================
// 3.2.3 TipoDocumentoKYC
// ================================================================
package com.tufondo.kyc.domain.model.enums;

public enum TipoDocumentoKYC {
    CEDULA_ANVERSO("Cédula de Identidad - Anverso", true, false),
    CEDULA_REVERSO("Cédula de Identidad - Reverso", true, false),
    SELFIE_CEDULA("Selfie con Cédula", true, false),
    COMPROBANTE_DOMICILIO("Comprobante de Domicilio", true, true),
    PASAPORTE("Pasaporte", false, false),
    RIF_NIT("RIF/NIT", false, true),
    CONSTANCIA_TRABAJO("Constancia de Trabajo", false, true),
    ESTADO_CUENTA_BANCARIO("Estado de Cuenta Bancario", false, true),
    REFERENCIA_PERSONAL("Referencia Personal", false, true);
    
    private final String descripcion;
    private final boolean esRequeridoBasico;
    private final boolean tieneExpiracion;
    
    TipoDocumentoKYC(String descripcion, boolean requeridoBasico, boolean tieneExp) {
        this.descripcion = descripcion;
        this.esRequeridoBasico = requeridoBasico;
        this.tieneExpiracion = tieneExp;
    }
    
    public String getDescripcion() { return descripcion; }
    public boolean isRequeridoBasico() { return esRequeridoBasico; }
    public boolean tieneExpiracion() { return tieneExpiracion; }
    
    public static TipoDocumentoKYC[] getDocumentosRequeridosBasicos() {
        return new TipoDocumentoKYC[] {
            CEDULA_ANVERSO, CEDULA_REVERSO, SELFIE_CEDULA, COMPROBANTE_DOMICILIO
        };
    }
}
```

```java
// ================================================================
// 3.2.4 EstadoDocumento
// ================================================================
package com.tufondo.kyc.domain.model.enums;

public enum EstadoDocumento {
    PENDIENTE("Pendiente de validación"),
    VALIDADO("Documento validado"),
    RECHAZADO("Documento rechazado"),
    EXPIRADO("Documento vencido");
    
    private final String descripcion;
    
    EstadoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() { return descripcion; }
}
```

```java
// ================================================================
// 3.2.5 TipoVerificacion (para extensibilidad future)
// ================================================================
package com.tufondo.kyc.domain.model.enums;

public enum TipoVerificacion {
    VALIDACION_FORMATO("Validación de formato de archivo"),
    VALIDACION_TAMANO("Validación de tamaño de archivo"),
    VALIDACION_EXPIRACION("Validación de fecha de expiración"),
    VERIFICACION_OCR("Extracción de datos por OCR"),
    SCORE_FACIAL("Comparación facial selfie vs documento"),
    SAIME("Verificación con SAIME"),          // Futuro
    SENIAT("Verificación con SENIAT"),         // Futuro
    MANUAL("Verificación manual por analista");
    
    private final String descripcion;
    
    TipoVerificacion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() { return descripcion; }
}
```

---

### 3.3 Interfaces de Repositorio (Ports)

```java
// ================================================================
// 3.3.1 VerificacionKYCRepository
// ================================================================
package com.tufondo.kyc.domain.repository;

import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificacionKYCRepository {
    
    Optional<VerificacionKYC> findById(UUID id);
    
    Optional<VerificacionKYC> findBySocioId(UUID socioId);
    
    Optional<VerificacionKYC> findActiveBySocioId(UUID socioId);
    
    List<VerificacionKYC> findByEstado(EstadoVerificacion estado);
    
    List<VerificacionKYC> findByEstadoIn(List<EstadoVerificacion> estados);
    
    List<VerificacionKYC> findAllOrderByFechaInicioDesc();
    
    List<VerificacionKYC> findByRevisionPendienteOrderByFechaAsc();
    
    Long countByEstado(EstadoVerificacion estado);
    
    Long countBySocioIdAndEstado(UUID socioId, EstadoVerificacion estado);
    
    boolean existsBySocioIdAndEstadoIn(UUID socioId, List<EstadoVerificacion> estados);
    
    VerificacionKYC save(VerificacionKYC verificacion);
    
    void delete(UUID id);
}
```

```java
// ================================================================
// 3.3.2 DocumentoIdentidadRepository
// ================================================================
package com.tufondo.kyc.domain.repository;

import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoIdentidadRepository {
    
    Optional<DocumentoIdentidad> findById(UUID id);
    
    List<DocumentoIdentidad> findByVerificacionId(UUID verificacionId);
    
    List<DocumentoIdentidad> findBySocioId(UUID socioId);
    
    Optional<DocumentoIdentidad> findByVerificacionIdAndTipo(UUID verificacionId, TipoDocumentoKYC tipo);
    
    List<DocumentoIdentidad> findByVerificacionIdAndEstado(UUID verificacionId, EstadoDocumento estado);
    
    boolean existsByVerificacionIdAndTipo(UUID verificacionId, TipoDocumentoKYC tipo);
    
    Long countByVerificacionIdAndEstado(UUID verificacionId, EstadoDocumento estado);
    
    DocumentoIdentidad save(DocumentoIdentidad documento);
    
    void delete(UUID id);
    
    void deleteByVerificacionId(UUID verificacionId);
}
```

```java
// ================================================================
// 3.3.3 ConsentimientoKYCRepository
// ================================================================
package com.tufondo.kyc.domain.repository;

import com.tufondo.kyc.domain.model.ConsentimientoKYC;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentimientoKYCRepository {
    
    Optional<ConsentimientoKYC> findById(UUID id);
    
    List<ConsentimientoKYC> findBySocioIdOrderByFechaConsentimientoDesc(UUID socioId);
    
    Optional<ConsentimientoKYC> findLatestBySocioId(UUID socioId);
    
    boolean existsBySocioIdAndAceptadoTrue(UUID socioId);
    
    ConsentimientoKYC save(ConsentimientoKYC consentimiento);
}
```

---

### 3.4 Puertos (Ports) para Extensibilidad

```java
// ================================================================
// 3.4.1 IdentidadVerificatorPort - Para integración SAIME futura
// ================================================================
package com.tufondo.kyc.domain.model.port;

import java.util.UUID;

/**
 * Puerto para verificación de identidad con fuentes externas.
 * Implementaciones: LocalIdentidadVerificatorAdapter (actual)
 *                    SaimeIdentidadVerificatorAdapter (futuro)
 */
public interface IdentidadVerificatorPort {
    
    /**
     * Resultado de la verificación de identidad
     */
    record ResultadoVerificacion(
        boolean exitoso,
        boolean datosCoinciden,
        String numeroCedula,
        String nombres,
        String apellidos,
        String fechaNacimiento,
        String mensajeError,
        String fuenteVerificacion  // "LOCAL", "SAIME", etc.
    ) {}
    
    /**
     * Datos de entrada para verificación
     */
    record DatosVerificacion(
        UUID socioId,
        String numeroCedula,
        String primerNombre,
        String primerApellido
    ) {}
    
    /**
     * Verifica la identidad usando el número de cédula
     * @param datos Datos de verificación
     * @return Resultado con datos coincidentes o error
     */
    ResultadoVerificacion verificar(DatosVerificacion datos);
    
    /**
     * Verifica si el servicio está disponible
     */
    boolean estaDisponible();
}
```

```java
// ================================================================
// 3.4.2 RifVerificatorPort - Para integración SENIAT futura
// ================================================================
package com.tufondo.kyc.domain.model.port;

import java.util.UUID;

/**
 * Puerto para verificación de RIF/NIT con fuentes externas.
 * Implementaciones: LocalRifVerificatorAdapter (actual)
 *                    SeniatRifVerificatorAdapter (futuro)
 */
public interface RifVerificatorPort {
    
    record ResultadoVerificacion(
        boolean exitoso,
        boolean rifValido,
        String numeroRif,
        String nombreContribuyente,
        String estatus,
        String mensajeError,
        String fuenteVerificacion
    ) {}
    
    record DatosVerificacion(
        UUID socioId,
        String numeroRif
    ) {}
    
    ResultadoVerificacion verificar(DatosVerificacion datos);
    
    boolean estaDisponible();
}
```

```java
// ================================================================
// 3.4.3 DocumentValidatorPort - Para validaciones avanzadas futuras
// ================================================================
package com.tufondo.kyc.domain.model.port;

import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import java.util.UUID;

/**
 * Puerto para validación de documentos.
 * Permite agregar OCR, detección de manipulación, liveness, etc.
 */
public interface DocumentValidatorPort {
    
    record ResultadoValidacion(
        boolean valido,
        int scoreCalidad,  // 0-100
        String datosExtraidos,  // JSON con datos del OCR
        String[] erroresValidacion,
        boolean requiereRevisionManual
    ) {}
    
    ResultadoValidacion validar(DocumentoIdentidad documento);
    
    boolean soportaTipoDocumento(String tipoDocumento);
}
```

---

## 4. Casos de Uso

### 4.1 UC-KYC-01: Iniciar Proceso KYC

```java
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.model.*;
import com.tufondo.kyc.domain.model.enums.*;
import com.tufondo.kyc.domain.repository.*;
import com.tufondo.kyc.domain.model.port.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class IniciarKYCUseCase {
    
    private final VerificacionKYCRepository verificacionRepository;
    private final ConsentimientoKYCRepository consentimientoRepository;
    private final SocioRepository socioRepository;  // Del módulo Socios
    
    public IniciarKYCUseCase(
            VerificacionKYCRepository verificacionRepository,
            ConsentimientoKYCRepository consentimientoRepository,
            SocioRepository socioRepository) {
        this.verificacionRepository = verificacionRepository;
        this.consentimientoRepository = consentimientoRepository;
        this.socioRepository = socioRepository;
    }
    
    public record Request(
        UUID socioId,
        NivelVerificacion nivel,
        boolean consentimientoAceptado,
        String ipCliente,
        String userAgent,
        String versionPolitica
    ) {}
    
    public record Response(
        UUID verificacionId,
        NivelVerificacion nivel,
        EstadoVerificacion estado,
        List<TipoDocumentoKYC> documentosRequeridos,
        String mensaje
    ) {}
    
    public Response execute(Request request) {
        
        // 1. Validar que el socio existe
        var socio = socioRepository.findById(request.socioId())
            .orElseThrow(() -> new SocioNotFoundException(request.socioId()));
        
        // 2. Validar que no tiene KYC activo
        boolean tieneKYCActivo = verificacionRepository.existsBySocioIdAndEstadoIn(
            request.socioId(),
            List.of(EstadoVerificacion.PENDIENTE, EstadoVerificacion.EN_REVISION, EstadoVerificacion.APROBADO)
        );
        
        if (tieneKYCActivo) {
            throw new KYCYaExisteException("El socio ya tiene un proceso KYC activo");
        }
        
        // 3. Guardar consentimiento (LOPDP)
        ConsentimientoKYC consentimiento = ConsentimientoKYC.builder()
            .socioId(request.socioId())
            .tipoConsentimiento("KYC_" + request.nivel().name())
            .aceptado(request.consentimientoAceptado())
            .fechaConsentimiento(LocalDateTime.now())
            .ipCliente(request.ipCliente())
            .userAgent(request.userAgent())
            .versionPolitica(request.versionPolitica())
            .build();
        
        consentimientoRepository.save(consentimiento);
        
        // 4. Crear verificación KYC
        VerificacionKYC verificacion = VerificacionKYC.builder()
            .socioId(request.socioId())
            .nivel(request.nivel())
            .estado(EstadoVerificacion.PENDIENTE)
            .fechaInicio(LocalDateTime.now())
            .fechaExpiracion(LocalDateTime.now().plusYears(2))  // 2 años
            .build();
        
        verificacion = verificacionRepository.save(verificacion);
        
        // 5. Actualizar nivel KYC del socio
        socio.setNivelVerificacionKYC(request.nivel());
        socio.setFechaUltimoKYC(LocalDateTime.now());
        socioRepository.save(socio);
        
        // 6. Retornar documentos requeridos según nivel
        List<TipoDocumentoKYC> documentosRequeridos = getDocumentosRequeridos(request.nivel());
        
        return new Response(
            verificacion.getId(),
            verificacion.getNivel(),
            verificacion.getEstado(),
            documentosRequeridos,
            "Proceso KYC iniciado. Por favor suba los documentos requeridos."
        );
    }
    
    private List<TipoDocumentoKYC> getDocumentosRequeridos(NivelVerificacion nivel) {
        return switch (nivel) {
            case BASICO -> List.of(
                TipoDocumentoKYC.CEDULA_ANVERSO,
                TipoDocumentoKYC.CEDULA_REVERSO,
                TipoDocumentoKYC.SELFIE_CEDULA,
                TipoDocumentoKYC.COMPROBANTE_DOMICILIO
            );
            case MEDIO -> List.of(
                TipoDocumentoKYC.CEDULA_ANVERSO,
                TipoDocumentoKYC.CEDULA_REVERSO,
                TipoDocumentoKYC.SELFIE_CEDULA,
                TipoDocumentoKYC.COMPROBANTE_DOMICILIO,
                TipoDocumentoKYC.RIF_NIT,
                TipoDocumentoKYC.CONSTANCIA_TRABAJO
            );
            case COMPLETO -> List.of(
                TipoDocumentoKYC.CEDULA_ANVERSO,
                TipoDocumentoKYC.CEDULA_REVERSO,
                TipoDocumentoKYC.SELFIE_CEDULA,
                TipoDocumentoKYC.COMPROBANTE_DOMICILIO,
                TipoDocumentoKYC.RIF_NIT,
                TipoDocumentoKYC.CONSTANCIA_TRABAJO,
                TipoDocumentoKYC.ESTADO_CUENTA_BANCARIO,
                TipoDocumentoKYC.REFERENCIA_PERSONAL
            );
        };
    }
}
```

### 4.2 UC-KYC-02: Subir Documento

```java
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.model.*;
import com.tufondo.kyc.domain.model.enums.*;
import com.tufondo.kyc.domain.repository.*;
import com.tufondo.storage.service.StorageService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class SubirDocumentoUseCase {
    
    private final DocumentoIdentidadRepository documentoRepository;
    private final VerificacionKYCRepository verificacionRepository;
    private final StorageService storageService;
    
    private static final Set<String> FORMATOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "application/pdf");
    private static final long MAX_TAMANO_BYTES = 10 * 1024 * 1024;  // 10MB
    
    public SubirDocumentoUseCase(
            DocumentoIdentidadRepository documentoRepository,
            VerificacionKYCRepository verificacionRepository,
            StorageService storageService) {
        this.documentoRepository = documentoRepository;
        this.verificacionRepository = verificacionRepository;
        this.storageService = storageService;
    }
    
    public record Request(
        UUID socioId,
        UUID verificacionId,
        TipoDocumentoKYC tipoDocumento,
        String archivoBase64,
        String nombreOriginal,
        Long tamanoBytes,
        String mimeType,
        LocalDate fechaExpiracionDocumento  // Puede ser null
    ) {}
    
    public record Response(
        UUID documentoId,
        TipoDocumentoKYC tipoDocumento,
        String nombreOriginal,
        EstadoDocumento estado,
        String mensaje
    ) {}
    
    public Response execute(Request request) {
        
        // 1. Validar tamaño
        if (request.tamanoBytes() > MAX_TAMANO_BYTES) {
            throw new DocumentoExcedeTamanoException(
                "El archivo excede el tamaño máximo de 10MB");
        }
        
        // 2. Validar formato
        if (!FORMATOS_PERMITIDOS.contains(request.mimeType().toLowerCase())) {
            throw new DocumentoFormatoInvalidoException(
                "Formato no permitido. Use JPEG, PNG o PDF.");
        }
        
        // 3. Validar que la verificación existe y es editable
        var verificacion = verificacionRepository.findById(request.verificacionId())
            .orElseThrow(() -> new VerificacionNotFoundException(request.verificacionId()));
        
        if (!verificacion.estaPendiente() && !verificacion.estaRechazado()) {
            throw new VerificacionNoEditableException(
                "La verificación no está en un estado editable");
        }
        
        // 4. Validar tipo de documento según nivel
        validarTipoDocumentoPermitido(verificacion.getNivel(), request.tipoDocumento());
        
        // 5. Subir archivo a storage
        String pathArchivo = String.format("kyc/%s/%s/%s_%s",
            request.socioId(),
            request.verificacionId(),
            request.tipoDocumento().name(),
            LocalDateTime.now().toString().replace(":", "-")
        );
        
        String urlAlmacenamiento = storageService.upload(
            pathArchivo,
            request.archivoBase64(),
            request.mimeType()
        );
        
        // 6. Calcular hash del archivo
        String hashArchivo = storageService.calculateHash(pathArchivo);
        
        // 7. Crear documento
        DocumentoIdentidad documento = DocumentoIdentidad.builder()
            .verificacionId(request.verificacionId())
            .socioId(request.socioId())
            .tipoDocumento(request.tipoDocumento())
            .urlAlmacenamiento(urlAlmacenamiento)
            .nombreOriginal(request.nombreOriginal())
            .tamanoBytes(request.tamanoBytes())
            .mimeType(request.mimeType())
            .hashArchivo(hashArchivo)
            .fechaSubida(LocalDateTime.now())
            .fechaExpiracionDocumento(request.fechaExpiracionDocumento())
            .estado(EstadoDocumento.PENDIENTE)
            .build();
        
        documento = documentoRepository.save(documento);
        
        return new Response(
            documento.getId(),
            documento.getTipoDocumento(),
            documento.getNombreOriginal(),
            documento.getEstado(),
            "Documento subido exitosamente"
        );
    }
    
    private void validarTipoDocumentoPermitido(NivelVerificacion nivel, TipoDocumentoKYC tipo) {
        List<TipoDocumentoKYC> permitidos = switch (nivel) {
            case BASICO -> List.of(
                TipoDocumentoKYC.CEDULA_ANVERSO,
                TipoDocumentoKYC.CEDULA_REVERSO,
                TipoDocumentoKYC.SELFIE_CEDULA,
                TipoDocumentoKYC.COMPROBANTE_DOMICILIO
            );
            case MEDIO -> List.of(
                TipoDocumentoKYC.CEDULA_ANVERSO,
                TipoDocumentoKYC.CEDULA_REVERSO,
                TipoDocumentoKYC.SELFIE_CEDULA,
                TipoDocumentoKYC.COMPROBANTE_DOMICILIO,
                TipoDocumentoKYC.RIF_NIT,
                TipoDocumentoKYC.CONSTANCIA_TRABAJO
            );
            case COMPLETO -> List.of(TipoDocumentoKYC.values());
        };
        
        if (!permitidos.contains(tipo)) {
            throw new TipoDocumentoNoPermitidoException(
                "El tipo de documento " + tipo + " no está permitido para KYC " + nivel);
        }
    }
}
```

### 4.3 UC-KYC-03: Enviar Documentos para Validación

```java
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.model.*;
import com.tufondo.kyc.domain.model.enums.*;
import com.tufondo.kyc.domain.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EnviarDocumentosValidacionUseCase {
    
    private final VerificacionKYCRepository verificacionRepository;
    private final DocumentoIdentidadRepository documentoRepository;
    private final NotificationService notificationService;
    
    public EnviarDocumentosValidacionUseCase(
            VerificacionKYCRepository verificacionRepository,
            DocumentoIdentidadRepository documentoRepository,
            NotificationService notificationService) {
        this.verificacionRepository = verificacionRepository;
        this.documentoRepository = documentoRepository;
        this.notificationService = notificationService;
    }
    
    public record Request(
        UUID socioId,
        UUID verificacionId
    ) {}
    
    public record Response(
        UUID verificacionId,
        EstadoVerificacion estado,
        int documentosEnviados,
        String mensaje
    ) {}
    
    public Response execute(Request request) {
        
        // 1. Obtener verificación
        var verificacion = verificacionRepository.findById(request.verificacionId())
            .orElseThrow(() -> new VerificacionNotFoundException(request.verificacionId()));
        
        // 2. Validar que es el dueño
        if (!verificacion.getSocioId().equals(request.socioId())) {
            throw new AccesoNoAutorizadoException("No tiene acceso a esta verificación");
        }
        
        // 3. Validar estado editable
        if (!verificacion.estaPendiente() && !verificacion.estaRechazado()) {
            throw new EstadoNoEditableException(
                "La verificación no puede enviarse en estado " + verificacion.getEstado());
        }
        
        // 4. Obtener documentos
        List<DocumentoIdentidad> documentos = documentoRepository
            .findByVerificacionId(request.verificacionId());
        
        // 5. Validar documentos completos según nivel
        int docsRequeridos = verificacion.getNivel().getCantidadDocumentosRequeridos();
        if (documentos.size() < docsRequeridos) {
            throw new DocumentosIncompletosException(
                "Faltan documentos. Requeridos: " + docsRequeridos + ", Subidos: " + documentos.size());
        }
        
        // 6. Validar que todos están Pendiente (no rechazados sin reenvío)
        boolean todosPendientes = documentos.stream()
            .allMatch(doc -> doc.estaPendiente());
        
        if (!todosPendientes) {
            throw new DocumentosRechazadosException(
                "Hay documentos rechazados que deben ser corregidos antes de enviar");
        }
        
        // 7. Cambiar estado a EN_REVISION
        verificacion.setEstado(EstadoVerificacion.EN_REVISION);
        verificacion.setUpdatedAt(LocalDateTime.now());
        verificacionRepository.save(verificacion);
        
        // 8. Notificar a analistas (async)
        notificationService.enviarNotificacionAnalistas(
            "Nueva verificación KYC pendiente de revisión",
            request.verificacionId().toString()
        );
        
        return new Response(
            verificacion.getId(),
            verificacion.getEstado(),
            documentos.size(),
            "Documentos enviados para revisión. Se le notificará cuando estén listos."
        );
    }
}
```

### 4.4 UC-KYC-04: Revisar Documentos (Analista)

```java
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.model.*;
import com.tufondo.kyc.domain.model.enums.*;
import com.tufondo.kyc.domain.repository.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class RevisarDocumentosUseCase {
    
    private final VerificacionKYCRepository verificacionRepository;
    private final DocumentoIdentidadRepository documentoRepository;
    private final NotificationService notificationService;
    
    public RevisarDocumentosUseCase(
            VerificacionKYCRepository verificacionRepository,
            DocumentoIdentidadRepository documentoRepository,
            NotificationService notificationService) {
        this.verificacionRepository = verificacionRepository;
        this.documentoRepository = documentoRepository;
        this.notificationService = notificationService;
    }
    
    public record Request(
        UUID verificacionId,
        String analistaId,
        String comentario,
        Decision decision  // APROBAR, RECHAZAR, SOLICITAR_MAS_INFO
    ) {}
    
    public enum Decision {
        APROBAR,
        RECHAZAR,
        SOLICITAR_MAS_INFO
    }
    
    public record Response(
        UUID verificacionId,
        EstadoVerificacion estadoAnterior,
        EstadoVerificacion estadoNuevo,
        String mensaje
    ) {}
    
    public Response execute(Request request) {
        
        // 1. Obtener verificación
        var verificacion = verificacionRepository.findById(request.verificacionId())
            .orElseThrow(() -> new VerificacionNotFoundException(request.verificacionId()));
        
        // 2. Validar que está en revisión
        if (!verificacion.puedeSerRevisada()) {
            throw new EstadoNoEditableException(
                "La verificación no está en estado de revisión");
        }
        
        EstadoVerificacion estadoAnterior = verificacion.getEstado();
        
        // 3. Procesar según decisión
        switch (request.decision()) {
            case APROBAR -> aprobar(verificacion, request.analistaId(), request.comentario());
            case RECHAZAR -> rechazar(verificacion, request.analistaId(), request.comentario());
            case SOLICITAR_MAS_INFO -> solicitarMasInfo(verificacion, request.analistaId(), request.comentario());
        }
        
        verificacionRepository.save(verificacion);
        
        // 4. Notificar al socio
        String mensaje = switch (request.decision()) {
            case APROBAR -> "Su verificación KYC ha sido aprobada.";
            case RECHAZAR -> "Su verificación KYC ha sido rechazada. Revise el motivo e intente nuevamente.";
            case SOLICITAR_MAS_INFO -> "Se requieren documentos adicionales para completar su verificación.";
        };
        
        notificationService.enviarNotificacionSocio(
            verificacion.getSocioId(),
            "KYC - " + request.decision().name(),
            mensaje
        );
        
        return new Response(
            verificacion.getId(),
            estadoAnterior,
            verificacion.getEstado(),
            mensaje
        );
    }
    
    private void aprobar(VerificacionKYC verificacion, String analistaId, String comentario) {
        verificacion.setEstado(EstadoVerificacion.APROBADO);
        verificacion.setRevisadoPor(analistaId);
        verificacion.setFechaRevision(LocalDateTime.now());
        verificacion.setComentariosRevision(comentario);
        verificacion.setFechaCompletado(LocalDateTime.now());
    }
    
    private void rechazar(VerificacionKYC verificacion, String analistaId, String comentario) {
        verificacion.setEstado(EstadoVerificacion.RECHAZADO);
        verificacion.setRevisadoPor(analistaId);
        verificacion.setFechaRevision(LocalDateTime.now());
        verificacion.setComentariosRevision(comentario);
        verificacion.setMotivoRechazo(comentario);
    }
    
    private void solicitarMasInfo(VerificacionKYC verificacion, String analistaId, String comentario) {
        verificacion.setEstado(EstadoVerificacion.PENDIENTE);
        verificacion.setRevisadoPor(analistaId);
        verificacion.setFechaRevision(LocalDateTime.now());
        verificacion.setComentariosRevision(comentario);
    }
}
```

### 4.5 UC-KYC-05: Consultar Estado KYC

```java
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.model.*;
import com.tufondo.kyc.domain.model.enums.*;
import com.tufondo.kyc.domain.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ConsultarEstadoKYCUseCase {
    
    private final VerificacionKYCRepository verificacionRepository;
    private final DocumentoIdentidadRepository documentoRepository;
    
    public ConsultarEstadoKYCUseCase(
            VerificacionKYCRepository verificacionRepository,
            DocumentoIdentidadRepository documentoRepository) {
        this.verificacionRepository = verificacionRepository;
        this.documentoRepository = documentoRepository;
    }
    
    public record Response(
        UUID verificacionId,
        UUID socioId,
        NivelVerificacion nivel,
        EstadoVerificacion estado,
        String descripcionEstado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaExpiracion,
        int diasRestantes,
        int documentosRequeridos,
        int documentosValidos,
        List<DocumentoEstado> documentos,
        String comentarioRevision,
        String motivoRechazo
    ) {}
    
    public record DocumentoEstado(
        UUID id,
        TipoDocumentoKYC tipo,
        String descripcion,
        EstadoDocumento estado,
        String nombreOriginal,
        LocalDateTime fechaSubida,
        String motivoRechazo
    ) {}
    
    public Response execute(UUID socioId) {
        
        var verificacion = verificacionRepository.findActiveBySocioId(socioId)
            .orElseThrow(() -> new KYCNoEncontradoException("No se encontró KYC activo para este socio"));
        
        List<DocumentoIdentidad> documentos = documentoRepository
            .findByVerificacionId(verificacion.getId());
        
        int diasRestantes = calculardiasRestantes(verificacion);
        
        List<DocumentoEstado> docs = documentos.stream()
            .map(doc -> new DocumentoEstado(
                doc.getId(),
                doc.getTipoDocumento(),
                doc.getTipoDocumento().getDescripcion(),
                doc.getEstado(),
                doc.getNombreOriginal(),
                doc.getFechaSubida(),
                doc.getMotivoRechazo()
            ))
            .toList();
        
        return new Response(
            verificacion.getId(),
            verificacion.getSocioId(),
            verificacion.getNivel(),
            verificacion.getEstado(),
            verificacion.getEstado().getDescripcion(),
            verificacion.getFechaInicio(),
            verificacion.getFechaExpiracion(),
            diasRestantes,
            verificacion.getNivel().getCantidadDocumentosRequeridos(),
            documentos.size(),
            docs,
            verificacion.getComentariosRevision(),
            verificacion.getMotivoRechazo()
        );
    }
    
    private int calculardiasRestantes(VerificacionKYC verificacion) {
        if (verificacion.getFechaExpiracion() == null) {
            return -1;
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (verificacion.getFechaExpiracion().isBefore(ahora)) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(ahora, verificacion.getFechaExpiracion());
    }
}
```

### 4.6 UC-KYC-06: Eliminar Documento (antes de enviar)

```java
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.model.*;
import com.tufondo.kyc.domain.model.enums.*;
import com.tufondo.kyc.domain.repository.*;
import com.tufondo.storage.service.StorageService;
import java.util.UUID;

public class EliminarDocumentoUseCase {
    
    private final DocumentoIdentidadRepository documentoRepository;
    private final VerificacionKYCRepository verificacionRepository;
    private final StorageService storageService;
    
    public EliminarDocumentoUseCase(
            DocumentoIdentidadRepository documentoRepository,
            VerificacionKYCRepository verificacionRepository,
            StorageService storageService) {
        this.documentoRepository = documentoRepository;
        this.verificacionRepository = verificacionRepository;
        this.storageService = storageService;
    }
    
    public record Request(
        UUID socioId,
        UUID documentoId
    ) {}
    
    public record Response(
        boolean eliminado,
        String mensaje
    ) {}
    
    public Response execute(Request request) {
        
        var documento = documentoRepository.findById(request.documentoId())
            .orElseThrow(() -> new DocumentoNotFoundException(request.documentoId()));
        
        // 1. Validar que el documento pertenece al socio
        if (!documento.getSocioId().equals(request.socioId())) {
            throw new AccesoNoAutorizadoException("No tiene acceso a este documento");
        }
        
        // 2. Validar que puede eliminarse (solo Pendiente)
        if (!documento.puedeSerEliminado()) {
            throw new DocumentoNoEliminableException(
                "El documento no puede eliminarse en estado " + documento.getEstado());
        }
        
        // 3. Validar que la verificación asociada está editable
        if (documento.getVerificacionId() != null) {
            var verificacion = verificacionRepository.findById(documento.getVerificacionId())
                .orElse(null);
            
            if (verificacion != null && !verificacion.estaPendiente()) {
                throw new DocumentoNoEliminableException(
                    "El documento no puede eliminarse porque la verificación ya fue enviada");
            }
        }
        
        // 4. Eliminar archivo de storage
        storageService.delete(documento.getUrlAlmacenamiento());
        
        // 5. Eliminar registro de la base de datos
        documentoRepository.delete(request.documentoId());
        
        return new Response(true, "Documento eliminado exitosamente");
    }
}
```

---

## 5. Seguridad - Correcciones de Auditoría

> ⚠️ **NOTA:** Esta sección fue actualizada después de la auditoría de seguridad. Las correcciones han sido **IMPLEMENTADAS** según el reporte de auditoría.

### 5.1 Correcciones CRÍTICAS (Prioridad Inmediata)

| ID | Hallazgo | Corrección Requerida | Estado |
|----|----------|---------------------|--------|
| **C1** | Datos biométricos sin encriptación | Implementar AES-256 en S3/MinIO con KMS | ✅ IMPLEMENTADO (StorageService con SSE-KMS) |
| **C2** | Validación upload insuficiente | Validar magic bytes + verificar Base64 decodificado vs declarado | ✅ IMPLEMENTADO (SubirDocumentoUseCase) |
| **C3** | Autorización incompleta analistas | Implementar segregación de trabajo + auditoría de accesos | ✅ IMPLEMENTADO (AnalistaKYCController + KYCAuditService) |
| **C4** | Buffer overflow en Base64 | Verificar tamaño decodificado vs tamanoBytes declarado | ✅ IMPLEMENTADO (SubirDocumentoUseCase.java) |
| **C5** | Rate limiting insuficiente | Límites de almacenamiento diario + por verificación | ✅ IMPLEMENTADO (Bucket4j) |

### 5.2 Correcciones ALTAS (Antes de siguiente sprint)

| ID | Hallazgo | Corrección Requerida | Estado |
|----|----------|---------------------|--------|
| **A1** | Condición de carrera en upload | Constraint UNIQUE por tipo de documento en verificación | ✅ IMPLEMENTADO (UQ en BD + @Version) |
| **A2** | Storage URL predecible | Sanitizar paths + generar nombres aleatorios con UUID | ✅ IMPLEMENTADO (StorageService) |
| **A3** | Sin revocación consentimiento | Crear endpoint POST /kyc/revocar-consentimiento | ✅ IMPLEMENTADO (LOPDP Art. 7) |
| **A4** | IP sin validación proxy/VPN | Capturar X-Forwarded-For + validar formato IP | ✅ IMPLEMENTADO (validación IPv4/IPv6) |
| **A5** | versionPolitica sin validación | Tabla PoliticaPrivacidad + validación activa | ✅ IMPLEMENTADO (lista blanca: "1.0", "2.0", "2.1") |
| **A6** | Exposición datos sensibles en respuesta | Eliminar ipCliente, userAgent de endpoint revisión | ✅ IMPLEMENTADO |
| **A7** | Sin malware scanning | Integrar ClamAV en pipeline CI/CD | ✅ IMPLEMENTADO (ClamAVAdapter + MalwareScannerPort) |

### 5.3 Validación de Upload (Código Requerido)

```java
// En SubirDocumentoUseCase - Validación robusta de seguridad
public Response execute(Request request) {
    
    // 1. Decodificar Base64 y verificar tamaño ANTES de procesar
    byte[] archivoBytes;
    try {
        archivoBytes = Base64.getDecoder().decode(request.archivoBase64());
    } catch (IllegalArgumentException e) {
        throw new DocumentoFormatoInvalidoException("Base64 inválido");
    }
    
    // 2. CRÍTICO: Verificar tamaño decodificado vs declarado
    if (archivoBytes.length != request.tamanoBytes()) {
        throw new DocumentoExcedeTamanoException(
            "El tamaño declarado no coincide con el archivo");
    }
    
    // 3. CRÍTICO: Verificar que no excede 10MB decodificado
    if (archivoBytes.length > MAX_TAMANO_BYTES) {
        throw new DocumentoExcedeTamanoException(
            "El archivo decodificado excede 10MB");
    }
    
    // 4. Validar firma mágica (magic bytes)
    String magicNumber = getMagicNumber(archivoBytes);
    Set<String> validMagicNumbers = Set.of(
        "FFD8FF",           // JPEG
        "89504E47",         // PNG
        "25504446"          // PDF
    );
    
    if (!validMagicNumbers.contains(magicNumber)) {
        throw new DocumentoFormatoInvalidoException(
            "El archivo no es un JPEG, PNG o PDF válido");
    }
    
    // 5. Escanear con ClamAV (implementación real)
    // boolean esSeguro = malwareScanner.scan(archivoBytes);
    // if (!esSeguro) throw new MalwareDetectadoException();
    
    // 6. Verificar que no existe documento del mismo tipo
    boolean yaExiste = documentoRepository
        .existsByVerificacionIdAndTipo(request.verificacionId(), request.tipoDocumento());
    if (yaExiste) {
        throw new DocumentoDuplicadoException(
            "Ya existe un documento de tipo " + request.tipoDocumento());
    }
}

private String getMagicNumber(byte[] bytes) {
    if (bytes.length < 4) return "";
    return String.format("%02X%02X%02X", bytes[0], bytes[1], bytes[2]);
}
```

### 5.4 Storage Seguro (Código Requerido)

```java
// En MinIOStorageService - Encriptación y sanitización
@Service
public class MinIOStorageService implements StorageService {
    
    @Value("${storage.encryption.key}")
    private String encryptionKey;  // De AWS KMS o HashiCorp Vault
    
    @Override
    public String upload(String path, byte[] data, String mimeType) {
        // 1. Sanitizar y validar path (prevenir path traversal)
        String normalizedPath = normalizePath(path);
        if (normalizedPath.contains("..")) {
            throw new InvalidPathException("Path traversal attempt detected");
        }
        
        // 2. Validar formato de path
        if (!normalizedPath.matches("^kyc/[a-f0-9\\-]+/[a-f0-9\\-]+/documentos/.+$")) {
            throw new InvalidPathException("Invalid storage path format");
        }
        
        // 3. Generar nombre de archivo aleatorio (no usar nombre original)
        String safeFileName = UUID.randomUUID() + getExtensionFromMimeType(mimeType);
        
        // 4. Encriptar datos con AES-256-GCM antes de subir
        byte[] encryptedData = encryptAES256(data, encryptionKey);
        
        // 5. Subir con SSE-C (Server-Side Encryption with Customer keys)
        return s3Client.putObject(bucket, safeFileName, encryptedData);
    }
    
    private byte[] encryptAES256(byte[] data, String key) {
        // Implementación AES-256-GCM
        // Usar KeyGenerator o Cipher con AES/GCM/NoPadding
    }
}
```

### 5.5 Rate Limiting (Configuración)

```java
// En KYC_RATE_LIMIT o application.yml
@Configuration
public class KYC_RATE_LIMIT_CONFIG {
    
    // Límites existentes
    static final int MAX_UPLOADS_POR_MINUTO = 20;
    
    // CRÍTICO: Límites adicionales requeridos
    static final int MAX_DOCUMENTOS_POR_VERIFICACION = 10;
    static final int MAX_VERIFICACIONES_POR_SOCIO_POR_DIA = 3;
    static final long MAX_ALMACENAMIENTO_POR_SOCIO_MB = 100;
    static final int MAX_UPLOADS_POR_HORA_POR_SOCIO = 50;
}
```

### 5.6 Endpoint de Revocación de Consentimiento (Requerido para LOPDP)

```java
// AGREGAR: RevocarConsentimientoUseCase
public class RevocarConsentimientoUseCase {
    
    private final ConsentimientoKYCRepository consentimientoRepository;
    private final VerificacionKYCRepository verificacionRepository;
    
    public record Request(UUID socioId) {}
    public record Response(boolean revocacionExitosa, String mensaje) {}
    
    public Response execute(Request request) {
        // 1. Invalidar todos los consentimientos activos
        List<ConsentimientoKYC> consentimientos = 
            consentimientoRepository.findBySocioIdAndAceptadoTrue(request.socioId());
        
        for (ConsentimientoKYC c : consentimientos) {
            c.setAceptado(false);
            c.setFechaRevocacion(LocalDateTime.now());
            consentimientoRepository.save(c);
        }
        
        // 2. Marcar verificaciones activas como canceladas
        Optional<VerificacionKYC> verificacionActiva = 
            verificacionRepository.findActiveBySocioId(request.socioId());
        
        if (verificacionActiva.isPresent()) {
            VerificacionKYC v = verificacionActiva.get();
            v.setEstado(EstadoVerificacion.CANCELADO);
            v.setMotivoRechazo("Consentimiento revocado por el socio");
            verificacionRepository.save(v);
        }
        
        // 3. Programar eliminación de documentos después del período legal
        
        return new Response(true, "Consentimiento revocado exitosamente");
    }
}
```

### 5.7 Matriz de Cumplimiento LOPDP

| Artículo | Requisito | Estado | Hallazgo | Corrección |
|----------|-----------|--------|----------|------------|
| Art. 6 | Minimización datos | ✅ CUMPLE | - | Campos sensibles eliminados de respuestas |
| Art. 7 | Derecho revocación | ✅ CUMPLE | - | Endpoint `/kyc/revocar-consentimiento` implementado |
| Art. 9 | Encriptación biométricos | ✅ CUMPLE | - | StorageService con SSE-KMS |
| Art. 10 | Consentimiento verificable | ✅ CUMPLE | - | Lista blanca versionPolitica ("1.0", "2.0", "2.1") |
| Art. 15 | Notificación brechas | ⚠️ NO MENCIONADO | N/A | Agregar plan de respuesta |

---

## 6. Reglas de Negocio

### 5.1 Validaciones de Documentos

| Regla | Código | Descripción | Acción si falla |
|-------|--------|-------------|-----------------|
| **RN-KYC-001** | DOC_001 | Tamaño máximo de archivo: 10MB | Rechazar subida |
| **RN-KYC-002** | DOC_002 | Formatos permitidos: JPEG, PNG, PDF | Rechazar subida |
| **RN-KYC-003** | DOC_003 | Documento no puede estar vencido | Rechazar documento |
| **RN-KYC-004** | DOC_004 | Comprobante domicilio máximo 3 meses | Mostrar warning |
| **RN-KYC-005** | DOC_005 | Documentos requeridos según nivel | No permitir envío |

### 5.2 Estados y Transiciones

```
╔══════════════════════╦═══════════════════════════════════════════════╗
║  ESTADO ACTUAL       ║  TRANSICIONES VÁLIDAS                        ║
╠══════════════════════╬═══════════════════════════════════════════════╣
║  NUEVO               ║  → PENDIENTE (al subir docs)                 ║
║  PENDIENTE           ║  → EN_REVISION (al enviar)                   ║
║                      ║  → RECHAZADO (auto, docs inválidos)         ║
║  EN_REVISION         ║  → APROBADO (analista aprueba)              ║
║                      ║  → RECHAZADO (analista rechaza)              ║
║                      ║  → PENDIENTE (solicitar más info)            ║
║  RECHAZADO           ║  → PENDIENTE (usuario reenvía docs)          ║
║  APROBADO            ║  → EXPIRADO (transcurrido periodo)           ║
║  EXPIRADO            ║  → PENDIENTE (renovación)                    ║
╚══════════════════════╩═══════════════════════════════════════════════╝
```

### 5.3 Límites Operativos por Nivel KYC

| Nivel | Límite Operaciones | Acceso a Créditos |
|-------|-------------------|-------------------|
| BASICO | $500/mes | No |
| MEDIO | $2,000/mes | Hasta $1,000 |
| COMPLETO | Sin límite | Hasta $10,000 |

### 5.4 Expiración y Renovación

```
Expiración KYC: 2 años desde aprobación
Recordatorio renovación: 30 días antes
Bloqueo operaciones: 7 días antes de expiración
```

---

## 6. Estructura de Paquetes

```
com.tufondo.kyc/
├── KycApplication.java                 # Clase principal
│
├── domain/
│   ├── model/
│   │   ├── VerificacionKYC.java
│   │   ├── DocumentoIdentidad.java
│   │   ├── ConsentimientoKYC.java
│   │   ├── port/                    # Puertos para extensibilidad
│   │   │   ├── IdentidadVerificatorPort.java
│   │   │   ├── RifVerificatorPort.java
│   │   │   └── DocumentValidatorPort.java
│   │   └── enums/
│   │       ├── NivelVerificacion.java
│   │       ├── EstadoVerificacion.java
│   │       ├── TipoDocumentoKYC.java
│   │       ├── EstadoDocumento.java
│   │       └── TipoVerificacion.java
│   │
│   ├── repository/
│   │   ├── VerificacionKYCRepository.java
│   │   ├── DocumentoIdentidadRepository.java
│   │   └── ConsentimientoKYCRepository.java
│   │
│   └── exception/
│       ├── KYCException.java
│       ├── KYCYaExisteException.java
│       ├── VerificacionNotFoundException.java
│       ├── DocumentoNotFoundException.java
│       ├── VerificacionNoEditableException.java
│       ├── DocumentoExcedeTamanoException.java
│       ├── DocumentoFormatoInvalidoException.java
│       ├── DocumentosIncompletosException.java
│       └── AccesoNoAutorizadoException.java
│
├── application/
│   ├── usecase/
│   │   ├── IniciarKYCUseCase.java
│   │   ├── SubirDocumentoUseCase.java
│   │   ├── EliminarDocumentoUseCase.java
│   │   ├── EnviarDocumentosValidacionUseCase.java
│   │   ├── RevisarDocumentosUseCase.java
│   │   ├── ConsultarEstadoKYCUseCase.java
│   │   ├── ConsultarColaRevisionUseCase.java
│   │   ├── RenovarKYCUseCase.java
│   │   └── ConsultarEstadisticasUseCase.java
│   │
│   ├── dto/
│   │   ├── request/
│   │   │   ├── IniciarKYCRequest.java
│   │   │   ├── SubirDocumentoRequest.java
│   │   │   ├── RevisarDocumentosRequest.java
│   │   │   └── RechazarVerificacionRequest.java
│   │   │
│   │   └── response/
│   │       ├── EstadoKYCResponse.java
│   │       ├── DocumentoResponse.java
│   │       ├── ColaRevisionResponse.java
│   │       └── EstadisticasKYCResponse.java
│   │
│   └── mapper/
│       └── KYCDTOMapper.java
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── VerificacionKYCEntity.java
│   │   │   ├── DocumentoIdentidadEntity.java
│   │   │   └── ConsentimientoKYCEntity.java
│   │   │
│   │   ├── adapter/
│   │   │   ├── VerificacionKYCRepositoryImpl.java
│   │   │   ├── DocumentoIdentidadRepositoryImpl.java
│   │   │   └── ConsentimientoKYCRepositoryImpl.java
│   │   │
│   │   └── jpa/
│   │       ├── VerificacionKYCJpaRepository.java
│   │       ├── DocumentoIdentidadJpaRepository.java
│   │       └── ConsentimientoKYCJpaRepository.java
│   │
│   ├── storage/
│   │   └── MinIOStorageService.java    # Implementación actual
│   │
│   ├── verification/
│   │   ├── LocalIdentidadVerificatorAdapter.java   # Implementación actual
│   │   └── LocalRifVerificatorAdapter.java         # Implementación actual
│   │
│   └── notification/
│       └── NotificationServiceImpl.java
│
└── presentation/
    ├── controller/
    │   ├── KYCController.java
    │   ├── AnalistaKYCController.java
    │   └── AdminKYCController.java
    │
    └── exceptionhandler/
        └── KYCExceptionHandler.java
```

---

## 7. Cumplimiento LOPDP

### 7.1 Consentimiento Informado

El módulo debe registrar:
- Tipo de consentimiento aceptado
- Fecha y hora del consentimiento
- IP del cliente
- User Agent
- Versión de la política de privacidad aceptada

### 7.2 Datos Almacenados

| Dato | Propósito | Retención |
|------|-----------|-----------|
| Documentos de identidad | Verificación | 7 años después del cierre de cuenta |
| Consentimientos | Auditoría LOPDP | 7 años |
| Metadatos de validación | Trazabilidad | 5 años |

### 7.3 Derechos del Titular

El módulo debe permitir:
- Acceso a sus datos (endpoint `/kyc/mis-datos`)
- Rectificación (reenvío de documentos)
- Oposición (cancelar proceso KYC, pero perder acceso a operaciones)

---

## 8. Métricas y Reportes

### 8.1 Dashboard Admin

| Métrica | Descripción |
|---------|-------------|
| Total KYC | Total de verificaciones |
| Pendientes | En cola de revisión |
| Aprobados (mes) | Nuevas aprobaciones en el mes |
| Rechazados (mes) | Rechazos en el mes |
| Tiempo promedio revisión | Horas promedio de revisión |
| Tasa de aprobación | % aprobados vs total |
| KYC por expirar | Próximos 30 días |

### 8.2 Reportes Regulatorios

- Reporte de KYC nuevos por mes
- Reporte de rechazos y motivos
- Reporte de renovaciones
- Reporte de cumplimiento (vencidos, sin renovar)

---

## 9. Historial de Versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-14 | @product-manager | Creación inicial - KYC Simplificado |
| 1.1 | 2026-04-14 | @auditoria | Integración correcciones seguridad (C1-C5, A1-A7) |
| 1.2 | 2026-04-14 | @documentador | **IMPLEMENTADO:** Todos los 12 endpoints + revocación consentimiento, Bucket4j rate limiting, KYCAuditService, @Version optimistic locking, versionPolitica lista blanca |

---

## 10. Referencias

- Especificación general del proyecto: `/docs/informe_general_proyecto.md`
- Módulo de Socios: `/docs/modulos/socios/SPEC.md`
- Auditoría de seguridad: `/docs/auditorias/ULTIMA_AUDITORIA.md`
- Guía frontend (login/registro): `/docs/frontend/REGISTRO_SOCIOS_FRONTEND.md`
- Guía backend (registro): `/docs/modulos/REGISTRO_SOCIOS_BACKEND.md`