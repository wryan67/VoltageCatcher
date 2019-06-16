package org.wryan67.vc.database.tables;

import javax.persistence.*;

@Table(name = "OPTIONS")

@NamedQueries(value = {
        @NamedQuery(
                name = "OPTIONS.getAll",
                query = "SELECT u FROM OPTIONS u"
        ),
        @NamedQuery(
                name = "OPTIONS.getByUID",
                query = "SELECT u FROM OPTIONS u " +
                        "where u.UID = :UID"
        ),
})


@Entity
public class OPTIONS {

    @Id
    private String UID;
    private String OPTIONS;

    @Override
    public String toString() {
        return "OPTIONS{" +
                "UID='" + UID + '\'' +
                ", OPTIONS='" + OPTIONS + '\'' +
                '}';
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getOPTIONS() {
        return OPTIONS;
    }

    public void setOPTIONS(String OPTIONS) {
        this.OPTIONS = OPTIONS;
    }
}
