package de.hsb.app.pv.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@NamedQuery(name = "SelectKunden", query = "Select k from Kunde k where k.username is not null")
@Entity

public class Kunde implements Serializable {

	private static final long serialVersionUID = -8458300548433261434L;

	@Id
	@GeneratedValue
	private UUID id;
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "kunde_id", nullable = false)
	private List <Passwort> passwortList = new ArrayList<Passwort>();
	private Anrede anrede;
	private String vorname;
	private String nachname;
	@Temporal(TemporalType.DATE)
	private Date geburtsdatum;
	private String username;
	private String passwort;


	public Kunde() {
	}

	public Kunde(String vorname, String nachname, Date geburtsdatum, String username, String passwort, List<Passwort> passwortList) {
		super();
		this.vorname = vorname;
		this.nachname = nachname;
		this.geburtsdatum = geburtsdatum;
		this.username = username;
		this.passwort = passwort;
		this.passwortList = passwortList;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public Date getGeburtsdatum() {
		return geburtsdatum;
	}

	public Anrede getAnrede() {
		return anrede;
	}

	public void setAnrede(Anrede anrede) {
		this.anrede = anrede;
	}

	public void setGeburtsdatum(Date geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswort() {
		return passwort;
	}

	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}

	public List<Passwort> getPasswortList() {
		return passwortList;
	}

	public void setPasswortList(List<Passwort> passwortList) {
		this.passwortList = passwortList;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
}
