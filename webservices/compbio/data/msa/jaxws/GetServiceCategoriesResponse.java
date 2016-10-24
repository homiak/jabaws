
package compbio.data.msa.jaxws;

import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getServiceCategoriesResponse", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getServiceCategoriesResponse", namespace = "http://msa.data.compbio/01/12/2010/")
public class GetServiceCategoriesResponse {

    @XmlElement(name = "return", namespace = "")
    private Set<compbio.data.msa.Category> _return;

    /**
     * 
     * @return
     *     returns Set<Category>
     */
    public Set<compbio.data.msa.Category> getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(Set<compbio.data.msa.Category> _return) {
        this._return = _return;
    }

}
