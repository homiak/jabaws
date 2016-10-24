
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getRunnerOptionsResponse", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getRunnerOptionsResponse", namespace = "http://msa.data.compbio/01/12/2010/")
public class GetRunnerOptionsResponse {

    @XmlElement(name = "return", namespace = "")
    private compbio.metadata.RunnerConfig _return;

    /**
     * 
     * @return
     *     returns RunnerConfig
     */
    public compbio.metadata.RunnerConfig getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(compbio.metadata.RunnerConfig _return) {
        this._return = _return;
    }

}
