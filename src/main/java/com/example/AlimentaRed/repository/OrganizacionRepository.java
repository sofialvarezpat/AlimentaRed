package com.example.AlimentaRed.repository;

import com.example.AlimentaRed.entity.Organizacion;
import com.example.AlimentaRed.entity.enums.TipoOrganizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizacionRepository extends JpaRepository<Organizacion, Long> {

    List<Organizacion> findByTipo(TipoOrganizacion tipo);
}
