
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getJobStatusResponse", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getJobStatusResponse", namespace = "http://msa.data.compbio/01/12/2010/")
public class GetJobStatusResponse {

    @XmlElement(name = "return", namespace = "")
    private compbio.metadata.JobStatus _return;

    /**
     * 
     * @return
     *     returns JobStatus
     */
    public compbio.metadata.JobStatus getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(compbio.metadata.JobStatus _return) {
        this._return = _return;
    }

}
