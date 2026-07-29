-- =============================================================
-- SGI EA Taller y LubriBr - Datos semilla
-- Crea el primer usuario administrador para poder ingresar al sistema
-- una vez desplegado. CAMBIAR LA CONTRASENIA inmediatamente despues del
-- primer inicio de sesion en un entorno productivo.
--
-- Usuario: admin@eatallerylubribr.com
-- Contrasenia: Admin#2026
-- (hash generado con BCrypt, factor de costo 12)
-- =============================================================

USE ea_taller_lubribr;

INSERT INTO usuarios (nombre, email, password_hash, rol, activo)
VALUES (
    'Administrador del Sistema',
    'admin@eatallerylubribr.com',
    '$2a$12$X/FrD/MrbwVR4pReGN6LSOYmoc9modcCLZY.HA75vUgeLVOmEHbPK',
    'ADMINISTRADOR',
    TRUE
)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);
