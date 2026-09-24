package com.Lab06.demo.security.abac;

import com.Lab06.demo.entity.Documento;
import com.Lab06.demo.entity.Politica;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.repository.PoliticaRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbacServiceTest {

    @Test
    void confidentialDocumentIsDeniedOnPersonalDevice() {
        PoliticaRepository repository = mock(PoliticaRepository.class);
        when(repository.findByActivaTrue()).thenReturn(List.of(
                new Politica("Dispositivo corporativo", "DISPOSITIVO_CORPORATIVO", "Requiere equipo corporativo", "DENY", true)
        ));
        Documento document = new Documento();
        document.setNivelConfidencialidad(5);

        AbacResult result = new AbacService(repository).evaluar(
                new Usuario(), document, "Consultar documento", LocalTime.NOON, false);

        assertFalse(result.isPermitido());
        assertTrue(result.getMotivo().contains("Dispositivo corporativo"));
    }

    @Test
    void confidentialDocumentIsAllowedOnCorporateDeviceWhenOtherPolicyConditionsAreAbsent() {
        PoliticaRepository repository = mock(PoliticaRepository.class);
        when(repository.findByActivaTrue()).thenReturn(List.of(
                new Politica("Dispositivo corporativo", "DISPOSITIVO_CORPORATIVO", "Requiere equipo corporativo", "DENY", true)
        ));
        Documento document = new Documento();
        document.setNivelConfidencialidad(5);

        AbacResult result = new AbacService(repository).evaluar(
                new Usuario(), document, "Consultar documento", LocalTime.NOON, true);

        assertTrue(result.isPermitido());
    }

    @Test
    void devicePolicyDoesNotRestrictLowerConfidentialityDocuments() {
        PoliticaRepository repository = mock(PoliticaRepository.class);
        when(repository.findByActivaTrue()).thenReturn(List.of(
                new Politica("Dispositivo corporativo", "DISPOSITIVO_CORPORATIVO", "Requiere equipo corporativo", "DENY", true)
        ));
        Documento document = new Documento();
        document.setNivelConfidencialidad(3);

        AbacResult result = new AbacService(repository).evaluar(
                new Usuario(), document, "Consultar documento", LocalTime.NOON, false);

        assertTrue(result.isPermitido());
    }
}
