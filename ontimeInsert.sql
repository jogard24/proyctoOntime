create database Ontime2BD;
use Ontime2BD;

create table usuario (
id int auto_increment primary key,
documento_identidad varchar (30) unique not null,
nombre_completo varchar (100) not null, 
direccion varchar (100) not null,
fotoPerfil_url varchar (255) not null,
tipo_sangre enum ('a+','a-', 'b+', 'b-', 'o+','o-','ab+','ab-'),
fecha_registro timestamp default current_timestamp);

INSERT INTO usuario (documento_identidad, nombre_completo, direccion, fotoPerfil_url, tipo_sangre)
VALUES ('1020304050', 'Carlos Alberto Mendoza', 'Calle 45 # 12-34, Bucaramanga', 'https://miweb.com', 'o+');

INSERT INTO usuario (documento_identidad, nombre_completo, direccion, fotoPerfil_url, tipo_sangre)
VALUES ('9876543210', 'Ana María López', 'Carrera 27 # 50-10, Bucaramanga', 'https://miweb.com', 'a+');

INSERT INTO usuario (documento_identidad, nombre_completo, direccion, fotoPerfil_url, tipo_sangre)
VALUES ('1098741376', 'michael benavides', 'Carrera 37 # 50-10, Bucaramanga', 'https://miweb.com', 'o+');




create table email_personal(
id int auto_increment primary key,
usuario_id int not null,
email varchar (100) not null,
foreign key (usuario_id) references usuario(id));


create table telefono_personal(
id int auto_increment primary key,
usuario_id int not null,
telefono_celular varchar (50) not null,
foreign key (usuario_id) references usuario(id));


create table contacto_emergencia (
id int auto_increment primary key,
usuario_id int not null,
nombre varchar (100) not null,
telefono varchar (50) not null,
parentesco varchar (50) not null,
foreign key (usuario_id)references usuario(id));



create table roles (
id int auto_increment primary key,
nombre_rol enum ('administrador','empleado','contador') unique not null);



create table roles_permiso(
id_roles int not null,
permiso_id int not null,
primary key (id_roles, permiso_id),
foreign key (id_roles) references roles(id), 
foreign key (permiso_id) references permisos(id));


CREATE TABLE permisos (
id INT AUTO_INCREMENT PRIMARY KEY,
permiso_id INT NOT NULL,
permiso varchar (150) NOT NULL,
fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
foreign key (permiso_id) references roles(id));



create table credenciales (
id int auto_increment primary key,
usuario_id int,
usuario varchar (100) unique,
clave varchar (50),
rol_id int unique,
activo boolean default true,
foreign key (usuario_id) references usuario(id),
foreign key(rol_id) references roles (id));

show columns from credenciales;

alter table credenciales
modify rol_id int;

ALTER TABLE credenciales 
ADD constraint unique_rol unique (rol_id);


create table recuperar_clave(
id int auto_increment primary key,
usuario_id int,
token varchar (150) unique not null,
fecha_solicitud timestamp default current_timestamp,
fecha_expiracion timestamp not null,
medio_envio enum ('sms','correo') not null,
usado boolean default false, 
foreign key (usuario_id) references usuario(id));

-- identifiacion con codigo de barras cc --
create table tarjetaProximidad(
id int auto_increment primary key,
usuario_id int not null,
codigo_identificacion varchar (100) unique,
activa boolean default true,
fecha_registro timestamp default current_timestamp,
foreign key (usuario_id) references usuario(id));


-- almacenar asistencia
CREATE TABLE asistencia (
id INT AUTO_INCREMENT PRIMARY KEY,
usuario_id INT,
tipo_evento ENUM('entrada', 'salida'),
fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
observacion text,
tipo_turno varchar (100) not null,
jornada_id int not null,
FOREIGN KEY (usuario_id) REFERENCES usuario(id),
FOREIGN KEY (jornada_id) REFERENCES jornadaLaboral(id));

INSERT INTO asistencia (usuario_id, tipo_evento, fecha_hora, observacion, tipo_turno, jornada_id) 
VALUES (1, 'entrada', '2026-05-22 08:00:00', 'Ingreso a tiempo', 'Mañana', 101);

