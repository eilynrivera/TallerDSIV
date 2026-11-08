# Extra (A11) · IDOR: leer los datos de otro usuario

> Módulo **opcional**: no está en la agenda de 60 minutos. Sirve como ejercicio extra
> o si sobra tiempo.

**Ataque:** `GET /api/users/{id}` solo comprueba que haya sesión. `maria` inicia
sesión, cambia el número de la URL y lee los datos de `admin` (contraseña incluida).
**Cómo probarlo:** `requests/07-idor.http`.
**Marcador:** `IDOR` · `Leer el perfil de otro usuario`, con `exito` en 3000 y `bloqueado` en 3001.

## 1. Comprobar que el recurso es del usuario de la sesión

**Archivo:** `src/main/java/taller/owasp/patched/IdorPatchedRestController.java`
(antes: `vulnerable/IdorVulnerableRestController.java`)

Antes (vulnerable):

```java
if (!(session.getAttribute(SessionUser.ATRIBUTO_SESION) instanceof SessionUser actual)) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
}
return userRepository.findById(id).map(ResponseEntity::ok)...;   // cualquier id
```

Después (corregido):

```java
if (!id.equals(actual.id())) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("mensaje", "No puedes ver los datos de otro usuario"));
}
return ... new UsuarioResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole());
```

**Por qué:** estar **autenticado** (hay sesión) no es lo mismo que estar
**autorizado** a ver ese objeto. Cada petición debe comprobar que el recurso
pertenece a quien lo pide; además se responde con un DTO sin contraseña.

## Resultado esperado

| Prueba | 3000 (vulnerable) | 3001 (corregido) |
|---|---|---|
| `maria` pide `/api/users/1` (admin) | 200 con los datos de admin y `admin123` | 403 |
| `maria` pide su propio id | 200 con contraseña | 200 sin contraseña |
| Sin sesión | 401 | 401 |
