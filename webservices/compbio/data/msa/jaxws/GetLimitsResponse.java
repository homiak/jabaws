
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getLimitsResponse", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getLimitsResponse", namespace = "http://msa.data.compbio/01/12/2010/")
public class GetLimitsResponse {

    @XmlElement(name = "return", namespace = "")
    private compbio.metadata.LimitsManager _return;

    /**
     * 
     * @return
     *     returns LimitsManager
     */
    public compbio.metadata.LimitsManager getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(compbio.metadata.LimitsManager _return) {
        this._return = _return;
    }

}
