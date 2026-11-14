package com.biofer.ventas.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(
    name = "vendedores",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_vendedor_documento",
        columnNames = {"tipo_documento", "numero_documento"}
    )
)
public class Vendedor extends PersonaComercial {

    private LocalDate fechaNacimiento;

    @Column(length = 30)
    private String telefonoAlternativo;

    @Column(length = 250)
    private String direccion;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String provincia;

    @Column(length = 100)
    private String ciudad;

    @Column(nullable = false, length = 3)
    private String monedaPago;

    private LocalDate fechaCese;

    public Vendedor() {
    }

    public boolean isVigente() {
        LocalDate hoy = LocalDate.now(
            ZoneId.of("America/Lima")
        );

        return isActivo()
                && (fechaCese == null || !hoy.isAfter(fechaCese));
    }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefonoAlternativo() {
        return telefonoAlternativo;
    }
    public void setTelefonoAlternativo(String telefonoAlternativo) {
        this.telefonoAlternativo = telefonoAlternativo;
    }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getMonedaPago() { return monedaPago; }
    public void setMonedaPago(String monedaPago) {
        this.monedaPago = monedaPago;
    }

    public LocalDate getFechaCese() { return fechaCese; }
    public void setFechaCese(LocalDate fechaCese) {
        this.fechaCese = fechaCese;
    }
}
