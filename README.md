# Sushi Craft — API REST

**Bitácora DOSW · Corte 2**  
**Autor:** Santiago García

API para el restaurante Kaze & Nori, construida con Java 17 y Spring Boot. Incluye siete dominios, persistencia relacional en PostgreSQL, eventos de pedidos en MongoDB, autenticación JWT y documentación con Swagger.

## Funcionalidades

- CRUD de platos, mesas, pedidos, ítems, cuentas, reservas y vehículos.
- Menú público de platos disponibles.
- Historial de creación y cambios de estado de pedidos en MongoDB.
- JWT y autorización por roles: GERENTE, MESERO, COCINERO y CLIENTE.
- Manejo uniforme de errores y documentación OpenAPI.

## Arquitectura y diagramas

Los diagramas editables están en [`docs/diagramas`](docs/diagramas):

- [Contexto](docs/diagramas/diagrama-contexto-sushicraft.drawio)
- [Clases](docs/diagramas/diagrama-clases-sushicraft.drawio) · [Relaciones](docs/diagramas/diagrama-clases.md)
- [Secuencia: crear plato](docs/diagramas/diagrama-secuencia-crear-plato.drawio)
- [Modelo relacional](docs/diagramas/modelo-relacional-sushicraft.drawio)
- [Modelo no relacional](docs/diagramas/modelo-no-relacional-sushicraft.drawio)
- [Componentes de Pedidos](docs/diagramas/diagrama-componentes-especifico-sushicraft.drawio)
- [Componentes generales](docs/diagramas/diagrama-componentes-general-sushicraft.drawio)
- [Despliegue](docs/diagramas/diagrama-despliegue-sushicraft.drawio)

## Ejecución local

Con Docker Compose:

```bash
cp .env.example .env
# Define JWT_SECRET en .env
docker compose up --build -d
```

Swagger: `http://localhost:8080/swagger-ui/index.html`  
Estado de servicios: `docker compose ps` · Logs de la API: `docker compose logs api`

## API y seguridad

Swagger contiene el detalle de rutas y parámetros. Los recursos principales son `/api/v1/platos`, `/menu`, `/mesas`, `/pedidos`, `/items-pedido`, `/cuentas`, `/reservas` y `/vehiculos`. El historial se consulta en `/api/v1/pedidos/{idPedido}/eventos`.

`POST /auth/login` devuelve un JWT. En Swagger, pulsa **Authorize** y usa `Bearer <token>`. El menú es público; los demás endpoints requieren autenticación y las operaciones de escritura requieren el rol correspondiente.

Usuarios de prueba (contraseña: `sushicraft123`):

| Correo | Rol |
|---|---|
| `gerente@sushicraft.com` | GERENTE |
| `mesero@sushicraft.com` | MESERO |
| `cocinero@sushicraft.com` | COCINERO |
| `cliente@sushicraft.com` | CLIENTE |

## Pruebas, JaCoCo y SonarQube

Ejecuta pruebas y genera el reporte JaCoCo:

```bash
mvn clean verify
```

Reporte HTML: `target/site/jacoco/index.html`. La última ejecución local reportó **103 pruebas, 0 fallos y 0 errores**.

SonarQube Community se ejecuta localmente en Docker, puerto `9000`. Crea el proyecto `SushiCraft` con la clave configurada en `pom.xml` y ejecuta el análisis Maven con un token local. No publiques tokens en el repositorio.

## Docker

La imagen usa Maven para compilar y Java 17 para ejecutar. Variables principales:

| Variable | Uso |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Conexión PostgreSQL |
| `MONGO_DB` | Base de eventos MongoDB |
| `JWT_SECRET` | Firma de tokens JWT |
| `JWT_EXPIRATION` | Vigencia del token en milisegundos (24 h por defecto) |

## CI/CD y Azure

GitHub Actions ejecuta `mvn clean verify`, publica en Docker Hub las etiquetas `latest` y el hash del commit, despliega a QA y luego a PROD. PROD requiere aprobación manual mediante el environment `production` de GitHub.

