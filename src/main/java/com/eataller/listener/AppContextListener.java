package com.eataller.listener;

import com.eataller.config.DBConnectionManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsabilidad: verificar al iniciar la aplicacion que la conexion a
 * MySQL puede establecerse correctamente, dejando constancia en el log del
 * servidor. No detiene el despliegue si la base de datos no esta disponible
 * (permite iniciar Tomcat y diagnosticar el problema desde los logs).
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Iniciando SGI EA Taller y LubriBr...");
        try (Connection connection = DBConnectionManager.getConnection()) {
            LOGGER.info("Conexion a la base de datos verificada correctamente.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "No se pudo establecer conexion con la base de datos al iniciar la aplicacion.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Finalizando SGI EA Taller y LubriBr.");
    }
}
