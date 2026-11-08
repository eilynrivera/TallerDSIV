# PLANTILLA de las soluciones (definida por A en M1; la usan los tres)

Copiar como `soluciones/0X-<tema>.md`. Una sección por cada cambio, siempre con
las mismas tres partes: **archivo**, **antes / después** y **por qué** (una o dos
líneas). Lo que esté entre `< >` se reemplaza.

---

# M<N> · <Nombre del módulo>

**Ataque:** <una línea: qué hace el atacante y qué consigue en 3000>.
**Cómo probarlo:** `requests/0X-<tema>.http` (bloques 1a/1b, 2a/2b…).
**Marcador:** <qué ataques registra, con `exito` en 3000 y `bloqueado` en 3001>.

## 1. <Nombre del cambio>

**Archivo:** `src/main/java/taller/owasp/<paquete>/<Clase>.java`

Antes (vulnerable):

```java
// código vulnerable
```

Después (corregido):

```java
// código corregido
```

**Por qué:** <una o dos líneas>.

## Resultado esperado

| Ataque | 3000 (vulnerable) | 3001 (corregido) |
|---|---|---|
| <ataque> | <qué pasa> | <qué pasa> |
