# M6 - Rate Limiting

## Objetivo

Demostrar cómo la limitación de intentos protege el inicio de sesión contra ataques de fuerza bruta y diccionario.

## Funcionamiento

La versión protegida implementa un límite de intentos por dirección IP:

- Se permiten hasta 5 intentos de inicio de sesión.
- El límite se aplica al endpoint `POST /api/login`.
- La ventana de tiempo es de 60 segundos.
- Al superar el límite, el servidor responde con HTTP `429 Too Many Requests`.
- La respuesta incluye la cabecera `Retry-After`, que indica cuántos segundos faltan para poder intentar nuevamente.

## Cabeceras

El filtro devuelve información sobre el límite mediante estas cabeceras:

| Cabecera | Descripción |
|---|---|
| `RateLimit-Limit` | Cantidad máxima de intentos permitidos. |
| `RateLimit-Remaining` | Intentos que todavía quedan disponibles. |
| `RateLimit-Reset` | Segundos restantes de la ventana actual. |
| `Retry-After` | Tiempo que se debe esperar después de recibir HTTP 429. |

## Prueba

1. Levantar el perfil `patched` en el puerto 3001.
2. Abrir `requests/06-rate-limit.http`.
3. Ejecutar las primeras cinco solicitudes.
4. Comprobar que responden HTTP 401 por las credenciales incorrectas.
5. Ejecutar la sexta solicitud.
6. Comprobar que responde HTTP 429.

## Resultado esperado

El servidor permite cinco intentos fallidos de inicio de sesión dentro de la ventana de 60 segundos. El sexto intento es bloqueado temporalmente.

Esto dificulta los ataques automatizados de fuerza bruta y diccionario contra el endpoint de autenticación.