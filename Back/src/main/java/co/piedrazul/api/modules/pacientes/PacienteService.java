package co.piedrazul.api.modules.pacientes;

import co.piedrazul.api.core.AppException;
import java.util.List;
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
    String normalizado = normalizarDocumento(documento);
    if (normalizado.isEmpty()) {
      return null;
    }
    return pacienteRepository.findByNumDocumento(normalizado).orElse(null);
  }

  public FindOrCreateResult findOrCreate(PacienteInput input) {
    String documento = normalizarDocumento(input.numDocumento());
    String nombres = normalizarTexto(input.nombres());
    String apellidos = normalizarTexto(input.apellidos());
    String celular = normalizarDocumento(input.celular());
    String genero = input.genero() == null ? "" : input.genero().trim().toUpperCase();
    String email = normalizarEmail(input.email());

    var existente = pacienteRepository.findByNumDocumento(documento);
    if (existente.isPresent()) {
      return new FindOrCreateResult(existente.get(), false);
    }

    validar(documento, nombres, apellidos, celular, genero);
    Paciente p = new Paciente();
    p.setNumDocumento(documento);
    p.setNombres(nombres);
    p.setApellidos(apellidos);
    p.setCelular(celular);
    p.setGenero(genero);
    p.setFechaNacimiento(input.fechaNacimiento());
    p.setEmail(email);

    Paciente saved = pacienteRepository.save(p);
    return new FindOrCreateResult(saved, true);
  }

  public List<PacienteAutocompleteDto> sugerenciasPorDocumento(String prefijo) {
    String normalizado = normalizarDocumento(prefijo);
    if (normalizado.isEmpty()) {
      return List.of();
    }

    return pacienteRepository.findTop5ByNumDocumentoStartingWithOrderByNumDocumentoAsc(normalizado)
      .stream()
      .map(p -> new PacienteAutocompleteDto(
        p.getNumDocumento(),
        p.getNombres(),
        p.getApellidos(),
        p.getCelular(),
        p.getGenero(),
        p.getFechaNacimiento(),
        p.getEmail()
      ))
      .toList();
  }

  private void validar(String documento, String nombres, String apellidos, String celular, String genero) {
    if (documento.isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "numDocumento es obligatorio");
    }
    if (nombres.isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "nombres es obligatorio");
    }
    if (apellidos.isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "apellidos es obligatorio");
    }
    if (celular.isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "celular es obligatorio");
    }
    if (!GENEROS.contains(genero)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "genero invalido. Valores: HOMBRE, MUJER, OTRO");
    }
  }

  private String normalizarDocumento(String value) {
    return value == null ? "" : value.trim().replaceAll("\\s+", "");
  }

  private String normalizarTexto(String value) {
    return value == null ? "" : value.trim().replaceAll("\\s+", " ");
  }

  private String normalizarEmail(String value) {
    if (value == null) {
      return null;
    }
    String email = value.trim();
    return email.isEmpty() ? null : email;
  }
}
