package de.hsb.app.pv.controller;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;

import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.*;

import de.hsb.app.pv.model.Kunde;
import de.hsb.app.pv.model.Passwort;

/**
 * Der Sessionhandler ist für alle Transaktionen innerhalb einer Session verantwortlich.
 * Das beeinhaltet alle Transaktionen, die ein Kunde für sich selbst auf seinem Konto vornimmt
 */
@SessionScoped
@Named("sessionHandler")
public class sessionHandler implements Serializable {
    /**
     * Seriennummer für java.io.Serializable.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Field zum Handhaben von Entitäten in Transaktionen.
     */
    @PersistenceContext(name = "pv-persistence-unit")
    private EntityManager em;
    /**
     * Field zum Handhaben von Entitäten in Transaktionen.
     */
    @Resource
    private UserTransaction utx;
    /**
     * Zugangsdaten des Kunden.
     */
    private String username, passwort;
    /**
     * Der aktive Kunde innerhalb der Session
     */
    private Kunde kunde = null;
    /**
     * Das durch den User innerhalb seiner Session erstellte Passwort.
     */
    private Passwort pw = null;

    /**
     * Einloggen des Kunden.
     * Für den Fall, dass keine gültige Username-Passwort Kombination gefunden werden kann, bekommt der User eine Warnung.
     * Als Admin kann sich über die Zugangsdaten "admin" und "2424" eingeloggt werden.
     * @return Adresse zum JSF "adminview" für den Admin oder "main" für den Kunden.
     */
    public String login() {
        try {
            utx.begin();
            try {
                kunde = (Kunde) em.createQuery("FROM Kunde k WHERE k.username = :username")
                        .setParameter("username", username).getSingleResult();
            } catch (NoResultException e) {
            }
            try {
                utx.commit();
            } catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
                    | HeuristicRollbackException e) {
                e.printStackTrace();
            }
            // Adminlogin
            if(username.equals("admin") && passwort.equals("2424")) {
                kunde = new Kunde("admin","admin",new GregorianCalendar(1999,1,1).getTime(),"admin","2424",null);
                return "adminview";
            }
            // Erfolgreicher Login
            if (kunde != null && kunde.getPasswort().equals(passwort)) {
                return "main";
            }
            kunde = null;
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage("Username oder Passwort ist/sind inkorrekt"));
            return null;
        } catch (NotSupportedException e) {
            e.printStackTrace();
            return "";
        } catch (SystemException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Ausloggen des Kunden.
     * @return Adresse zum JSF "login".
     */
    public String logout() {
        kunde = null;
        return "login";
    }

    /**
     * Methode zum Überprüfen, ob ein Kunde zur Zeit eingeloggt ist.
     * Wird versucht auf eine Seite mit entsprechendem EventListener zuzugreifen, wird auf das JSF "access-denied" verwiesen, welches einem die Möglichkeit bietet, sich zu registrieren oder anzumelden.
     * @param event Das vom JSF übergebene Event
     */
    public void checkLoggedIn(ComponentSystemEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (kunde == null) {
            ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication()
                    .getNavigationHandler();
            nav.performNavigation("access-denied");
        }
    }
    public void checkLoggedInAdmin(ComponentSystemEvent event){
        FacesContext fc = FacesContext.getCurrentInstance();
        if (kunde == null || !kunde.getUsername().equals("admin") ) {
            kunde = null;
            ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication()
                    .getNavigationHandler();
            nav.performNavigation("access-denied");
        }
    }

    /**
     * Hinzufügen eines Passworts bzw. erstellen einer Datenstruktur für Passwörter, falls der Kunde noch keine besitzt.
     * @return Adresse zum JSF "neuesPasswort".
     */
    public String addPassword() {
        pw = new Passwort();
        kunde.getPasswortList().add(pw);
        return "neuesPasswort";
    }

    /**
     * Speichern eines Passworts oder Kundendaten.
     * @return Adresse zum JSF "main".
     */
    @Transactional
    public String speichern() {
        kunde = em.merge(kunde);
        em.persist(kunde);
        pw = null;
        //An dieser Stelle nötig, da die Daten sonst nicht sofort beim Aufruf der Adminseite aktualisiert werden
        kundenHandler.getKunden().setWrappedData(em.createNamedQuery("SelectKunden").getResultList());
        return "main";
    }

    /**
     * Editieren eines Passworts.
     * @param x Das zu editierende Passwort.
     * @return Adresse zum JSF "neuesPasswort".
     */
    @Transactional
    public String edit(Passwort x){
        pw = x;
        return "neuesPasswort";
    }

    /**
     * Editieren des Kundenprofils.
     * @return Adresse zum JSF "editProfile".
     */
    @Transactional
    public String editProfile(){
        return "editProfile";
    }

    /**
     * Löschen eines Passwortes.
     * @param x Das zu löschende Passwort.
     * @return Adresse zum JSF "main".
     */
    @Transactional
    public String loeschen(Passwort x){
        pw = em.find(Passwort.class, x.getId());
        em.remove(pw);
        kunde.getPasswortList().remove(x);
        kunde = em.merge(kunde);
        pw = null;
        return "main";
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

    public Kunde getKunde() {
        return kunde;
    }

    public void setKunde(Kunde kunde) {
        this.kunde = kunde;
    }

    public Passwort getPw() {
        return pw;
    }

    public void setPw(Passwort pw) {
        this.pw = pw;
    }

    /**
     * Das alphabetische Sortieren von Passwörtern über das java.util Comparator-Interface.
     * Die gespeicherte Kundenliste wird entsprechend umgeschrieben.
     * @return Adresse zum JSF "Main".
     */
    public String sortByName(){
        Comparator<Passwort> compareByUsedFor = (Passwort o1, Passwort o2) -> o1.getUsedFor().compareTo(o2.getUsedFor());
        Collections.sort(kunde.getPasswortList(), compareByUsedFor);
        return "main";
    }
    /**
     * Das Sortieren von Passwörtern nach ihrer Nutzung über das java.util Comparator-Interface.
     * Die gespeicherte Kundenliste wird entsprechend umgeschrieben.
     * @return Adresse zum JSF "Main".
     */
    public String sortByNutzung(){
        Comparator<Passwort> compareByNutzung = (Passwort o1, Passwort o2) -> o1.getNutzung().compareTo(o2.getNutzung());
        Collections.sort(kunde.getPasswortList(), compareByNutzung);
        return "main";
    }
}
