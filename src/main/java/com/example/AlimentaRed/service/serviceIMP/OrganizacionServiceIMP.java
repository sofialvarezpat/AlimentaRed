package com.example.AlimentaRed.service.serviceIMP;

import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.dto.OrganizacionRequest;
import com.example.AlimentaRed.entity.Organizacion;
import com.example.AlimentaRed.entity.enums.TipoOrganizacion;
import com.example.AlimentaRed.repository.OrganizacionRepository;
import com.example.AlimentaRed.service.OrganizacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizacionServiceIMP implements OrganizacionService {

    private final OrganizacionRepository organizacionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Organizacion> listarOrganizaciones() {
        return organizacionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organizacion> listarPorTipo(TipoOrganizacion tipo) {
        return organizacionRepository.findByTipo(tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public Organizacion buscarPorId(Long id) {
        return organizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada con id " + id));
    }

    @Override
    @Transactional
    public Organizacion crearOrganizacion(OrganizacionRequest request) {
        Organizacion organizacion = new Organizacion();
        organizacion.setNombre(request.nombre());
        organizacion.setTipo(request.tipo());
        organizacion.setDireccion(request.direccion());
        organizacion.setTelefono(request.telefono());

        return organizacionRepository.save(organizacion);
    }

    @Override
    @Transactional
    public Organizacion actualizarOrganizacion(Long id, OrganizacionRequest request) {
        Organizacion organizacion = buscarPorId(id);

        organizacion.setNombre(request.nombre());
        organizacion.setTipo(request.tipo());
        organizacion.setDireccion(request.direccion());
        organizacion.setTelefono(request.telefono());

        return organizacionRepository.save(organizacion);
    }

    @Override
    @Transactional
    public void eliminarOrganizacion(Long id) {
        Organizacion organizacion = buscarPorId(id);
        organizacionRepository.delete(organizacion);
    }
}