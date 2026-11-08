# M1 · Inyección SQL

**Ataque:** el texto del usuario se pega dentro de la consulta SQL. Con `admin' --`
se entra sin contraseña, con una comilla se ve el error de la base y con
`UNION SELECT` se leen otras tablas (usuarios y contraseñas).
**Cómo probarlo:** `requests/01-sqli.http` (bloques 1a/1b, 2a/2b y 3a/3b).
**Marcador:** `M1` · `Login sin contraseña (admin' --)`, `Error SQL visible (comilla)`,
`UNION SELECT a users` y `Reto CTF (secreto_ctf)`, con `exito` en 3000 y `bloqueado` en 3001.

> Ojo: Hibernate parametriza por defecto, así que **no** es vulnerable si se usan
> repositorios. La versión vulnerable usa a propósito una consulta nativa concatenada.

## 1. Login: consulta concatenada → consulta parametrizada

**Archivo:** `src/main/java/taller/owasp/service/UserService.java`

Antes (vulnerable):

```java
String sql = "SELECT * FROM users WHERE username = '" + username
           + "' AND password = '" + password + "'";
return entityManager.createNativeQuery(sql, User.class).getResultList();
```

Con `username = admin' --` la consulta queda
`... WHERE username = 'admin' --' AND password = '...'`: el `--` comenta la
comprobación de la contraseña.

Después (corregido, paso de M1):

```java
// UserRepository (Spring Data genera: WHERE username = ? AND password = ?)
Optional<User> findByUsernameAndPassword(String username, String password);
```

**Por qué:** con parámetros, la base recibe el texto como **dato**, nunca como
código SQL; `admin' --` es solo un usuario que no existe → 401.

> En el código final, M2 (B) reemplaza este paso por `findByUsername` +
> `passwordEncoder.matches(...)`, porque las contraseñas pasan a guardarse con
> BCrypt. La protección contra la inyección es la misma: consulta parametrizada.

## 2. Búsqueda de productos: concatenación → repositorio

**Archivo:** `src/main/java/taller/owasp/service/ProductService.java`

Antes (vulnerable):

```java
String sql = "SELECT id, name, description, price FROM products "
           + "WHERE name ILIKE '%" + q + "%'";
List<?> filas = entityManager.createNativeQuery(sql).getResultList();
```

Después (corregido):

```java
// ProductRepository
List<Product> findByNameContainingIgnoreCase(String name);

// ProductService.buscar
return productRepository.findByNameContainingIgnoreCase(name);
```

**Por qué:** la comilla y el `UNION SELECT` se buscan como texto literal en el
nombre del producto: no rompen la consulta ni la amplían → lista vacía.

## 3. No mostrar el error de la base al cliente

**Archivo:** `src/main/java/taller/owasp/patched/SqliPatchedRestController.java`
(antes: `vulnerable/SqliVulnerableRestController.java`)

Antes (vulnerable):

```java
} catch (Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
}
```

Después (corregido):

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<String> manejarError(Exception e) {
    log.error("Error en SqliPatchedRestController", e);       // detalle solo en el log
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Ocurrió un error. Intenta de nuevo.");      // mensaje genérico
}
```

**Por qué:** el mensaje de la base le dice al atacante qué motor es, cómo es la
consulta y cuántas columnas tiene; eso es justo lo que necesita para armar el `UNION`.

## Resultado esperado

| Ataque | 3000 (vulnerable) | 3001 (corregido) |
|---|---|---|
| `admin' --` en el login | 200, entra como admin (y la respuesta trae la contraseña) | 401 |
| `'` en `q` | 500 con el error de SQL de H2 | 200, `[]` |
| `' UNION SELECT id, username, password, 0 FROM users --` | usuarios y contraseñas en la lista de productos | 200, `[]` |
