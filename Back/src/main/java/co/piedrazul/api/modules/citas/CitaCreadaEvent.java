package co.piedrazul.api.modules.citas;

public record CitaCreadaEvent(Long citaId, String medicoId, String creadoPor, boolean esNuevoPaciente) {}
