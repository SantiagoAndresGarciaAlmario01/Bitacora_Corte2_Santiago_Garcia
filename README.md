# Bitacora DOSW - Corte 2 - Sushi Craft

**Autor:** Santiago Garcia

## Indice

- [Descripcion](#descripcion)
- [Funcionalidades](#funcionalidades)
- [Diagramas](#diagramas)
- [Tabla de endpoints](#tabla-de-endpoints)
- [Como se ejecuta](#como-se-ejecuta)
- [Pruebas y cobertura](#pruebas-y-cobertura)
- [Analisis estatico (SonarQube local)](#analisis-estatico-sonarqube-local)
- [Seguridad](#seguridad)
- [Docker](#docker)
- [CI/CD y Despliegue](#cicd-y-despliegue)
- [Evidencias](#evidencias)

## Descripcion

API REST del restaurante **Sushi Craft** (app *Kaze & Nori*), el concepto de
restaurante escogido en el LAB03 de corte 1. Construida siguiendo la Guia
Turtwig (arquitectura por capas: Dominio -> DTO -> Mapper -> Service ->
Controller) y las diapositivas de la Semana 8 de DOSW 1.

Ademas del CRUD de platos, se implemento el CRUD completo de los otros 6
dominios del restaurante (Mesa, Pedido, ItemPedido, Cuenta, Reserva y
RegistroVehiculo), siguiendo la misma arquitectura por capas.

**Semana 9:** los 7 dominios ya no guardan en memoria, ahora persisten
en PostgreSQL con JPA/Hibernate. Los cambios de estado de un pedido
quedan ademas en un historial en MongoDB. Y la API quedo protegida:
hay que autenticarse con un token JWT para usarla, y crear, editar o
eliminar esta restringido por rol (GERENTE, MESERO, COCINERO).

## Funcionalidades

La API agrupa las funcionalidades en los 7 dominios del restaurante:

- **Platos** (`/api/v1/platos`): CRUD completo para que el gerente cree,
  edite, consulte, active/desactive disponibilidad y elimine platos del
  menu.
- **Menu publico** (`/api/v1/menu`): vista de solo lectura para el
  cliente, que solo muestra los platos disponibles en este momento, con
  filtro opcional por categoria (Roll, Nigiri, Sashimi, Temaki, Entrada,
  Bebida).
- **Mesas** (`/api/v1/mesas`): CRUD de las mesas del restaurante y cambio
  de estado (LIBRE, OCUPADA, RESERVADA).
- **Pedidos** (`/api/v1/pedidos`): CRUD de los pedidos de cada mesa, con
  cambio de estado (RECIBIDO, EN_PREPARACION, LISTO, ENTREGADO,
  CANCELADO) y calculo automatico del total a partir de sus items.
- **Items de pedido** (`/api/v1/items-pedido`): agregar/editar/eliminar
  items dentro de un pedido. El nombre y el precio del plato se "congelan"
  al momento de crear el item (no cambian si despues se edita el plato).
- **Cuentas** (`/api/v1/cuentas`): CRUD de las cuentas por mesa, con
  actualizacion del total y cierre de cuenta.
- **Reservas** (`/api/v1/reservas`): CRUD de las reservas de mesa, con
  cancelacion y reprogramacion.
- **Parqueadero** (`/api/v1/vehiculos`): registro de entrada y salida de
  vehiculos.
- **Autenticacion** (`/auth/login`): login con email y password que
  devuelve un token JWT. El resto de la API (menos el menu publico) pide
  ese token, y crear/editar/eliminar ademas pide un rol especifico.
- **Historial de pedido** (`/api/v1/pedidos/{id}/eventos`): cada pedido
  que se crea o cambia de estado queda registrado en MongoDB.
- **Manejo de errores uniforme**: cualquier endpoint responde con el mismo
  formato de error (`ErrorResponseDTO`) ante recurso no encontrado,
  datos invalidos o body malformado.

## Diagramas

Todos son archivos `.drawio` (se abren en [draw.io](https://app.diagrams.net) /
diagrams.net, con Archivo > Abrir):

- **Diagrama de contexto (C4 nivel 1)**, actualizado con roles y JWT de la
  Semana 9: [`docs/diagramas/diagrama-contexto-sushicraft.drawio`](docs/diagramas/diagrama-contexto-sushicraft.drawio)
- **Diagrama de clases** del dominio (7 clases + 3 enums, sin DTOs ni
  anotaciones de Spring): [`docs/diagramas/diagrama-clases-sushicraft.drawio`](docs/diagramas/diagrama-clases-sushicraft.drawio)
  ([explicacion de las relaciones](docs/diagramas/diagrama-clases.md))
- **Diagrama de secuencia** de "Crear un plato" (happy path + los 4 casos de
  error: 401, 403, 400, 409): [`docs/diagramas/diagrama-secuencia-crear-plato.drawio`](docs/diagramas/diagrama-secuencia-crear-plato.drawio)
- **Modelo relacional** (las 8 tablas de PostgreSQL y sus relaciones):
  [`docs/diagramas/modelo-relacional-sushicraft.drawio`](docs/diagramas/modelo-relacional-sushicraft.drawio)
- **Modelo no relacional** (la coleccion `eventos_pedido` de MongoDB):
  [`docs/diagramas/modelo-no-relacional-sushicraft.drawio`](docs/diagramas/modelo-no-relacional-sushicraft.drawio)
- **Diagrama de componentes especifico** del modulo de Pedidos (Controller,
  Service, Mapper, Repository y como se conecta a Postgres y a Mongo):
  [`docs/diagramas/diagrama-componentes-especifico-sushicraft.drawio`](docs/diagramas/diagrama-componentes-especifico-sushicraft.drawio)
- **Diagrama de componentes general** de toda la API, agrupado por dominio:
  [`docs/diagramas/diagrama-componentes-general-sushicraft.drawio`](docs/diagramas/diagrama-componentes-general-sushicraft.drawio)
- **Diagrama de despliegue**, con los nodos (PC del desarrollador, GitHub,
  Docker Hub, App Service de QA/PROD y sus bases de datos) y las
  comunicaciones entre cada uno:
  [`docs/diagramas/diagrama-despliegue-sushicraft.drawio`](docs/diagramas/diagrama-despliegue-sushicraft.drawio)

## Tabla de endpoints

### Platos y menu

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| GET | `/api/v1/platos` | Listar todos los platos (disponibles o no) | 200 |
| GET | `/api/v1/platos/{id}` | Obtener un plato por id | 200, 404 |
| POST | `/api/v1/platos` | Crear un plato nuevo (queda disponible por defecto) | 201, 400 |
| PUT | `/api/v1/platos/{id}` | Actualizar un plato existente | 200, 404, 400 |
| PATCH | `/api/v1/platos/{id}/disponible?disponible={true\|false}` | Cambiar la disponibilidad de un plato | 200, 404 |
| DELETE | `/api/v1/platos/{id}` | Eliminar un plato | 204, 404 |
| GET | `/api/v1/menu` | Ver el menu publico (solo platos disponibles) | 200 |
| GET | `/api/v1/menu/categoria/{categoria}` | Ver el menu filtrado por categoria | 200 |

### Mesas

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| GET | `/api/v1/mesas` | Listar todas las mesas | 200 |
| GET | `/api/v1/mesas/{id}` | Obtener una mesa por id | 200, 404 |
| POST | `/api/v1/mesas` | Crear una mesa nueva (queda LIBRE por defecto) | 201, 400 |
| PUT | `/api/v1/mesas/{id}` | Actualizar una mesa existente | 200, 404, 400 |
| PATCH | `/api/v1/mesas/{id}/estado?estado={LIBRE\|OCUPADA\|RESERVADA}` | Cambiar el estado de una mesa | 200, 404 |
| DELETE | `/api/v1/mesas/{id}` | Eliminar una mesa | 204, 404 |

### Pedidos

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| GET | `/api/v1/pedidos?idMesa=` | Listar todos los pedidos (o filtrar por mesa) | 200 |
| GET | `/api/v1/pedidos/{id}` | Obtener un pedido por id | 200, 404 |
| POST | `/api/v1/pedidos` | Crear un pedido nuevo (queda RECIBIDO y sin items) | 201, 400 |
| PUT | `/api/v1/pedidos/{id}` | Actualizar un pedido existente | 200, 404, 400 |
| PATCH | `/api/v1/pedidos/{id}/estado?estado=` | Cambiar el estado del pedido | 200, 404 |
| DELETE | `/api/v1/pedidos/{id}` | Eliminar un pedido | 204, 404 |

### Items de pedido

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| GET | `/api/v1/items-pedido?idPedido=` | Listar todos los items (o filtrar por pedido) | 200 |
| GET | `/api/v1/items-pedido/{id}` | Obtener un item por id | 200, 404 |
| POST | `/api/v1/items-pedido` | Agregar un item a un pedido (congela nombre/precio del plato) | 201, 400, 404 |
| PATCH | `/api/v1/items-pedido/{id}/cantidad?cantidad=` | Cambiar la cantidad de un item | 200, 404 |
| DELETE | `/api/v1/items-pedido/{id}` | Eliminar un item | 204, 404 |

### Cuentas

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| GET | `/api/v1/cuentas?idMesa=` | Listar todas las cuentas (o filtrar por mesa) | 200 |
| GET | `/api/v1/cuentas/{id}` | Obtener una cuenta por id | 200, 404 |
| POST | `/api/v1/cuentas` | Abrir una cuenta nueva (queda ABIERTA en 0) | 201, 400 |
| PATCH | `/api/v1/cuentas/{id}/total?total=` | Actualizar el total acumulado | 200, 404 |
| PATCH | `/api/v1/cuentas/{id}/cerrar` | Cerrar una cuenta | 200, 404 |
| DELETE | `/api/v1/cuentas/{id}` | Eliminar una cuenta | 204, 404 |

### Reservas

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| GET | `/api/v1/reservas?idMesa=` | Listar todas las reservas (o filtrar por mesa) | 200 |
| GET | `/api/v1/reservas/{id}` | Obtener una reserva por id | 200, 404 |
| POST | `/api/v1/reservas` | Crear una reserva nueva | 201, 400 |
| PUT | `/api/v1/reservas/{id}` | Actualizar una reserva existente | 200, 404, 400 |
| PATCH | `/api/v1/reservas/{id}/cancelar` | Cancelar una reserva | 200, 404 |
| PATCH | `/api/v1/reservas/{id}/reprogramar?fechaHora=` | Reprogramar una reserva | 200, 404 |
| DELETE | `/api/v1/reservas/{id}` | Eliminar una reserva | 204, 404 |

### Parqueadero

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| GET | `/api/v1/vehiculos?activos=` | Listar todos los registros (o solo los activos) | 200 |
| GET | `/api/v1/vehiculos/{id}` | Obtener un registro por id | 200, 404 |
| POST | `/api/v1/vehiculos` | Registrar el ingreso de un vehiculo | 201, 400 |
| PATCH | `/api/v1/vehiculos/{id}/salida` | Registrar la salida de un vehiculo | 200, 404 |
| DELETE | `/api/v1/vehiculos/{id}` | Eliminar un registro | 204, 404 |

### Autenticacion

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| POST | `/auth/login` | Login con email y password, devuelve un token JWT valido por 24h | 200, 401 |

El token se manda en cada peticion protegida con el header
`Authorization: Bearer <token>`. Sin token (o con uno invalido) la API
responde 401. Con token pero sin el rol necesario, responde 403.

Los GET de los 7 dominios solo piden estar autenticado, sin importar el
rol. Crear, editar y cambiar estado esta permitido para GERENTE y
MESERO (el cambio de estado de un pedido tambien lo puede hacer
COCINERO). Eliminar esta reservado para GERENTE, salvo en items de
pedido donde tambien puede MESERO. El menu publico (`/api/v1/menu`) no
pide token, para que un cliente pueda verlo sin loguearse.

Usuarios de prueba (se crean solos al arrancar la app la primera vez,
con `DataSeeder`; password `sushicraft123` para los 4):

| Email | Rol |
|---|---|
| gerente@sushicraft.com | GERENTE |
| mesero@sushicraft.com | MESERO |
| cocinero@sushicraft.com | COCINERO |
| cliente@sushicraft.com | CLIENTE |

### Historial de un pedido (MongoDB)

| Metodo | Ruta | Descripcion | Respuestas |
|---|---|---|---|
| GET | `/api/v1/pedidos/{idPedido}/eventos` | Ver el historial de eventos de un pedido (creacion, cambios de estado) | 200 |

Cada vez que se crea un pedido o cambia de estado, se guarda un evento
en una coleccion de MongoDB (`eventos_pedido`), aparte de PostgreSQL. Es
el unico dato del proyecto que vive en Mongo; el resto sigue en
PostgreSQL.

## Como se ejecuta

### Opcion A: todo con Docker (recomendado, Semana 10)

Levanta la API junto con Postgres y Mongo en un solo paso, sin instalar
nada mas que Docker. Ver el detalle en la seccion [Docker](#docker).

```
cp .env.example .env
# completar JWT_SECRET en .env (por ejemplo con: openssl rand -base64 32)
docker compose up --build -d
```
- API: http://localhost:8080/api/v1/...
- Swagger UI: http://localhost:8080/swagger-ui/index.html

### Opcion B: manual (Postgres y Mongo en contenedores sueltos, API local)

1. Levanta PostgreSQL local con Docker (una sola vez; si ya existe el
   contenedor, solo hace falta arrancarlo desde Docker Desktop):

```
docker run --name sushicraft-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=sushicraft -p 5432:5432 -d postgres:16
```

2. Levanta MongoDB local con Docker (tambien una sola vez):

```
docker run --name sushicraft-mongo -p 27017:27017 -d mongo:7
```

3. Corre la aplicacion (Hibernate crea las tablas solo, gracias a
   `spring.jpa.hibernate.ddl-auto=update`, y al arrancar se crean los 4
   usuarios de prueba si la tabla todavia esta vacia):

```
mvn spring-boot:run
```
- API: http://localhost:8080/api/v1/...
- Swagger UI: http://localhost:8080/swagger-ui/index.html

### Probar un endpoint protegido en Swagger

Primero hacer POST `/auth/login` con uno de los usuarios de prueba,
copiar el `token` de la respuesta, y pegarlo en el boton **Authorize**
de Swagger (arriba a la derecha) como `Bearer <token>`.

## Pruebas y cobertura

```
mvn test
```
Los tests de los 7 `ServiceImpl` se reescribieron con Mockito: antes el
service guardaba todo en un Map en memoria y el test lo usaba
directamente, ahora el service depende del repositorio JPA (y en
Pedido/ItemPedido tambien de otros services), asi que hay que simular
esas dependencias. De paso se agregaron casos que antes no existian,
como crear un plato con un nombre repetido. Corre `mvn test` para ver
el numero final de pruebas y el reporte de cobertura de Jacoco en
`target/site/jacoco/index.html`.

## Analisis estatico (SonarQube local)

SonarQube corre localmente en Docker (contenedor `sonarqube`, puerto 9000).

1. En Docker Desktop, inicia el contenedor `sonarqube` (boton Play) y
   espera ~1 minuto a que levante.
2. Abre http://localhost:9000 (usuario/clave que ya tengas configurados).
3. Crea el proyecto (o usa uno existente) y copia su **Project Key**.
4. En **My Account > Security**, genera un **token**.
5. En `pom.xml`, reemplaza `sonar.projectKey` por la key del paso 3.
6. Corre:

```
mvn verify sonar:sonar -Dsonar.token=TU_TOKEN
```

(o exporta `SONAR_TOKEN` como variable de entorno en vez de pasarlo por
linea de comandos). El reporte queda visible en http://localhost:9000
dentro del proyecto.

## Seguridad

Login, JWT y roles ya estaban implementados desde la Semana 9
(`SecurityConfig`, `JwtAuthFilter`, `JwtUtil`, `@PreAuthorize` en cada
controller). Para la Semana 10 se completo lo que faltaba: el boton
**Authorize** de Swagger (configurado en `OpenApiConfig` con el esquema
`bearerAuth`) y una prueba de integracion de seguridad en
`src/test/java/com/restaurante/security/SecurityIntegrationTest.java`
que levanta la app completa con MockMvc y verifica, contra los
endpoints reales:

- Sin token -> 401
- Token invalido -> 401
- Rol sin permiso (MESERO intentando crear un plato) -> 403
- Rol correcto (GERENTE) -> 201
- Login con password incorrecta -> 401
- Login valido -> 200 con el token

Esta prueba necesita Postgres y Mongo corriendo (usa `docker compose up`,
ver [Docker](#docker)) porque levanta el contexto completo de Spring.

### OWASP Top 10 - cuales aplican y como se mitigan

| Riesgo | Aplica | Como se mitiga |
|---|---|---|
| Broken Access Control | Si | `@PreAuthorize` por rol en cada endpoint de escritura; los GET solo piden estar autenticado |
| Cryptographic Failures | Si | Password con `BCryptPasswordEncoder`; nunca se devuelve el password en los DTO de respuesta |
| Injection | Si | Spring Data JPA con queries parametrizadas, no se arma SQL a mano |
| Insecure Design | Si | Validacion con Bean Validation (`@NotBlank`, `@Positive`, etc.) antes de llegar a la logica de negocio |
| Security Misconfiguration | Si | CORS explicito (`CorsConfig`), mensajes de error genericos via `GlobalExceptionHandler` (sin stacktrace al cliente) |
| Vulnerable and Outdated Components | Si | Dependencias manejadas por el BOM de `spring-boot-starter-parent` 3.3.4 |
| Identification and Authentication Failures | Si | JWT con expiracion de 24h, password hasheado, 401 uniforme ante credenciales invalidas |
| Software and Data Integrity Failures | Parcial | La imagen Docker se publica firmada por el pipeline de GitHub Actions (ver CI/CD), no hay plugins ni actualizaciones de fuentes externas |
| Security Logging and Monitoring Failures | Si | `@Slf4j` registra login fallido y accesos denegados en `AuthController` y `GlobalExceptionHandler` |
| SSRF | No aplica | La API no hace peticiones salientes a URLs que el usuario pueda controlar |

### Evidencia pendiente de agregar

Captura del boton Authorize con el token puesto, captura de los headers
de seguridad en Postman (`X-Frame-Options`, `X-Content-Type-Options`),
captura del campo `password` hasheado directo en la base de datos, y el
resultado de `mvn test` con `SecurityIntegrationTest` en verde. El resto
(login exitoso, 401, 403) ya esta en la seccion [Evidencias](#evidencias),
capturado en la Semana 9.

## Docker

La API se empaqueta con un `Dockerfile` de dos etapas (build con Maven,
runtime en `eclipse-temurin:17-jre-alpine`) y se levanta junto con
Postgres y Mongo con `docker-compose.yml`.

```
cp .env.example .env
# editar .env: como minimo poner un JWT_SECRET propio
docker compose up --build -d
```

Esto levanta tres servicios: `sushicraft-postgres`, `sushicraft-mongo` y
`sushicraft-api` (puertos 5432, 27017 y 8080). La API espera a que
Postgres y Mongo esten `healthy` antes de arrancar (`depends_on` con
`condition: service_healthy`).

Comandos utiles:

```
docker compose ps                 # ver el estado de los 3 servicios
docker compose logs api           # ver el log de arranque de la API
docker compose restart api        # reiniciar solo la API
docker compose down               # apagar todo (los datos quedan en los volumenes)
```

La imagen tambien se publica en Docker Hub automaticamente desde el
pipeline de CI/CD (ver la seccion siguiente) cada vez que se hace push a
`main`.

### Variables de entorno

| Variable | Para que sirve | Valor por defecto |
|---|---|---|
| `POSTGRES_DB` | Nombre de la base de datos | `sushicraft` |
| `POSTGRES_USER` | Usuario de Postgres | `postgres` |
| `POSTGRES_PASSWORD` | Password de Postgres | `postgres` |
| `MONGO_DB` | Nombre de la base de Mongo | `sushicraft` |
| `JWT_SECRET` | Clave para firmar los JWT (Base64, 32+ bytes) | *(sin valor, hay que ponerla)* |
| `JWT_EXPIRATION` | Tiempo de vida del token en milisegundos | `86400000` (24h) |

### Evidencia pendiente de agregar

Link a la imagen publicada en Docker Hub, captura de `docker compose ps`
con los 3 servicios healthy, captura de Swagger funcionando desde el
contenedor, y captura de los logs de arranque de la API conectandose a
las bases de datos.

## CI/CD y Despliegue

El pipeline esta en `.github/workflows/ci-cd.yml` y corre en GitHub
Actions cada vez que hay un push o un pull request a `main`:

1. **build-test**: compila y corre `mvn clean verify` (con Postgres y
   Mongo como service containers de GitHub Actions).
2. **docker-build-push**: si el paso anterior paso, construye la imagen
   Docker y la publica en Docker Hub con dos tags (`latest` y el hash
   del commit).
3. **deploy-qa**: despliega esa imagen al App Service de Azure de QA.
4. **deploy-prod**: despliega a PROD, pero solo despues de una
   **aprobacion manual** (environment `production` de GitHub, que pide
   un reviewer antes de dejar correr el job).

### Configuracion pendiente en GitHub (una sola vez)

En *Settings > Secrets and variables > Actions*, crear estos secrets
(nombres exactos, sin subir los valores a ningun lado):

- `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`
- `AZURE_WEBAPP_PUBLISH_PROFILE_QA`, `AZURE_WEBAPP_PUBLISH_PROFILE_PROD`

En *Settings > Environments*, crear el environment `production` y
activarle **Required reviewers** (eso es lo que convierte el deploy a
PROD en manual). El environment `qa` no necesita reviewers.

### Ambientes

| Ambiente | Que recibe | Cuando | Base de datos |
|---|---|---|---|
| QA | Cada push a `main` que pasa las pruebas | Automatico | Postgres y Cosmos DB (Mongo API) propios de QA, Azure Database for PostgreSQL Flexible Server (B1ms) + Azure Cosmos DB for MongoDB (RU, nivel gratis) |
| PROD | Lo mismo que paso por QA | Solo con aprobacion manual | Postgres y Cosmos DB (Mongo API) propios de PROD, separados por completo de los de QA |

- URL QA: `https://restaurante-qa-kevinsg-f8cze3ecckd8cjdx.eastus-01.azurewebsites.net/swagger-ui/index.html`
- URL PROD: `https://restaurante-prod-kevinsg-amandrh7csf7gfaz.eastus-01.azurewebsites.net/swagger-ui/index.html`

### Diagrama de despliegue

Dibujado a mano en draw.io (notacion UML: nodos como cubos, artefactos y
caminos de comunicacion), siguiendo el modulo de diagramacion de la
Semana 10: [`docs/diagramas/diagrama-despliegue-sushicraft.drawio`](docs/diagramas/diagrama-despliegue-sushicraft.drawio)

Muestra el flujo completo: PC del desarrollador -> GitHub (repo +
Actions) -> Docker Hub (imagen) -> Azure App Service QA (despliegue
automatico) -> aprobacion manual -> Azure App Service PROD; y cada App
Service conectado a su propia base de datos (Postgres + Cosmos DB Mongo),
separadas entre QA y PROD.

### Evidencia

Pipeline completo en verde, con la aprobacion manual del deploy a PROD:

![Pipeline CI/CD](docs/evidencias/pipeline-cicd-verde-aprobado.png)

Swagger de QA corriendo en Azure, conectado a su Postgres y Mongo reales:

![Swagger QA en la nube](docs/evidencias/swagger-qa-nube.png)

Swagger de PROD corriendo en Azure, conectado a su Postgres y Mongo
reales (separados de los de QA):

![Swagger PROD en la nube](docs/evidencias/swagger-prod-nube.png)

Imagen publicada en Docker Hub con sus tags (hash del commit y `latest`):

![Tags en Docker Hub](docs/evidencias/dockerhub-tags.png)

## Evidencias


### Swagger UI

Los 8 grupos de endpoints (Platos, Menu, Mesas, Pedidos, Items de pedido, Cuentas, Reservas, Parqueadero):

<img width="1916" height="997" alt="image" src="https://github.com/user-attachments/assets/dc318df3-0ab8-45cf-9ac3-cff2c52e1e51" />

<img width="1513" height="798" alt="image" src="https://github.com/user-attachments/assets/17450ae6-7543-4359-8ef7-66284f0dfbb3" />

<img width="1466" height="861" alt="image" src="https://github.com/user-attachments/assets/11137ff2-9908-4b61-ac27-bd1783faa501" />

<img width="1916" height="1001" alt="image" src="https://github.com/user-attachments/assets/0755d64d-accf-4448-978d-cd12232d8dc1" />

### Cobertura de pruebas (Jacoco)

![Cobertura Jacoco](docs/evidencias/jacoco-cobertura.png)

Cobertura total: 66% de instrucciones y 50% de ramas. La mayor parte esta
en `service.impl` (99%) y `mapper` (93%), que es donde estan los 95 tests
unitarios. En `controller` y `config` queda en 0% porque solo se hicieron
pruebas unitarias, no de integracion con MockMvc.

### Analisis estatico (SonarQube local)

En el primer analisis el Quality Gate ya pasaba, pero salieron 37 issues:
3 de Reliability por usar `LocalDateTime.now()` sin zona horaria
(`GlobalExceptionHandler`, `RegistroVehiculo` y `Reserva`) y 34 de
Maintainability por usar `@ApiResponses({...})` como wrapper en los 7
controladores en vez de poner cada `@ApiResponse` por separado, que es
lo que pide la regla desde Java 8. Se corrigieron los dos y quedo en 0
issues.

![SonarQube antes](docs/evidencias/sonar-overview-antes.png)

![Issues antes](docs/evidencias/sonar-issues-antes.png)

![SonarQube despues](docs/evidencias/sonar-overview.png)

![Issues despues](docs/evidencias/sonar-issues-despues.png)

### Ejecucion

![Ejecucion de la app](docs/evidencias/ejecucion-consola.png)

### Autenticacion, roles y persistencia (Semana 9)

Se corrio `mvn clean verify` y paso: compilo y los 97 tests (mapper +
service, estos ultimos reescritos con Mockito) quedaron en verde. Ya con
la app corriendo, con Postgres y Mongo en Docker, se probo lo siguiente
desde Swagger:

Login con el usuario de prueba `gerente@sushicraft.com` (devuelve el
token JWT):

![Login exitoso](docs/evidencias/auth-login-exitoso.png)

Con ese token puesto en el boton Authorize, crear un plato nuevo
funciona (201):

![Crear plato con token](docs/evidencias/auth-crear-plato-201.png)

Sin token, el mismo tipo de endpoint responde 401 (se ve al abrir la
ruta directo en el navegador, sin loguearse):

![Sin token - 401](docs/evidencias/auth-sin-token-401.png)

Con token pero con el usuario `cliente@sushicraft.com` (que no tiene
permiso para crear platos), responde 403:

![Rol sin permiso - 403](docs/evidencias/auth-rol-incorrecto-403.png)

Cada vez que se crea un pedido o le cambian el estado, queda un evento
guardado en MongoDB. Esto es el `GET /api/v1/pedidos/{id}/eventos`
despues de crear un pedido y pasarlo a EN_PREPARACION:

![Historial de eventos en Mongo](docs/evidencias/mongo-eventos-pedido.png)

Y para confirmar que ya no se guarda en memoria: se pararon Postgres y
la app, se volvieron a prender, y los platos creados antes seguian ahi
(en la imagen, el plato `id=1` sigue con sus mismos datos despues del
reinicio):

![Persistencia tras reiniciar](docs/evidencias/persistencia-post-reinicio.png)

### Pruebas por funcionalidad (Postman)

Se probaron los 8 endpoints de platos y menu, cada uno con al menos un
caso exitoso y un caso de error:

| Endpoint | Caso exitoso | Caso de error |
|---|---|---|
| GET `/platos` | 200, lista completa | - |
| GET `/platos/{id}` | 200, plato encontrado | 404, id inexistente |
| POST `/platos` | 201, plato creado | 400, body invalido/vacio |
| PUT `/platos/{id}` | 200, plato actualizado | 404, id inexistente |
| PATCH `/platos/{id}/disponible` | 200, disponibilidad cambiada | 404, id inexistente |
| DELETE `/platos/{id}` | 204, plato eliminado | 404, id inexistente |
| GET `/menu` | 200, solo platos disponibles | - |
| GET `/menu/categoria/{categoria}` | 200, filtrado por categoria | - |

Capturas representativas (creacion, actualizacion, eliminacion y los
errores 404 al usar un id que ya no existe):

![POST exitoso](docs/evidencias/postman-post-exito.png)

![PUT exitoso](docs/evidencias/postman-put-exito.png)

![GET con id inexistente - 404](docs/evidencias/postman-get-404.png)

![PATCH con id inexistente - 404](docs/evidencias/postman-patch-404.png)

![DELETE exitoso](docs/evidencias/postman-delete-exito.png)

### CI/CD y despliegue en la nube (Semana 10)

Se completo el pipeline de principio a fin: push a `main`, build y
pruebas con Postgres y Mongo como services de GitHub Actions, build y
push de la imagen a Docker Hub, despliegue automatico a QA y despliegue
a PROD solo tras aprobacion manual. Para esto se crearon bases de datos
reales en Azure (Postgres Flexible Server + Cosmos DB for MongoDB), una
por ambiente, separadas entre QA y PROD.

Pipeline completo en verde, con la aprobacion manual del deploy a PROD:

![Pipeline CI/CD](docs/evidencias/pipeline-cicd-verde-aprobado.png)

Swagger de QA corriendo en Azure, conectado a su Postgres y Mongo reales:

![Swagger QA en la nube](docs/evidencias/swagger-qa-nube.png)

Swagger de PROD corriendo en Azure, conectado a su Postgres y Mongo
reales (separados de los de QA):

![Swagger PROD en la nube](docs/evidencias/swagger-prod-nube.png)

Imagen publicada en Docker Hub con sus tags (hash del commit y `latest`):

![Tags en Docker Hub](docs/evidencias/dockerhub-tags.png)
