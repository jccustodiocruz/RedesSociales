package Entidades;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author juanc
 */
@Entity
@Table(name = "plataforma")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Plataforma.findAll", query = "SELECT p FROM Plataforma p"),
    @NamedQuery(name = "Plataforma.findById", query = "SELECT p FROM Plataforma p WHERE p.id = :id"),
    @NamedQuery(name = "Plataforma.findByNombre", query = "SELECT p FROM Plataforma p WHERE p.nombre = :nombre"),
    @NamedQuery(name = "Plataforma.findByUrl", query = "SELECT p FROM Plataforma p WHERE p.url = :url"),
    @NamedQuery(name = "Plataforma.findByFechaInicio", query = "SELECT p FROM Plataforma p WHERE p.fechaInicio = :fechaInicio")})
public class Plataforma implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "url")
    private String url;
    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private Date fechaInicio;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "plataforma")
    private List<Registro> registroList;
    @ManyToMany(mappedBy = "plataforma")
    private List<Usuario> usuarios;
    
    public List<Usuario> getUsuarios(){
        return usuarios;
    }
    
    public void setUsuarios(List<Usuario> usuarios){
        this.usuarios = usuarios;
    }
    
    public void addUsuario(Usuario u){
        usuarios.add(u);
    }

    public Plataforma() {
    }

    public Plataforma(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    @XmlTransient
    public List<Registro> getRegistroList() {
        return registroList;
    }

    public void setRegistroList(List<Registro> registroList) {
        this.registroList = registroList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Plataforma)) {
            return false;
        }
        Plataforma other = (Plataforma) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entidades.Plataforma[ id=" + id + " ]";
    }

}
