package co.piedrazul.api.modules.pacientes;

import co.piedrazul.api.core.AppException;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PacienteService {
  private static final Set<String> GENEROS = Set.of("HOMBRE", "MUJER", "OTRO");
  private final PacienteRepository pacienteRepository;

  public PacienteService(PacienteRepository pacienteRepository) {
    this.pacienteRepository = pacienteRepository;
  }

  public Paciente buscarPorDocumento(String documento) {
    return pacienteRepository.findByNumDocumento(documento).orElse(null);
  }

  public FindOrCreateResult findOrCreate(PacienteInput input) {
    String genero = input.genero() == null ? "" : input.genero().toUpperCase();
    var existente = pacienteRepository.findByNumDocumento(input.numDocumento());
    if (existente.isPresent()) {
      return new FindOrCreateResult(existente.get(), false);
    }

    validar(input, genero);
    Paciente p = new Paciente();
    p.setNumDocumento(input.numDocumento().trim());
    p.setNombres(input.nombres().trim());
    p.setApellidos(input.apellidos().trim());
    p.setCelular(input.celular().trim());
    p.setGenero(genero);
    p.setFechaNacimiento(input.fechaNacimiento());
    p.setEmail(input.email());

    Paciente saved = pacienteRepository.save(p);
    return new FindOrCreateResult(saved, true);
  }

  private void validar(PacienteInput input, String genero) {
    if (input.numDocumento() == null || input.numDocumento().trim().isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "numDocumento es obligatorio");
    }
    if (input.nombres() == null || input.nombres().trim().isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "nombres es obligatorio");
    }
    if (input.apellidos() == null || input.apellidos().trim().isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "apellidos es obligatorio");
    }
    if (input.celular() == null || input.celular().trim().isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "celular es obligatorio");
    }
    if (!GENEROS.contains(genero)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "genero invalido. Valores: HOMBRE, MUJER, OTRO");
    }
  }
}
