package com.eataller.config;

import com.eataller.constants.AppConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsabilidad: centralizar la obtencion de conexiones JDBC hacia MySQL.
 * Prioriza el DataSource administrado por Apache Tomcat (JNDI, con pool de
 * conexiones configurado en META-INF/context.xml). Si el lookup JNDI falla
 * (por ejemplo al ejecutar pruebas fuera del contenedor), utiliza
 * DriverManager leyendo db.properties del classpath.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class DBConnectionManager {

    private static final Logger LOGGER = Logger.getLogger(DBConnectionManager.class.getName());
    private static volatile DataSource dataSource;

    private DBConnectionManager() {
    }

    /**
     * Obtiene una conexion JDBC lista para usar. El llamador es responsable
     * de cerrarla (preferentemente con try-with-resources).
     *
     * @return conexion activa a la base de datos
     * @throws SQLException si no fue posible establecer la conexion
     */
    public static Connection getConnection() throws SQLException {
        DataSource ds = resolveDataSource();
        if (ds != null) {
            return ds.getConnection();
        }
        return getConnectionFromProperties();
    }

    private static DataSource resolveDataSource() {
        if (dataSource == null) {
            synchronized (DBConnectionManager.class) {
                if (dataSource == null) {
                    try {
                        Context initContext = new InitialContext();
                        dataSource = (DataSource) initContext.lookup(AppConstants.JNDI_DATASOURCE);
                        LOGGER.log(Level.INFO, "DataSource JNDI resuelto correctamente: {0}", AppConstants.JNDI_DATASOURCE);
                    } catch (NamingException e) {
                        LOGGER.log(Level.WARNING, "No se pudo resolver el DataSource JNDI, se utilizara db.properties como respaldo.", e);
                        dataSource = null;
                    }
                }
            }
        }
        return dataSource;
    }

    private static Connection getConnectionFromProperties() throws SQLException {
        Properties props = new Properties();
        try (InputStream input = DBConnectionManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new SQLException("No se encontro db.properties en el classpath y el DataSource JNDI no esta disponible.");
            }
            props.load(input);
        } catch (IOException e) {
            throw new SQLException("Error al leer db.properties", e);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        String driver = props.getProperty("db.driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontro el driver JDBC: " + driver, e);
        }

        return DriverManager.getConnection(url, user, password);
    }
}
