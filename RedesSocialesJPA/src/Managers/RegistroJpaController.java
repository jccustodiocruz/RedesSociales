package Managers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Entidades.Plataforma;
import Entidades.Registro;
import Entidades.RegistroPK;
import Entidades.Usuario;
import Managers.exceptions.NonexistentEntityException;
import Managers.exceptions.PreexistingEntityException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class RegistroJpaController implements Serializable {

    public RegistroJpaController() {
        this.manager = Persistence.createEntityManagerFactory("RedesSocialesJPAPU");
    }
    private EntityManagerFactory manager = null;

    public EntityManager getEntityManager() {
        return manager.createEntityManager();
    }

    public void create(Registro registro) throws PreexistingEntityException, Exception {
        if (registro.getRegistroPK() == null) {
            registro.setRegistroPK(new RegistroPK());
        }
        registro.getRegistroPK().setIdPlataforma(registro.getPlataforma().getId());
        registro.getRegistroPK().setIdUsuario(registro.getUsuario().getId());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Plataforma plataforma = registro.getPlataforma();
            if (plataforma != null) {
                plataforma = em.getReference(plataforma.getClass(), plataforma.getId());
                registro.setPlataforma(plataforma);
            }
            Usuario usuario = registro.getUsuario();
            if (usuario != null) {
                usuario = em.getReference(usuario.getClass(), usuario.getId());
                registro.setUsuario(usuario);
            }
            em.persist(registro);
            if (plataforma != null) {
                plataforma.getRegistroList().add(registro);
                plataforma = em.merge(plataforma);
            }
            if (usuario != null) {
                usuario.getRegistroList().add(registro);
                usuario = em.merge(usuario);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findRegistro(registro.getRegistroPK()) != null) {
                throw new PreexistingEntityException("Registro " + registro + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Registro registro) throws NonexistentEntityException, Exception {
        registro.getRegistroPK().setIdPlataforma(registro.getPlataforma().getId());
        registro.getRegistroPK().setIdUsuario(registro.getUsuario().getId());
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Registro persistentRegistro = em.find(Registro.class, registro.getRegistroPK());
            Plataforma plataformaOld = persistentRegistro.getPlataforma();
            Plataforma plataformaNew = registro.getPlataforma();
            Usuario usuarioOld = persistentRegistro.getUsuario();
            Usuario usuarioNew = registro.getUsuario();
            if (plataformaNew != null) {
                plataformaNew = em.getReference(plataformaNew.getClass(), plataformaNew.getId());
                registro.setPlataforma(plataformaNew);
            }
            if (usuarioNew != null) {
                usuarioNew = em.getReference(usuarioNew.getClass(), usuarioNew.getId());
                registro.setUsuario(usuarioNew);
            }
            registro = em.merge(registro);
            if (plataformaOld != null && !plataformaOld.equals(plataformaNew)) {
                plataformaOld.getRegistroList().remove(registro);
                plataformaOld = em.merge(plataformaOld);
            }
            if (plataformaNew != null && !plataformaNew.equals(plataformaOld)) {
                plataformaNew.getRegistroList().add(registro);
                plataformaNew = em.merge(plataformaNew);
            }
            if (usuarioOld != null && !usuarioOld.equals(usuarioNew)) {
                usuarioOld.getRegistroList().remove(registro);
                usuarioOld = em.merge(usuarioOld);
            }
            if (usuarioNew != null && !usuarioNew.equals(usuarioOld)) {
                usuarioNew.getRegistroList().add(registro);
                usuarioNew = em.merge(usuarioNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                RegistroPK id = registro.getRegistroPK();
                if (findRegistro(id) == null) {
                    throw new NonexistentEntityException("The registro with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(RegistroPK id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Registro registro;
            try {
                registro = em.getReference(Registro.class, id);
                registro.getRegistroPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The registro with id " + id + " no longer exists.", enfe);
            }
            Plataforma plataforma = registro.getPlataforma();
            if (plataforma != null) {
                plataforma.getRegistroList().remove(registro);
                plataforma = em.merge(plataforma);
            }
            Usuario usuario = registro.getUsuario();
            if (usuario != null) {
                usuario.getRegistroList().remove(registro);
                usuario = em.merge(usuario);
            }
            em.remove(registro);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Registro> findRegistroEntities() {
        return findRegistroEntities(true, -1, -1);
    }

    public List<Registro> findRegistroEntities(int maxResults, int firstResult) {
        return findRegistroEntities(false, maxResults, firstResult);
    }

    private List<Registro> findRegistroEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Registro.class));
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

    public Registro findRegistro(RegistroPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Registro.class, id);
        } finally {
            em.close();
        }
    }

    public int getRegistroCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Registro> rt = cq.from(Registro.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
