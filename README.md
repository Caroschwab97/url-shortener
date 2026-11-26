# URL Shortener API

API REST para acortar URLs, hecha con **Java 21** y **Spring Boot 4**. Recibe una URL larga, devuelve un link corto (`http://localhost:8080/aZ3kP9q`) que redirige a la original y cuenta cuántas veces se usó.

## Funcionalidades

- Crea links cortos con códigos aleatorios de 7 caracteres (base62).
- Redirige con `302 Found` a la URL original.
- Cuenta los clics de cada link. El incremento es atómico en la base de datos, así que no se pierden clics con requests concurrentes.
- Valida la entrada: solo acepta URLs `http`/`https` con dominio, así que rechaza cosas como `javascript:` o `ftp://`.
- Devuelve los errores con el formato estándar [Problem Details (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457).
- Guarda los datos en H2 (en archivo), y los links sobreviven a reinicios.

## Tecnologías

| Área | Herramienta |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Validation) |
| Base de datos | H2 |
| Tests | JUnit 5 + MockMvc |
| Build | Maven (incluye wrapper) |

## Cómo correrlo

Requisitos: JDK 21 o superior. No hace falta instalar Maven, porque el proyecto trae el wrapper.

```bash
# Levantar la API en http://localhost:8080
./mvnw spring-boot:run

# Correr los tests
./mvnw test
```

En Windows, usa `mvnw.cmd` en lugar de `./mvnw`.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/links` | Crea un link corto |
| `GET` | `/api/links/{code}` | Devuelve los datos y estadísticas de un link |
| `GET` | `/{code}` | Redirige a la URL original y suma un clic |

### Crear un link

```bash
curl -i -X POST http://localhost:8080/api/links \
  -H "Content-Type: application/json" \
  -d '{"url": "https://github.com"}'
```

Respuesta `201 Created` (incluye el header `Location: /api/links/aZ3kP9q`):

```json
{
  "code": "aZ3kP9q",
  "shortUrl": "http://localhost:8080/aZ3kP9q",
  "originalUrl": "https://github.com",
  "clicks": 0,
  "createdAt": "2026-11-26T14:03:12.481Z"
}
```

### Usar el link corto

```bash
curl -i http://localhost:8080/aZ3kP9q
```

Respuesta `302 Found` con `Location: https://github.com`. Si lo abres en el navegador, te redirige directamente.

### Ver estadísticas

```bash
curl http://localhost:8080/api/links/aZ3kP9q
```

Respuesta `200 OK` con el mismo formato que al crear el link, con el contador `clicks` actualizado.

### Errores

Todos los errores tienen el mismo formato:

```json
{
  "type": "about:blank",
  "title": "URL inválida",
  "status": 400,
  "detail": "La URL debe empezar con http:// o https://",
  "instance": "/api/links"
}
```

| Status | Cuándo |
|---|---|
| `400 Bad Request` | La URL está vacía, supera los 2048 caracteres, no es `http`/`https` o no tiene dominio |
| `404 Not Found` | El código no existe |

## Estructura del proyecto

```
src/main/java/com/shortener/
├── UrlShortenerApplication.java     # Punto de entrada
└── link/
    ├── ShortLink.java               # Entidad JPA (tabla short_links)
    ├── ShortLinkRepository.java     # Acceso a datos + incremento atómico de clics
    ├── ShortLinkService.java        # Lógica: validar URL, generar código, resolver
    ├── ShortLinkController.java     # Endpoints REST
    ├── CodeGenerator.java           # Códigos aleatorios base62 con SecureRandom
    ├── dto/                         # Objetos de request/response
    └── exception/                   # Excepciones y handler global de errores
```

## Decisiones de diseño

- **Códigos aleatorios en lugar de IDs secuenciales.** Así nadie puede recorrer todos los links probando `/1`, `/2`, `/3`. Con 7 caracteres base62 hay unos 3,5 billones de combinaciones, y si sale un código repetido se genera otro.
- **`302` en lugar de `301`.** Un `301` lo cachea el navegador, y las visitas siguientes no pasarían por la API ni sumarían clics.
- **DTOs separados de la entidad.** La API no expone el `id` interno, y la base de datos puede cambiar sin romper el contrato.
- **Base de datos para desarrollo.** Durante el desarrollo, la consola web de H2 está disponible en `/h2-console` (JDBC URL: `jdbc:h2:file:./data/shortener`, usuario `sa`, sin contraseña).

## Próximos pasos

- [ ] Documentación interactiva con Swagger / OpenAPI
- [ ] Alias personalizados (`/mi-link`)
- [ ] Fecha de expiración de links
- [ ] Docker + PostgreSQL
- [ ] CI con GitHub Actions
- [ ] Frontend para crear links y ver estadísticas
