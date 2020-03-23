package Entidades;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RegistroPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "id_plataforma")
    private int idPlataforma;
    @Basic(optional = false)
    @Column(name = "id_usuario")
    private int idUsuario;

    public RegistroPK() {
    }

    public RegistroPK(int idPlataforma, int idUsuario) {
        this.idPlataforma = idPlataforma;
        this.idUsuario = idUsuario;
    }

    public int getIdPlataforma() {
        return idPlataforma;
    }

    public void setIdPlataforma(int idPlataforma) {
        this.idPlataforma = idPlataforma;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idPlataforma;
        hash += (int) idUsuario;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RegistroPK)) {
            return false;
        }
        RegistroPK other = (RegistroPK) object;
        if (this.idPlataforma != other.idPlataforma) {
            return false;
        }
        if (this.idUsuario != other.idUsuario) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entidades.RegistroPK[ idPlataforma=" + idPlataforma + ", idUsuario=" + idUsuario + " ]";
    }

}
