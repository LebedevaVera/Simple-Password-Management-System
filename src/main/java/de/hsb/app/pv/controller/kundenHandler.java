package de.hsb.app.pv.controller;

import de.hsb.app.pv.model.Anrede;
import de.hsb.app.pv.model.Kunde;
import de.hsb.app.pv.model.Passwort;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.*;
/**
 * Die KundenHandler-Klasse ist zuständig für das Verwalten der Kundendaten.
 * Er wird genutzt, um Kunden zu persistieren und (überwiegend über den Admin) als Ganzes zu verwalten.
 * Einzelne Kunden und ihre Daten über den User werden im SessionHandler behandelt.
 *
*/
@ApplicationScoped
@Named("kundenHandler")
public class kundenHandler implements Serializable {

    /**
     * Seriennummer für java.io.Serializable.
     */
    private static final long serialVersionUID = -2270264364807391691L;
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
     * Datenstrukturen zum Persistieren der Kunden.
     */
    private static DataModel<Kunde> kunden;
    /**
     * Merkt sich den aktuellen Kunden für den Sessioncontext.
     */
    private Kunde merkeKunde = new Kunde();
    private Passwort merkePasswort;

    public Kunde getMerkeKunde() {
        return merkeKunde;
    }

    public void setMerkeKunde(Kunde merkeKunde) {
        this.merkeKunde = merkeKunde;
    }

    public static DataModel<Kunde> getKunden() {
        return kunden;
    }

    public void setKunden(DataModel<Kunde> kunden) {
        this.kunden = kunden;
    }

    public Passwort getMerkePasswort() {
        return merkePasswort;
    }

    public void setMerkePasswort(Passwort merkePasswort) {
        this.merkePasswort = merkePasswort;
    }

    /**
     * Wird automatisch aufgerufen, generiert Kundenliste nach Instanzierung des Kundenhandlers.
     */
    @PostConstruct
    public void init() {


        try {
            utx.begin();
            kunden = new ListDataModel<>();
            kunden.setWrappedData(em.createNamedQuery("SelectKunden").getResultList());
            utx.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Legt einen neuen Kunden an.
     * @return Adresse zum JSF "neuerKunde".
     */
    public String neu() {
        merkeKunde = new Kunde();
        return "neuerKunde";
    }

    /**
     * Persistiert einen neuen Kunden. Falls der vom Kunden gewünschte Username bereits vergeben ist, wird dies über die Methode addMessage() der Klasse FacesContext
     * mitgeteilt und der Speichervorgang abgebrochen.
     * @return Adresse zum JSF "neuerKunde" oder "index", je nachdem, ob ein neuer Kunde angelegt werden konnte oder nicht.
     */
    @Transactional
    public String speichern() {
        try {
           Kunde kunde = (Kunde) em.createQuery("FROM Kunde k WHERE k.username = :username")
                    .setParameter("username", merkeKunde.getUsername()).getSingleResult();
            if(kunde!=null){
                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage(null, new FacesMessage("Username ist bereits vergeben"));
                return null;
            }
        } catch (NoResultException e) {
        }
        //Code muss nach dem Try-Catch Block stehen, da sonst die Abfrage, ob der Username gleich "admin" ist, bei 0 registrierten Kunden nie erreicht wird
        if(merkeKunde.getUsername().equals("admin")){
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage("Username ist bereits vergeben"));
            return null;
        }
        merkeKunde = em.merge(merkeKunde);
        em.persist(merkeKunde);
        kunden.setWrappedData(em.createNamedQuery("SelectKunden").getResultList());
        return "index";
    }

    /**
     * Speichert Änderungen am Kunden über den Admin, sofern diese möglich sind. Funktion ähnlich der Speichernmethode, jedoch aufgrund kleiner Feinheiten eine Standalone-Methode.
     * @return Adresse zum JSF "neuerKunde" oder "adminView", je nachdem, ob ein Kunde bearbeitet werden konnte oder nicht.
     */
    @Transactional
    public String speicherEdit(){
        try {
            Kunde kunde = (Kunde) em.createQuery("FROM Kunde k WHERE k.username = :username")
                    .setParameter("username", merkeKunde.getUsername()).getSingleResult();
            if(kunde!=null && !merkeKunde.getId().equals(kunde.getId())){
                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage("neuerKunde", new FacesMessage("Username ist bereits vergeben"));
                return null;
            }
        } catch (NoResultException e) {
        }
        merkeKunde = em.merge(merkeKunde);
        em.persist(merkeKunde);
        kunden.setWrappedData(em.createNamedQuery("SelectKunden").getResultList());
        return "adminview";
    }

    /**
     * Löscht einen Kunden permanent (nur für den Admin).
     * @return Adresse zum JSF "Adminview".
     */
    @Transactional
    public String delete() {
        merkeKunde= kunden.getRowData();
        merkeKunde= em.merge(merkeKunde);
        em.remove(merkeKunde);
        kunden.setWrappedData(em.createNamedQuery("SelectKunden").getResultList());
        return "adminview";
    }

    /**
     * Bearbeitet einen Kunden (nur für den Admin).
     * @return Adresse zum JSF "editKunde".
     */
    @Transactional
    public String edit() {
        /*
        Wir hatten hier ein paar Probleme mit der Methode getRowData()
        Dies war letztenendes der Fix, auf den wir zurückgegriffen haben
        */
        int index = kunden.getRowIndex();
        kunden.setWrappedData(em.createNamedQuery("SelectKunden").getResultList());
        kunden.setRowIndex(index);
        merkeKunde= kunden.getRowData();
        merkeKunde= em.merge(merkeKunde);
        kunden.setWrappedData(em.createNamedQuery("SelectKunden").getResultList());
    return "editKunde";
    }

    /**
     * Öffnet die Passwortansicht für den Admin für einen einzelnen Kunden.
     * @return Adresse zum JSF "passwordview".
     */
    @Transactional
    public String createPasswordview(){
        int index = kunden.getRowIndex();
        kunden.setWrappedData(em.createNamedQuery("SelectKunden").getResultList());
        kunden.setRowIndex(index);
        merkeKunde = kunden.getRowData();
        merkeKunde= em.merge(merkeKunde);
        kunden.setWrappedData(em.createNamedQuery("SelectKunden").getResultList());
        return "passwordview";
    }

    /**
     * Löscht ein Passwort eines Kunden.
     * @param p das zu löschende Passwort.
     * @return Adresse zum JSF "passwordview".
     */
    @Transactional
    public String removePassword(Passwort p){
    	merkeKunde.getPasswortList().remove(p);
        merkeKunde = em.merge(merkeKunde);
    	p = em.find(Passwort.class, p.getId());
        em.remove(p);
        return "passwordview";
    }

    /**
     * Bearbeitet ein Passwort eines Kunden.
     * @param p das zu bearbeitende Passwort.
     * @return Adresse zum JDF "editpasswort".
     */
    @Transactional
    public String editPassword(Passwort p){
        merkePasswort = p;
        return "editpasswort";
    }

    /**
     * Speichert das Passwort eines Kunden.
     * @return Adresse zum JSF "passwordview".
     */
    @Transactional
    public String safePassword(){
        merkeKunde = em.merge(merkeKunde);
        em.persist(merkeKunde);
        return "passwordview";
    }

    public Anrede[] getAnredeValues() {
        return Anrede.values();
    }
    
    public void detach()
    {
    }

}
