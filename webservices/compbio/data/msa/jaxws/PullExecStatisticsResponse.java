
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "pullExecStatisticsResponse", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pullExecStatisticsResponse", namespace = "http://msa.data.compbio/01/12/2010/")
public class PullExecStatisticsResponse {

    @XmlElement(name = "return", namespace = "")
    private compbio.metadata.ChunkHolder _return;

    /**
     * 
     * @return
     *     returns ChunkHolder
     */
    public compbio.metadata.ChunkHolder getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(compbio.metadata.ChunkHolder _return) {
        this._return = _return;
    }

}
