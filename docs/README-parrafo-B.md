# Párrafo de la Persona B para el README

Contenido que B aporta al README: M2 Protección de datos sensibles, M6 Fuerza bruta, instalación, ejecución, aviso ético y convenciones.

## M2 - Protección de datos sensibles

- **Vulnerable (puerto 3000):** almacena las contraseñas de los usuarios en texto plano.
- **Seguro (puerto 3001):** utiliza BCrypt para almacenar las contraseñas mediante hashes, evitando guardar las contraseñas originales.
- El inicio de sesión seguro utiliza `PasswordEncoder` de Spring Security.

## M6 - Protección contra fuerza bruta

La versión segura implementa un límite de intentos de inicio de sesión:

- Se permiten 5 intentos por dirección IP.
- La ventana de tiempo es de 60 segundos.
- El sexto intento recibe HTTP `429 Too Many Requests`.
- Se utilizan las cabeceras `RateLimit-Limit`, `RateLimit-Remaining`, `RateLimit-Reset` y `Retry-After`.

Material:

- `requests/06-rate-limit.http`
- `soluciones/06-rate-limit.md`

## Instalación y ejecución

### Requisitos

- Java 21.
- Git.
- Maven Wrapper incluido en el proyecto.

### Preparar el proyecto

Clonar el repositorio:

```bash
git clone https://github.com/eilynrivera/TallerDSIV.git
cd TallerDSIV
```

### Verificar el proyecto

Ejecutar las pruebas:

```powershell
.\mvnw.cmd clean test
```

### Ejecutar la aplicación

Cada perfil debe ejecutarse en una terminal independiente.

**Versión vulnerable (puerto 3000):**

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=vulnerable"
```

**Versión segura (puerto 3001):**

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=patched"
```

**Versión maliciosa (puerto 4000):**

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=evil"
```

La aplicación utiliza una base de datos H2 en memoria, por lo que los datos se reinician al detener la aplicación.

## Aviso ético

Este proyecto tiene fines educativos y permite demostrar vulnerabilidades y mecanismos de protección.

Las pruebas deben realizarse únicamente en el entorno local del proyecto o en sistemas donde se tenga autorización expresa. No se deben utilizar estas técnicas contra sistemas reales sin permiso.

## Convenciones del proyecto

- `vulnerable`: contiene implementaciones con vulnerabilidades para fines demostrativos.
- `patched`: contiene las implementaciones corregidas o protegidas.
- `evil`: contiene recursos utilizados para demostrar determinados ataques.
- `shared`: contiene componentes compartidos entre perfiles.
- `requests/`: almacena las solicitudes HTTP utilizadas en las pruebas.
- `soluciones/`: contiene la documentación y explicación de las pruebas.
- Los cambios deben realizarse en la rama de trabajo correspondiente y registrarse mediante commits.