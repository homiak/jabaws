
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getAnnotationResponse", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAnnotationResponse", namespace = "http://msa.data.compbio/01/12/2010/")
public class GetAnnotationResponse {

    @XmlElement(name = "return", namespace = "")
    private compbio.data.sequence.ScoreManager _return;

    /**
     * 
     * @return
     *     returns ScoreManager
     */
    public compbio.data.sequence.ScoreManager getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(compbio.data.sequence.ScoreManager _return) {
        this._return = _return;
    }

}
