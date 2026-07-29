# SGI EA Taller y LubriBr

Sistema de Gestion Integral para EA Taller y LubriBr (mecanica y lubricentro). Arquitectura MVC en Java EE (Jakarta Servlets + JSP), persistencia JDBC/MySQL, frontend propio (HTML5/CSS3/JS + Bootstrap solo como base).

Ver `docs/modulos/` para la especificacion tecnica de cada modulo y `docs/AVANCE.md` para el estado del proyecto.

## Requisitos

- JDK 21
- Apache Maven 3.9+
- Apache Tomcat 10.1+ (Jakarta EE 10)
- MySQL 8

## Base de datos

```bash
mysql -u root -p < db/schema.sql
mysql -u root -p < db/seed.sql   # crea el usuario administrador inicial
```

Crear el usuario de aplicacion (ajustar password):

```sql
CREATE USER 'ea_taller_app'@'localhost' IDENTIFIED BY 'TU_PASSWORD';
GRANT ALL PRIVILEGES ON ea_taller_lubribr.* TO 'ea_taller_app'@'localhost';
FLUSH PRIVILEGES;
```

Usuario inicial (cambiar la contrasenia despues del primer ingreso):

- Email: `admin@eatallerylubribr.com`
- Contrasenia: `Admin#2026`

## Configuracion de conexion (JNDI)

El proyecto resuelve la conexion via DataSource JNDI (`jdbc/eaTallerDB`), definido en `src/main/webapp/META-INF/context.xml`. Editar ese archivo con las credenciales reales antes de desplegar. Si el DataSource JNDI no esta disponible (por ejemplo al ejecutar fuera de Tomcat), se usa como respaldo `src/main/resources/db.properties`.

## Build y despliegue

```bash
mvn clean package
cp target/ea-taller-lubribr.war $CATALINA_HOME/webapps/ROOT.war
```

Iniciar Tomcat y acceder a `http://localhost:8080/`.

## Estructura del proyecto

```
src/main/java/com/eataller/
  config/       DataSource JNDI (DBConnectionManager)
  constants/    Constantes globales
  entity/       Entidades (tablas)
  dto/          Objetos de transporte hacia la vista
  dao/          Interfaces + implementaciones JDBC
  service/      Logica de negocio y transacciones
  controller/   Servlets
  validator/    Validaciones de formularios
  exception/    Excepciones de negocio especificas
  utils/        Utilidades (hash de password, CSRF, sesion, mapeos, funciones EL)
  filter/       Filtros (encoding, autenticacion/autorizacion)
  listener/     Listeners de arranque y sesion

src/main/webapp/
  WEB-INF/jsp/  Vistas (auth, dashboard, usuarios, layout, error)
  WEB-INF/tags/ Descriptor de funciones EL propias
  assets/       Design System (css/js) — identidad visual unica del sistema

db/             Scripts SQL (schema, seed)
docs/           Especificacion tecnica por modulo y control de avance
```
