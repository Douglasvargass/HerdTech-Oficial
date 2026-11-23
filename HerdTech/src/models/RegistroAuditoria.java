package models;

import java.time.LocalDateTime;

// Uma linha da auditoria: quem fez o quê, em qual máquina, quando e em qual registro
public class RegistroAuditoria {
	private final Integer id;
	private final LocalDateTime dataHora;
	private final String maquina;
	private final String usuario;
	private final String acao;      // INCLUSAO, ALTERACAO, EXCLUSAO ou CONSULTA
	private final String entidade;  // Cliente, Animal ou Vacina
	private final Integer registroId;
	private final String detalhes;

	public RegistroAuditoria(Integer id, LocalDateTime dataHora, String maquina, String usuario, String acao,
							 String entidade, Integer registroId, String detalhes) {
		this.id = id;
		this.dataHora = dataHora;
		this.maquina = maquina;
		this.usuario = usuario;
		this.acao = acao;
		this.entidade = entidade;
		this.registroId = registroId;
		this.detalhes = detalhes;
	}

	public Integer getId() { return id; }
	public LocalDateTime getDataHora() { return dataHora; }
	public String getMaquina() { return maquina; }
	public String getUsuario() { return usuario; }
	public String getAcao() { return acao; }
	public String getEntidade() { return entidade; }
	public Integer getRegistroId() { return registroId; }
	public String getDetalhes() { return detalhes; }
}
