CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  login VARCHAR(60) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nombre_completo VARCHAR(120) NOT NULL,
  rol VARCHAR(20) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medicos (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id BIGINT NULL,
  nombres VARCHAR(120) NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  especialidad VARCHAR(30) NOT NULL,
  intervalo_min INT NOT NULL DEFAULT 20,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_med_usr FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS medico_disponibilidad (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  medico_id BIGINT NOT NULL,
  dia_semana INT NOT NULL,
  hora_inicio TIME NOT NULL,
  hora_fin TIME NOT NULL,
  CONSTRAINT uq_med_dia UNIQUE (medico_id, dia_semana),
  CONSTRAINT fk_disp_med FOREIGN KEY (medico_id) REFERENCES medicos(id)
);

CREATE TABLE IF NOT EXISTS configuracion_citas (
  id INT PRIMARY KEY,
  ventana_semanas INT NOT NULL DEFAULT 4
);

CREATE TABLE IF NOT EXISTS pacientes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  num_documento VARCHAR(20) NOT NULL UNIQUE,
  nombres VARCHAR(80) NOT NULL,
  apellidos VARCHAR(80) NOT NULL,
  celular VARCHAR(15) NOT NULL,
  genero VARCHAR(10) NOT NULL,
  fecha_nacimiento DATE NULL,
  email VARCHAR(120) NULL,
  usuario_id BIGINT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pac_usr FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS citas (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  medico_id BIGINT NOT NULL,
  paciente_id BIGINT NOT NULL,
  creado_por BIGINT NOT NULL,
  fecha_hora TIMESTAMP NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA',
  origen VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_medico_slot UNIQUE (medico_id, fecha_hora),
  CONSTRAINT fk_cita_med FOREIGN KEY (medico_id) REFERENCES medicos(id),
  CONSTRAINT fk_cita_pac FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
  CONSTRAINT fk_cita_usr FOREIGN KEY (creado_por) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS auditoria (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id BIGINT NULL,
  accion VARCHAR(60) NOT NULL,
  entidad VARCHAR(40) NULL,
  entidad_id BIGINT NULL,
  detalle VARCHAR(1000) NULL,
  fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_aud_usr FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX IF NOT EXISTS idx_citas_medico_fecha ON citas (medico_id, fecha_hora);
CREATE INDEX IF NOT EXISTS idx_pac_documento ON pacientes (num_documento);
CREATE INDEX IF NOT EXISTS idx_disp_medico ON medico_disponibilidad (medico_id);
