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
UPDATE usuario SET nombre = 'michael' WHERE id = 5;

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