INSERT INTO asistencia (usuario_id, tipo_evento, fecha_hora, observacion, tipo_turno, jornada_id) 
VALUES (1, 'salida', '2026-05-22 17:00:00', 'Salida regular', 'Mañana', 101);

-- Registros de entrada y salida para el Usuario 2 (Turno Tarde)
INSERT INTO asistencia (usuario_id, tipo_evento, fecha_hora, observacion, tipo_turno, jornada_id) 
VALUES (2, 'entrada', '2026-05-22 14:00:00', 'Ingreso sin novedades', 'Tarde', 102);

INSERT INTO asistencia (usuario_id, tipo_evento, fecha_hora, observacion, tipo_turno, jornada_id) 
VALUES (2, 'salida', '2026-05-22 22:00:00', 'Salida regular', 'Tarde', 102);

select * from asistencia;

-- para crear horarios
create table jornadaLaboral(
id int auto_increment primary key,
nombrejornada varchar (50) unique not null,
hora_entrada time, 
hora_salida time);

select * from jornadaLaboral;

INSERT INTO jornadaLaboral (id, nombrejornada, hora_entrada, hora_salida)
VALUES (101, 'Mañana Completa', '08:00:00', '17:00:00');

INSERT INTO jornadaLaboral (id, nombrejornada, hora_entrada, hora_salida)
VALUES (102, 'Tarde Completa', '14:00:00', '22:00:00');

CREATE TABLE autorizaciones (
id INT AUTO_INCREMENT PRIMARY KEY,
usuario_id INT,
motivo TEXT,
fecha_inicio DATE,
fecha_fin DATE,
aprobado BOOLEAN DEFAULT FALSE,
fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
autorizado_por int,
FOREIGN KEY (usuario_id) REFERENCES usuario(id), -- el empleado que solicita la autorizacion 
FOREIGN KEY (autorizado_por)REFERENCES usuario(id)); -- el usuario que autoriza la solicitud


-- nomina --

-- Totales de tiempo y eventos
CREATE TABLE nomina_tiempos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    fecha_inicio DATE,
    fecha_fin DATE,
    total_dias_trabajados INT,
    total_horas_trabajadas INT,
    total_permisos INT,
    total_extras_diurnas INT DEFAULT 0,
    total_extras_nocturnas INT DEFAULT 0,
    total_dominicales INT DEFAULT 0,
    total_festivos INT DEFAULT 0,
    total_vacaciones INT DEFAULT 0,
    total_incapacidades INT DEFAULT 0,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id));

-- Totales financieros
CREATE TABLE nomina_valores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nomina_tiempo_id INT NOT NULL,
    total_bonificaciones DECIMAL(10,2) DEFAULT 0,
    total_deducciones DECIMAL(10,2) DEFAULT 0,
    total_auxilios DECIMAL(10,2) DEFAULT 0,
    total_aportes DECIMAL(10,2) DEFAULT 0,
    total_devengado DECIMAL(10,2) DEFAULT 0,
    total_neto DECIMAL(10,2) DEFAULT 0,
    fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nomina_tiempo_id) REFERENCES nomina_tiempos(id));





-- auditoria --

create table auditoria_nomina(
id int auto_increment primary key,
usuario_id int not null, 
nomina_id int not null,
tipo_movimiento enum (
	'festivos',
    'dominicales',
    'extras',
    'permisos',
    'nocturnos',
    'bonificaciones',
    'deducciones',
    'auxilios', -- transporte , alimentaciones
    'vacaciones',
    'incapacidad',
    'prestaciones',
    'aportes'
    )not null,
descripcion text,
estado enum ('pendiente','aprobado','rechazado'),
fecha timestamp default current_timestamp,
foreign key (usuario_id) references usuario(id),
foreign key (nomina_id)references nomina_valores(id));



create table contrato (
id int auto_increment primary key,
usuario_id int not null,
tipo_contrato varchar (100) not null,
cargo varchar (100) not null,
salario_base decimal (10, 2) not null,
jornada_id int,
foreign key (usuario_id) references usuario(id));




