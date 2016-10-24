
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getResultResponse", namespace = "http://msa.data.compbio/01/01/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getResultResponse", namespace = "http://msa.data.compbio/01/01/2010/")
public class GetResultResponse {

    @XmlElement(name = "return", namespace = "")
    private compbio.data.sequence.Alignment _return;

    /**
     * 
     * @return
     *     returns Alignment
     */
    public compbio.data.sequence.Alignment getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(compbio.data.sequence.Alignment _return) {
        this._return = _return;
    }

}
