# M2 · Datos sensibles y hashing

**Ataque:** un login válido en la versión vulnerable responde con la **entidad `User` completa**, con la contraseña en claro; además la tabla `users` guarda las contraseñas sin cifrar (visibles en la consola H2).
**Cómo probarlo:** `requests/02-datos-sensibles.http` (bloques 1a/1b) y la **consola H2** en `http://localhost:3000/h2-console` (JDBC `jdbc:h2:mem:tienda`, usuario `sa`, sin contraseña) → `SELECT * FROM users`.
**Marcador:** registra `M2 · Contraseña expuesta en el login` con `exito` en 3000 (la respuesta lleva la contraseña) y `bloqueado` en 3001 (responde un DTO sin contraseña).

## 1. Guardar las contraseñas con BCrypt, no en claro

**Archivo:** `src/main/java/taller/owasp/shared/UserSeed.java`

Antes (vulnerable):

```java
userRepository.save(new User("maria", "sunshine", null, "USER")); // texto plano
```

Después (corregido):

```java
userRepository.save(new User("maria", passwordEncoder.encode("sunshine"), null, "USER"));
```

**Por qué:** `BCryptPasswordEncoder` guarda un hash con sal (`$2a$...`). Aunque se filtre la tabla, no se recupera la contraseña original.

## 2. No verificar comparando texto; usar `matches(...)`

**Archivo:** `src/main/java/taller/owasp/patched/SqliPatchedRestController.java`

Antes (vulnerable — `vulnerable/SqliVulnerableRestController.java`):

```java
List<User> usuarios = userService.loginInseguro(username, password); // compara texto
User u = usuarios.get(0);
return ResponseEntity.ok(u); // devuelve la entidad con la contraseña
```

Después (corregido):

```java
Optional<User> encontrado = userService.findByUsername(datos.username());
if (encontrado.isEmpty()
        || !passwordEncoder.matches(datos.password(), encontrado.get().getPassword())) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)...;
}
return ResponseEntity.ok(new LoginResponse(u.getId(), u.getUsername(), u.getRole())); // DTO
```

**Por qué:** se compara el hash con `matches(...)` y se responde con un **DTO** `{id, username, role}`. La contraseña nunca sale del servidor.

## Resultado esperado

| Ataque | 3000 (vulnerable) | 3001 (corregido) |
|---|---|---|
| Login válido (`maria`/`sunshine`) | 200 con la entidad completa (incluye `password`) | 200 con un DTO sin `password` |
| `SELECT * FROM users` en la consola H2 | Contraseñas en claro | Hashes `$2a$...` |
