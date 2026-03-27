package co.piedrazul.api.modules.citas;

import co.piedrazul.api.core.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionCitasService {
  private static final int CONFIG_ID = 1;
  private static final int DEFAULT_VENTANA_SEMANAS = 4;

  private final ConfiguracionCitasRepository repository;

  public ConfiguracionCitasService(ConfiguracionCitasRepository repository) {
    this.repository = repository;
  }

  public int getVentanaSemanas() {
    ConfiguracionCitas config = repository.findById(CONFIG_ID).orElseGet(this::crearDefault);
    return config.getVentanaSemanas();
  }

  @Transactional
  public void actualizarVentanaSemanas(int ventanaSemanas) {
    validarVentana(ventanaSemanas);
    ConfiguracionCitas config = repository.findById(CONFIG_ID).orElseGet(this::crearDefault);
    config.setVentanaSemanas(ventanaSemanas);
    repository.save(config);
  }

  private ConfiguracionCitas crearDefault() {
    ConfiguracionCitas config = new ConfiguracionCitas();
    config.setId(CONFIG_ID);
    config.setVentanaSemanas(DEFAULT_VENTANA_SEMANAS);
    return repository.save(config);
  }

  private void validarVentana(int ventanaSemanas) {
    if (ventanaSemanas < 1 || ventanaSemanas > 12) {
      throw new AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "La ventana de agendamiento debe estar entre 1 y 12 semanas");
    }
  }
}
