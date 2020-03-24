package Managers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Entidades.Registro;
import Entidades.Usuario;
import Managers.exceptions.IllegalOrphanException;
import Managers.exceptions.NonexistentEntityException;
import Managers.exceptions.PreexistingEntityException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class UsuarioJpaController implements Serializable {

    public UsuarioJpaController() {
        this.manager = Persistence.createEntityManagerFactory("RedesSocialesJPAPU");
    }
    private EntityManagerFactory manager = null;

    public EntityManager getEntityManager() {
        return manager.createEntityManager();
    }

    public void add(Usuario usuario) throws PreexistingEntityException, Exception {
        if (usuario.getRegistroList() == null) {
            usuario.setRegistroList(new ArrayList<Registro>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Registro> attachedRegistroList = new ArrayList<Registro>();
            for (Registro registroListRegistroToAttach : usuario.getRegistroList()) {
                registroListRegistroToAttach = em.getReference(registroListRegistroToAttach.getClass(), registroListRegistroToAttach.getRegistroPK());
                attachedRegistroList.add(registroListRegistroToAttach);
            }
            usuario.setRegistroList(attachedRegistroList);
            em.persist(usuario);
            for (Registro registroListRegistro : usuario.getRegistroList()) {
                Usuario oldUsuarioOfRegistroListRegistro = registroListRegistro.getUsuario();
                registroListRegistro.setUsuario(usuario);
                registroListRegistro = em.merge(registroListRegistro);
                if (oldUsuarioOfRegistroListRegistro != null) {
                    oldUsuarioOfRegistroListRegistro.getRegistroList().remove(registroListRegistro);
                    oldUsuarioOfRegistroListRegistro = em.merge(oldUsuarioOfRegistroListRegistro);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (find(usuario.getId()) != null) {
                throw new PreexistingEntityException("Usuario " + usuario + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void update(Usuario usuario) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Usuario persistentUsuario = em.find(Usuario.class, usuario.getId());
            List<Registro> registroListOld = persistentUsuario.getRegistroList();
            List<Registro> registroListNew = usuario.getRegistroList();
            List<String> illegalOrphanMessages = null;
            for (Registro registroListOldRegistro : registroListOld) {
                if (!registroListNew.contains(registroListOldRegistro)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Registro " + registroListOldRegistro + " since its usuario field is not nullable.");
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
            usuario.setRegistroList(registroListNew);
            usuario = em.merge(usuario);
            for (Registro registroListNewRegistro : registroListNew) {
                if (!registroListOld.contains(registroListNewRegistro)) {
                    Usuario oldUsuarioOfRegistroListNewRegistro = registroListNewRegistro.getUsuario();
                    registroListNewRegistro.setUsuario(usuario);
                    registroListNewRegistro = em.merge(registroListNewRegistro);
                    if (oldUsuarioOfRegistroListNewRegistro != null && !oldUsuarioOfRegistroListNewRegistro.equals(usuario)) {
                        oldUsuarioOfRegistroListNewRegistro.getRegistroList().remove(registroListNewRegistro);
                        oldUsuarioOfRegistroListNewRegistro = em.merge(oldUsuarioOfRegistroListNewRegistro);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = usuario.getId();
                if (find(id) == null) {
                    throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.");
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
            Usuario usuario;
            try {
                usuario = em.getReference(Usuario.class, id);
                usuario.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Registro> registroListOrphanCheck = usuario.getRegistroList();
            for (Registro registroListOrphanCheckRegistro : registroListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Usuario (" + usuario + ") cannot be destroyed since the Registro " + registroListOrphanCheckRegistro + " in its registroList field has a non-nullable usuario field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(usuario);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Usuario> getAll() {
        return getAll(true, -1, -1);
    }

    public List<Usuario> getAll(int maxResults, int firstResult) {
        return getAll(false, maxResults, firstResult);
    }

    private List<Usuario> getAll(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuario.class));
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

    public Usuario find(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public int getCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuario> rt = cq.from(Usuario.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
