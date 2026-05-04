-- Medicos vinculados a usuarios Keycloak con rol MEDICO
INSERT INTO medicos (id, nombres, tipo, especialidad, intervalo_min, activo) VALUES
  ('795ee435-a5d2-4817-87b0-11632b46ff4c', 'javier gomez', 'MEDICO', 'GENERAL', 30, TRUE),
  ('bc119041-1689-4247-8f21-352b1e7068d3', 'Javier Gomez', 'MEDICO', 'GENERAL', 30, TRUE);

INSERT INTO medico_disponibilidad (medico_id, dia_semana, hora_inicio, hora_fin) VALUES
  ('795ee435-a5d2-4817-87b0-11632b46ff4c', 1, '08:00:00', '12:00:00'),
  ('795ee435-a5d2-4817-87b0-11632b46ff4c', 2, '08:00:00', '12:00:00'),
  ('795ee435-a5d2-4817-87b0-11632b46ff4c', 3, '08:00:00', '12:00:00'),
  ('795ee435-a5d2-4817-87b0-11632b46ff4c', 4, '08:00:00', '12:00:00'),
  ('795ee435-a5d2-4817-87b0-11632b46ff4c', 5, '08:00:00', '12:00:00'),
  ('bc119041-1689-4247-8f21-352b1e7068d3', 2, '08:30:00', '11:30:00'),
  ('bc119041-1689-4247-8f21-352b1e7068d3', 4, '08:30:00', '11:30:00');

INSERT INTO configuracion_citas (id, ventana_semanas) VALUES
  (1, 4);

INSERT INTO pacientes (id, num_documento, nombres, apellidos, celular, genero) VALUES
  (1, '79453201', 'Carlos', 'Rios Vargas', '3112345678', 'HOMBRE'),
  (2, '52318740', 'Luisa', 'Fernandez', '3001239876', 'MUJER');
