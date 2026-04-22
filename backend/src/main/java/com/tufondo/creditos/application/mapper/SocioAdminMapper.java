// com/tufondo/creditos/application/mapper/SocioAdminMapper.java
package com.tufondo.creditos.application.mapper;

import com.tufondo.socios.domain.model.Socio;
import org.springframework.stereotype.Component;

@Component
public class SocioAdminMapper {

    public String toNombreCompleto(Socio socio) {
        if (socio == null) return "";

        StringBuilder nombre = new StringBuilder();
        if (socio.getPrimerNombre() != null) nombre.append(socio.getPrimerNombre());
        if (socio.getSegundoNombre() != null) nombre.append(" ").append(socio.getSegundoNombre());
        if (socio.getPrimerApellido() != null) nombre.append(" ").append(socio.getPrimerApellido());
        if (socio.getSegundoApellido() != null) nombre.append(" ").append(socio.getSegundoApellido());
        return nombre.toString().trim();
    }

    public String toNumeroSocio(Socio socio) {
        if (socio == null) return "";
        return socio.getNumeroSocio() != null ? socio.getNumeroSocio() : "";
    }

    public String toCedula(Socio socio) {
        if (socio == null) return "";
        if (socio.getTipoDocumento() == null || socio.getNumeroDocumento() == null) return "";
        return socio.getTipoDocumento().name() + "-" + socio.getNumeroDocumento();
    }

    public String toCorreo(Socio socio) {
        if (socio == null) return "";
        return socio.getCorreoElectronico() != null ? socio.getCorreoElectronico() : "";
    }

    public String toEmpresa(Socio socio) {
        if (socio == null) return "";
        return socio.getEmpresa() != null ? socio.getEmpresa() : "";
    }
}