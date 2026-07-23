CREATE DATABASE Ontime3BD;
USE Ontime3BD;




drop database ontime3bd;
-- 1. Tabla Central de Usuarios 
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    documento_identidad VARCHAR(30) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL, 
    apellido VARCHAR(100) NOT NULL, 
    direccion VARCHAR(100) NOT NULL,
    estado ENUM('activo', 'inactivo') NOT NULL DEFAULT 'activo',
    tipo_sangre ENUM('a+','a-', 'b+', 'b-', 'o+','o-','ab+','ab-'),
    fotoPerfil_url VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_modificador_id INT, -- Auditoría (RF25)
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_modificador_id) REFERENCES usuario(id)
);

ALTER TABLE usuario MODIFY COLUMN estado ENUM('activo', 'inactivo') NOT NULL DEFAULT 'activo';


CREATE TABLE contacto_emergencia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(50) NOT NULL,
    parentesco VARCHAR(50) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE telefono_personal (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    telefono_celular VARCHAR(50) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE email_personal (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    email VARCHAR(100) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- 2. Tabla de Roles
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    Rol ENUM('administrador','empleado','contador') UNIQUE NOT NULL
);

-- 3. Tabla de Credenciales del login
CREATE TABLE credenciales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    usuario VARCHAR(100) UNIQUE NOT NULL,
    clave VARCHAR(255) NOT NULL, -- soporta BCrypt/SHA-256
    rol_id INT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    requiere_cambio_clave BOOLEAN DEFAULT FALSE, -- Soporte para claves temporales (RF07)
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

ALTER TABLE credenciales 
ADD CONSTRAINT unique_usuario_credencial UNIQUE (usuario_id);

-- 4. Tabla de Jornadas Laborales / horarios
CREATE TABLE jornadaLaboral (
    id INT PRIMARY KEY, 
    nombrejornada VARCHAR(50) UNIQUE NOT NULL,
    hora_entrada TIME NOT NULL, 
    hora_salida TIME NOT NULL
);

ALTER TABLE jornadaLaboral MODIFY COLUMN id INT AUTO_INCREMENT;

-- 5. Tabla de Asistencia en las que inyecta el keypad de forma  automática al Backend 
CREATE TABLE asistencia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo_evento ENUM('entrada', 'salida') NOT NULL,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    jornada_id INT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (jornada_id) REFERENCES jornadaLaboral(id)
);

-- 6. Tabla de Contratos
CREATE TABLE contrato (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo_contrato VARCHAR(100) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    salario_base DECIMAL(10,2) NOT NULL,
    jornada_id INT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (jornada_id) REFERENCES jornadaLaboral(id)
);

ALTER TABLE contrato ADD COLUMN estado VARCHAR(20) DEFAULT 'activo';

USE ontime3bd;

-- Agregamos la columna para definir la periodicidad pactada en el contrato 
ALTER TABLE contrato ADD COLUMN periodo_pago VARCHAR(20) DEFAULT 'Mensual';


ALTER TABLE contrato 
ADD COLUMN fecha_inicio DATE NOT NULL DEFAULT '2026-01-01',
ADD COLUMN fecha_fin DATE NULL;

UPDATE contrato 
SET fecha_inicio = '2026-06-16', fecha_fin = '2026-07-14' 
WHERE usuario_id = 2;

-- 7. Tabla de Permisos Laborales 
CREATE TABLE permiso_laboral (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL, -- El permiso le pertenece a un empleado
    tipo_permiso VARCHAR(150) NOT NULL, -- Ej: "Médico", "Calamidad"
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado ENUM('pendiente', 'aprobado', 'rechazado') DEFAULT 'pendiente', -- Corregido para cumplir RF12 y RF13
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- 8. Periodos de Nómina maneja los tiempos de nomina mensuales
CREATE TABLE periodo_nomina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado ENUM('abierto', 'cerrado') DEFAULT 'abierto'
);
    
-- 9. Nómina Principal
CREATE TABLE nomina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    periodo_id INT NOT NULL,
    salario_base_periodo DECIMAL(10,2) NOT NULL,
    total_horas_extras DECIMAL(5,2) DEFAULT 0.0,
    total_neto DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (periodo_id) REFERENCES periodo_nomina(id)
);


-- Creamos la tabla  para conectar físicamente los dos módulos de asistencia y nomina
CREATE TABLE nomina_asistencia (
    nomina_id INT NOT NULL,
    asistencia_id INT NOT NULL,
    PRIMARY KEY (nomina_id, asistencia_id),
    CONSTRAINT fk_nom_as_nomina FOREIGN KEY (nomina_id) REFERENCES nomina(id),
    CONSTRAINT fk_nom_as_asistencia FOREIGN KEY (asistencia_id) REFERENCES asistencia(id)
);


