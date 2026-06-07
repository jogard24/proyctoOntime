CREATE DATABASE IF NOT EXISTS Ontime2BD;
USE Ontime2BD;

-- 1. Tabla Central de Usuarios
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    documento_identidad VARCHAR(30) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL, 
    direccion VARCHAR(100) NOT NULL,
    estado enum ('activo', 'inactivo') not null,
    tipo_sangre ENUM('a+','a-', 'b+', 'b-', 'o+','o-','ab+','ab-'),
    fotoPerfil_url VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE usuario DROP COLUMN cargo;

create table contacto_emergencia (
id int auto_increment primary key,
usuario_id int not null,
nombre varchar (100) not null,
telefono varchar (50) not null,
parentesco varchar (50) not null,
foreign key (usuario_id)references usuario(id));

create table telefono_personal(
id int auto_increment primary key,
usuario_id int not null,
telefono_celular varchar (50) not null,
foreign key (usuario_id) references usuario(id));

create table email_personal(
id int auto_increment primary key,
usuario_id int not null,
email varchar (100) not null,
foreign key (usuario_id) references usuario(id));


select * from usuario;

-- 2. Tabla de Roles
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    Rol ENUM('administrador','empleado','contador') UNIQUE NOT NULL
);




-- 3. Tabla de Credenciales (Se eliminó la restricción UNIQUE en rol_id para permitir varios contadores/empleados)
CREATE TABLE credenciales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    usuario VARCHAR(100) UNIQUE,
    clave VARCHAR(50),
    rol_id INT,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- 4. Tabla de Jornadas Laborales (Debe ir ANTES de asistencia)
CREATE TABLE jornadaLaboral(
    id INT PRIMARY KEY,
    nombrejornada VARCHAR(50) UNIQUE NOT NULL,
    hora_entrada TIME, 
    hora_salida TIME
);

-- 5. Tabla de Asistencia
CREATE TABLE asistencia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    tipo_evento ENUM('entrada', 'salida'),
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    tipo_turno VARCHAR(100) NOT NULL,
    jornada_id INT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (jornada_id) REFERENCES jornadaLaboral(id)
);

-- 6. Tabla de Contratos (Para cálculos de salarios de nómina)
CREATE TABLE contrato (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo_contrato VARCHAR(100) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    salario_base DECIMAL(10,2) NOT NULL,
    jornada_id INT,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    FOREIGN KEY (jornada_id) REFERENCES jornadaLaboral(id)
);

-- ==========================================================================
-- INSERT PRUEBA 
-- ==========================================================================

INSERT INTO roles (nombre_rol) VALUES ('administrador'), ('empleado'), ('contador');

INSERT INTO jornadaLaboral (id, nombrejornada, hora_entrada, hora_salida)
VALUES (101, 'Mañana Completa', '08:00:00', '17:00:00'),
       (102, 'Tarde Completa', '14:00:00', '22:00:00');

-- Carlos (Admin)
INSERT INTO usuario (id, documento_identidad, nombre,  direccion, fotoPerfil_url, tipo_sangre)
VALUES (1, '1020304050', 'Carlos Alberto Mendoza', 'Calle 45 # 12-34, Bucaramanga', 'https://miweb.com', 'o+');
INSERT INTO credenciales (usuario_id, usuario, clave, rol_id, activo)
VALUES (1, 'admin_carlos', 'admin123', 1, true);

-- Ana (Contadora)
INSERT INTO usuario (id, documento_identidad, nombre,  direccion, fotoPerfil_url, tipo_sangre)
VALUES (2, '9876543210', 'Ana María López', 'Carrera 27 # 50-10, Bucaramanga', 'https://miweb.com', 'a+');
INSERT INTO credenciales (usuario_id, usuario, clave, rol_id, activo)
VALUES (2, 'contador_ana', 'conta123', 3, true);

--  (Empleado de prueba)
INSERT INTO usuario (id, documento_identidad, nombre,  direccion, fotoPerfil_url, tipo_sangre)
VALUES (3, '1098741376', 'Michael Benavides', 'Carrera 37 # 50-10, Bucaramanga', 'https://miweb.com', 'o+');
INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id)
VALUES (3, 'Término Fijo','Operario Backend', 1500000.00, 101);

INSERT INTO usuario (id, documento_identidad, nombre, direccion, fotoPerfil_url, tipo_sangre)
VALUES (4, '123456789', 'jose roa', 'Carrera 20  # 20-20, Bucaramanga', 'https://miweb.com', 'o+');
INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id)
VALUES (4, 'Término Fijo','Operario Backend', 1500000.00, 101);

INSERT INTO usuario (id, documento_identidad, nombre, direccion, fotoPerfil_url, tipo_sangre)
VALUES (5, '987654321', 'Homero', 'avenida siempre viva# 20-20, Springfield', 'https://miweb.com', 'o+');
INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id)
VALUES (5, 'Término Fijo','Operario frontend',   1500000.00, 102);

INSERT INTO usuario (id, documento_identidad, nombre, direccion, fotoPerfil_url, tipo_sangre)
VALUES (6, '654321987', 'Rene Huiguita', 'Carrera 22  # 22-22, Bucaramanga', 'https://miweb.com', 'o+');
INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id)
VALUES (6, 'Término Fijo', 'Operario base de datos', 1500000.00, 102);

INSERT INTO usuario (id, documento_identidad, nombre, direccion, fotoPerfil_url,  tipo_sangre)
VALUES (7, '987455856', 'peter perez', 'Carrera 22  # 20-20, Bucaramanga', 'https://miweb.com', 'o+');
INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id)
VALUES (7, 'Término Fijo', 'recursos humanos', 1500000.00, 101);


-- asistencias

