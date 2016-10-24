
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getPresetsResponse", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPresetsResponse", namespace = "http://msa.data.compbio/01/12/2010/")
public class GetPresetsResponse {

    @XmlElement(name = "return", namespace = "")
    private compbio.metadata.PresetManager _return;

    /**
     * 
     * @return
     *     returns PresetManager
     */
    public compbio.metadata.PresetManager getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(compbio.metadata.PresetManager _return) {
        this._return = _return;
    }

}
