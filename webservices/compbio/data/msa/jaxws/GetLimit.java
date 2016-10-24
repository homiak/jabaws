
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getLimit", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getLimit", namespace = "http://msa.data.compbio/01/12/2010/")
public class GetLimit {

    @XmlElement(name = "presetName", namespace = "")
    private String presetName;

    /**
     * 
     * @return
     *     returns String
     */
    public String getPresetName() {
        return this.presetName;
    }

    /**
     * 
     * @param presetName
     *     the value for the presetName property
     */
    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

}