Secrets requeridos en GitHub Actions: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `AZURE_WEBAPP_PUBLISH_PROFILE_QA` y `AZURE_WEBAPP_PUBLISH_PROFILE_PROD`.

| Ambiente | Swagger | Datos |
|---|---|---|
| QA | [Abrir Swagger QA](https://sushicraft-santiago-qa-2026-a0cgheh5pf0bwck.eastus2-01.azurewebsites.net/swagger-ui/index.html) | Base `sushicraft` |
| PROD | [Abrir Swagger PROD](https://sushicraft-santiago-prod-2026-abeuescabpa3h6cz.eastus2-01.azurewebsites.net/swagger-ui/index.html) | Base `sushicraft_prod` |

QA y PROD usan el mismo servidor PostgreSQL y clúster MongoDB, con bases lógicas distintas. Por ello comparten infraestructura, aunque los datos de cada ambiente están separados por base.

## Evidencias

### CI/CD y despliegue

![Pipeline CI/CD](https://github.com/user-attachments/assets/1865b355-cd99-4f33-b345-bab92d56900c)

![Swagger QA en Azure](https://github.com/user-attachments/assets/2853041a-fd1a-4ac8-9670-c557d19c63ff)

![Swagger PROD en Azure](https://github.com/user-attachments/assets/f56ff76d-4ebf-430d-a25e-4635a99c1740)

![Imagen y tags de Docker Hub](https://github.com/user-attachments/assets/a445f533-945d-426d-8abe-cede456c7d2d)

### Swagger

<img width="1916" height="997" alt="Endpoints Swagger 1" src="https://github.com/user-attachments/assets/dc318df3-0ab8-45cf-9ac3-cff2c52e1e51" />

<img width="1513" height="798" alt="Endpoints Swagger 2" src="https://github.com/user-attachments/assets/17450ae6-7543-4359-8ef7-66284f0dfbb3" />

<img width="1466" height="861" alt="Endpoints Swagger 3" src="https://github.com/user-attachments/assets/11137ff2-9908-4b61-ac27-bd1783faa501" />

<img width="1916" height="1001" alt="Endpoints Swagger 4" src="https://github.com/user-attachments/assets/0755d64d-accf-4448-978d-cd12232d8dc1" />

### JaCoCo y SonarQube

<img width="1918" height="376" alt="Cobertura JaCoCo" src="https://github.com/user-attachments/assets/5b53b4e6-0e1b-435c-bd04-cd4988687f9f" />

![SonarQube antes](https://github.com/user-attachments/assets/7b24d6e5-0ed8-4510-8c86-6f35fdc2f4c7)

![Issues antes](docs/evidencias/sonar-issues-antes.png)

![SonarQube después](docs/evidencias/sonar-overview.png)

![Issues después](docs/evidencias/sonar-issues-despues.png)

### Ejecución, autenticación y persistencia

<img width="1201" height="546" alt="Ejecución de la aplicación" src="https://github.com/user-attachments/assets/9e725b09-916b-4436-aff7-c2752d64ae79" />

![Login exitoso](docs/evidencias/auth-login-exitoso.png)

![Crear plato con token](docs/evidencias/auth-crear-plato-201.png)

![Sin token: 401](docs/evidencias/auth-sin-token-401.png)

![Rol sin permiso: 403](docs/evidencias/auth-rol-incorrecto-403.png)

![Eventos de pedido en MongoDB](docs/evidencias/mongo-eventos-pedido.png)

![Persistencia tras reiniciar](docs/evidencias/persistencia-post-reinicio.png)

### Postman

<img width="1292" height="798" alt="POST exitoso" src="https://github.com/user-attachments/assets/3595effe-2770-42e4-8cde-e5c2ad037199" />

<img width="1291" height="802" alt="PATCH 404" src="https://github.com/user-attachments/assets/197ea9b3-3f50-4e85-bd21-cf3ffe8f9f1c" />

<img width="1291" height="805" alt="DELETE exitoso" src="https://github.com/user-attachments/assets/93458892-b032-4331-b6c4-4908d35b3f8c" />

