package taller.owasp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import taller.owasp.domain.SecretoCtf;

// Repositorio de la bandera del CTF. Solo lo usa Seed para insertarla.
public interface SecretoCtfRepository extends JpaRepository<SecretoCtf, Long> {
}
