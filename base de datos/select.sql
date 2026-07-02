select * from asistencia;	
select * from usuario;
select * from jornadaLaboral;
select * from credenciales;
select * from contrato;
select * from nomina;
select * from roles;
select * from telefono_personal;
select * from email_personal;
select * from contacto_emergencia;
select * from detalle_nomina;
select * from periodo_nomina;
select * from nomina_asistencia;
select * from concepto_nomina;

USE ontime3bd;
SELECT DISTINCT usuario_id FROM asistencia;

-- Reemplaza el '1' por el ID real que te arrojó la consulta anterior
INSERT IGNORE INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id, fecha_inicio, fecha_fin)
VALUES (2, 'Término Fijo', 'Contador', 1800000.00, 1, '2026-06-01', '2026-09-30');

-- Por si acaso el contrato ya existía pero con datos desactualizados, forzamos la vigencia:
UPDATE contrato 
SET fecha_inicio = '2026-06-01', fecha_fin = '2026-09-30' 
WHERE usuario_id = 2;



-- Limpiamos la fila nula e insertamos los dos identificadores obligatorios
TRUNCATE TABLE concepto_nomina;

INSERT INTO concepto_nomina (id, nombre, tipo, factor_calculo) 
VALUES 
(1, 'Deducción por Retardo', 'descuento', 1.00),
(2, 'Bonificación de Hora Extra', 'extra', 1.00);



-- Insertamos una marca de salida que simula horas extras para Camilo Pérez
INSERT INTO asistencia (usuario_id, tipo_evento, fecha_hora, observacion, jornada_id)
VALUES (2, 'salida', '2026-06-18 19:30:00', 'Trabajo adicional. 1 hora extra.', 1);


-- visualizar quien modifico o asigno un rol en la tabla usuaro a usuario 
select id, nombre, apellido, usuario_modificador_id from usuario;

SELECT usuario, clave, rol_id FROM credenciales WHERE usuario = 'adminJose';
DESCRIBE credenciales;

USE ontime3bd;

USE ontime3bd;

-- Corregimos la fila 2 de Camilo Pérez
UPDATE usuario SET nombre = 'camilo' WHERE id = 2;
UPDATE usuario SET apellido = 'diaz' WHERE id = 7;
update telefono_personal set telefono_celular = '111111' where id = 9;

----- CONSULTA DE AGREGACION
SELECT u.id, con.salario_base, -- Extrae el identificador del usuario y su salario base mensual fijo.
-- CUENTA DEDÚCTIVA: Cuenta cuántas marcas de salida (jornadas completadas) tiene en ese año-mes
-- COALESCE asegura que si el usuario no tiene ninguna asistencia registrada, el resultado sea un \(0\) en lugar de un valor nulo
COALESCE(SUM(CASE WHEN a.tipo_evento = 'salida' THEN 1 ELSE 0 END), 0) as dias_trabajados 
-- Revisa todos los registros de asistencia. Si el evento es de tipo 'salida', suma \(1\); de lo contrario, suma \(0\).
FROM usuario u -- Toma la tabla de usuarios como base.
INNER JOIN contrato con ON u.id = con.usuario_id -- Une esta tabla con la tabla de contratos para asociar cada usuario con su respectivo salario base
-- left join une la tabla con registro de asistencia 
--  garantiza que todos los usuarios aparezcan en el reporte, incluso si no tienen registros de asistencia
--  date format Filtra los registros de asistencia para tomar únicamente los que ocurrieron en junio de 2026
LEFT JOIN asistencia a ON u.id = a.usuario_id AND DATE_FORMAT(a.fecha_hora, '%Y-%m') = '2026-06'
GROUP BY u.id, con.salario_base; -- Agrupa los resultados finales por cada usuario

USE ontime3bd;

-- Consulta relacional para auditar el horario oficial de cada empleado
SELECT 
    u.id AS usuario_id,
    u.documento_identidad,
    CONCAT(u.nombre, ' ', u.apellido) AS nombre_completo,
    con.cargo,
    j.nombrejornada AS jornada_asignada,
    j.hora_entrada,
    j.hora_salida
FROM usuario u
INNER JOIN contrato con ON u.id = con.usuario_id
INNER JOIN jornadaLaboral j ON con.jornada_id = j.id;

DELETE FROM usuario
WHERE usuario_id = 2 
AND cargo = 'Contador'; -- Elimina la fila con la 'C' mayúscula si es la duplicada



