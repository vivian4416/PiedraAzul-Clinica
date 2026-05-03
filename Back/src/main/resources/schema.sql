CREATE TABLE IF NOT EXISTS medicos (
  id VARCHAR(36) PRIMARY KEY,
  nombres VARCHAR(120) NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  especialidad VARCHAR(30) NOT NULL,
  intervalo_min INT NOT NULL DEFAULT 30,
  activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS medico_disponibilidad (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  medico_id VARCHAR(36) NOT NULL,
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
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS citas (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  medico_id VARCHAR(36) NOT NULL,
  paciente_id BIGINT NOT NULL,
  creado_por VARCHAR(64) NOT NULL,
  fecha_hora TIMESTAMP NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA',
  origen VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_medico_slot UNIQUE (medico_id, fecha_hora),
  CONSTRAINT fk_cita_med FOREIGN KEY (medico_id) REFERENCES medicos(id),
  CONSTRAINT fk_cita_pac FOREIGN KEY (paciente_id) REFERENCES pacientes(id)
);

CREATE TABLE IF NOT EXISTS auditoria (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id VARCHAR(64) NULL,
  accion VARCHAR(60) NOT NULL,
  entidad VARCHAR(40) NULL,
  entidad_id BIGINT NULL,
  detalle VARCHAR(1000) NULL,
  fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_citas_medico_fecha ON citas (medico_id, fecha_hora);
CREATE INDEX IF NOT EXISTS idx_pac_documento ON pacientes (num_documento);
CREATE INDEX IF NOT EXISTS idx_disp_medico ON medico_disponibilidad (medico_id);