-- 10. permite la exportacion del informe general de nomina
CREATE TABLE concepto_nomina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('extra', 'descuento') NOT NULL,
    factor_calculo DECIMAL(5,2) DEFAULT 1.0
);

-- 11. Detalle guarda el analisis de los conceptos por retardo, descuentos, extras
CREATE TABLE detalle_nomina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nomina_id INT NOT NULL,
    concepto_id INT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    FOREIGN KEY (nomina_id) REFERENCES nomina(id),
    FOREIGN KEY (concepto_id) REFERENCES concepto_nomina(id)
);





-- ==========================================================================
-- INSERT PRUEBA 
-- ==========================================================================
INSERT INTO roles (Rol) VALUES ('administrador');
INSERT INTO roles (Rol) VALUES ('empleado');
INSERT INTO roles (Rol) VALUES ('contador');



INSERT INTO jornadaLaboral (id, nombrejornada, hora_entrada, hora_salida)
VALUES (101, 'Mañana Completa', '08:00:00', '17:00:00'),
       (102, 'Tarde Completa', '14:00:00', '22:00:00');

--  (Admin)
INSERT INTO usuario (id, documento_identidad, nombre, apellido, direccion, estado, tipo_sangre, fotoPerfil_url, usuario_modificador_id)
VALUES (1, '123456789', 'Jose', 'Roa', 'Calle 45 # 12-34, Bucaramanga', 'activo', 'o+', 'img/usuario-defecto.png', NULL);

-- Creamos de una vez las credenciales de Administrador (Rol ID 1) para el Dashboard
INSERT INTO credenciales (usuario_id, usuario, clave, rol_id, activo, requiere_cambio_clave)
VALUES (1, 'adminJose', 'admin123', 1, true, false);

-- Creamos el contrato base del administrador para que el cargo refleje en el panel corporativo
INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id)
VALUES (1, 'Término Indefinido', 'Administrador', 2500000.00, 1);

--  empleado prueba
INSERT INTO usuario (id, documento_identidad, nombre, apellido, direccion, estado, tipo_sangre, fotoPerfil_url, usuario_modificador_id)
VALUES (2, '109876543', 'Camilo', 'Pérez', 'Carrera 27 # 36-45, Bucaramanga', 'activo', 'o+', 'img/usuario-defecto.png', 1);

--  Registro de sus canales de contacto relacionales
INSERT INTO telefono_personal (usuario_id, telefono_celular) VALUES (2, '3156789012');
INSERT INTO email_personal (usuario_id, email) VALUES (2, 'camilo.perez@email.com');
INSERT INTO contacto_emergencia (usuario_id, nombre, telefono, parentesco) VALUES (2, 'María Pérez', '3109876543', 'Madre');

--  Registro laboral obligatorio para el cálculo de nómina y retardos (Jornada Mañana ID 1)
INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id)
VALUES (2, 'Término Fijo', 'contador', 1400000.00, 1);

--  Creación de sus credenciales planas para el Keypad/Pinpad táctil (Rol Empleado ID 2)
INSERT INTO credenciales (usuario_id, usuario, clave, rol_id, activo, requiere_cambio_clave)
VALUES (2, 'contador', 'cont123', 3, true, false);

-------------------

INSERT INTO usuario (id, documento_identidad, nombre, apellido, direccion, estado, tipo_sangre, fotoPerfil_url, usuario_modificador_id)
VALUES (3, '135246789', 'Laura', 'Gómez', 'Calle 52 # 18-22, Floridablanca', 'activo', 'a+', 'img/usuario-defecto.png', 1);

-- 2. Registro de sus canales de contacto relacionales
INSERT INTO telefono_personal (usuario_id, telefono_celular) VALUES (3, '3182345678');
INSERT INTO email_personal (usuario_id, email) VALUES (3, 'laura.gomez@email.com');
INSERT INTO contacto_emergencia (usuario_id, nombre, telefono, parentesco) VALUES (3, 'Carlos Gómez', '3204567890', 'Padre');

-- 3. Registro laboral obligatorio para el cálculo de nómina y retardos (Jornada Mañana ID 1)
INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id)
VALUES (3, 'Término Indefinido', 'Analista de Soporte', 1800000.00, 1);

-- 4. Creación de sus credenciales planas para el Keypad/Pinpad táctil (Rol Empleado ID 2)
INSERT INTO credenciales (usuario_id, usuario, clave, rol_id, activo, requiere_cambio_clave)
VALUES (3, '', '', 2, true, false);
