package co.piedrazul.api.modules.citas;

public record CitaCreadaEvent(Long citaId, Long medicoId, Long creadoPor, boolean esNuevoPaciente) {}
