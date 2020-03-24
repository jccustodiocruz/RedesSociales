package Managers;

import Entidades.Plataforma;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Entidades.Registro;
import Managers.exceptions.IllegalOrphanException;
import Managers.exceptions.NonexistentEntityException;
import Managers.exceptions.PreexistingEntityException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PlataformaJpaController implements Serializable {

    public PlataformaJpaController(EntityManagerFactory emf) {
        this.manager = Persistence.createEntityManagerFactory("RedesSocialesJPAPU");
    }
    private EntityManagerFactory manager = null;

    public EntityManager getEntityManager() {
        return manager.createEntityManager();
    }

    public void add(Plataforma plataforma) throws PreexistingEntityException, Exception {
        if (plataforma.getRegistroList() == null) {
            plataforma.setRegistroList(new ArrayList<Registro>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Registro> attachedRegistroList = new ArrayList<Registro>();
            for (Registro registroListRegistroToAttach : plataforma.getRegistroList()) {
                registroListRegistroToAttach = em.getReference(registroListRegistroToAttach.getClass(), registroListRegistroToAttach.getRegistroPK());
                attachedRegistroList.add(registroListRegistroToAttach);
            }
            plataforma.setRegistroList(attachedRegistroList);
            em.persist(plataforma);
            for (Registro registroListRegistro : plataforma.getRegistroList()) {
                Plataforma oldPlataformaOfRegistroListRegistro = registroListRegistro.getPlataforma();
                registroListRegistro.setPlataforma(plataforma);
                registroListRegistro = em.merge(registroListRegistro);
                if (oldPlataformaOfRegistroListRegistro != null) {
                    oldPlataformaOfRegistroListRegistro.getRegistroList().remove(registroListRegistro);
                    oldPlataformaOfRegistroListRegistro = em.merge(oldPlataformaOfRegistroListRegistro);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (find(plataforma.getId()) != null) {
                throw new PreexistingEntityException("Plataforma " + plataforma + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void update(Plataforma plataforma) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Plataforma persistentPlataforma = em.find(Plataforma.class, plataforma.getId());
            List<Registro> registroListOld = persistentPlataforma.getRegistroList();
            List<Registro> registroListNew = plataforma.getRegistroList();
            List<String> illegalOrphanMessages = null;
            for (Registro registroListOldRegistro : registroListOld) {
                if (!registroListNew.contains(registroListOldRegistro)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Registro " + registroListOldRegistro + " since its plataforma field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Registro> attachedRegistroListNew = new ArrayList<Registro>();
            for (Registro registroListNewRegistroToAttach : registroListNew) {
                registroListNewRegistroToAttach = em.getReference(registroListNewRegistroToAttach.getClass(), registroListNewRegistroToAttach.getRegistroPK());
                attachedRegistroListNew.add(registroListNewRegistroToAttach);
            }
            registroListNew = attachedRegistroListNew;
            plataforma.setRegistroList(registroListNew);
            plataforma = em.merge(plataforma);
            for (Registro registroListNewRegistro : registroListNew) {
                if (!registroListOld.contains(registroListNewRegistro)) {
                    Plataforma oldPlataformaOfRegistroListNewRegistro = registroListNewRegistro.getPlataforma();
                    registroListNewRegistro.setPlataforma(plataforma);
                    registroListNewRegistro = em.merge(registroListNewRegistro);
                    if (oldPlataformaOfRegistroListNewRegistro != null && !oldPlataformaOfRegistroListNewRegistro.equals(plataforma)) {
                        oldPlataformaOfRegistroListNewRegistro.getRegistroList().remove(registroListNewRegistro);
                        oldPlataformaOfRegistroListNewRegistro = em.merge(oldPlataformaOfRegistroListNewRegistro);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = plataforma.getId();
                if (find(id) == null) {
                    throw new NonexistentEntityException("The plataforma with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void delete(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Plataforma plataforma;
            try {
                plataforma = em.getReference(Plataforma.class, id);
                plataforma.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The plataforma with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Registro> registroListOrphanCheck = plataforma.getRegistroList();
            for (Registro registroListOrphanCheckRegistro : registroListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Plataforma (" + plataforma + ") cannot be destroyed since the Registro " + registroListOrphanCheckRegistro + " in its registroList field has a non-nullable plataforma field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(plataforma);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Plataforma> getAll() {
        return getAll(true, -1, -1);
    }

    public List<Plataforma> getAll(int maxResults, int firstResult) {
        return getAll(false, maxResults, firstResult);
    }

    private List<Plataforma> getAll(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Plataforma.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Plataforma find(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Plataforma.class, id);
        } finally {
            em.close();
        }
    }

    public int getCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Plataforma> rt = cq.from(Plataforma.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
