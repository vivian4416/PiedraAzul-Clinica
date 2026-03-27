INSERT INTO usuarios (id, login, password_hash, nombre_completo, rol, activo) VALUES
  (1, 'admin', '$2b$12$jLSLXAzb05vDcvTUaSdofuGHv0oRQHYYWvWrGjCE5eE4ILy5nZr/G', 'Administrador Principal', 'ADMIN', TRUE),
  (2, 'agendadora', '$2b$12$jLSLXAzb05vDcvTUaSdofuGHv0oRQHYYWvWrGjCE5eE4ILy5nZr/G', 'Maria Aguilar', 'AGENDADOR', TRUE),
  (3, 'dr.mora', '$2b$12$jLSLXAzb05vDcvTUaSdofuGHv0oRQHYYWvWrGjCE5eE4ILy5nZr/G', 'Dr. Andres Mora', 'MEDICO', TRUE);

INSERT INTO medicos (id, usuario_id, nombres, tipo, especialidad, intervalo_min, activo) VALUES
  (1, 3, 'Dr. Andres Mora', 'MEDICO', 'QUIROPRACTICA', 20, TRUE),
  (2, NULL, 'Dra. Carolina Rios', 'TERAPISTA', 'FISIOTERAPIA', 30, TRUE);

INSERT INTO medico_disponibilidad (medico_id, dia_semana, hora_inicio, hora_fin) VALUES
  (1, 1, '08:00:00', '12:00:00'),
  (1, 2, '08:00:00', '12:00:00'),
  (1, 3, '08:00:00', '12:00:00'),
  (1, 4, '08:00:00', '12:00:00'),
  (1, 5, '08:00:00', '12:00:00'),
  (2, 2, '08:30:00', '11:30:00'),
  (2, 4, '08:30:00', '11:30:00');

INSERT INTO configuracion_citas (id, ventana_semanas) VALUES
  (1, 4);

INSERT INTO pacientes (id, num_documento, nombres, apellidos, celular, genero) VALUES
  (1, '79453201', 'Carlos', 'Rios Vargas', '3112345678', 'HOMBRE'),
  (2, '52318740', 'Luisa', 'Fernandez', '3001239876', 'MUJER');
