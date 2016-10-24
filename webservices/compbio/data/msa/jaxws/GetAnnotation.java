
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getAnnotation", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAnnotation", namespace = "http://msa.data.compbio/01/12/2010/")
public class GetAnnotation {

    @XmlElement(name = "jobId", namespace = "")
    private String jobId;

    /**
     * 
     * @return
     *     returns String
     */
    public String getJobId() {
        return this.jobId;
    }

    /**
     * 
     * @param jobId
     *     the value for the jobId property
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

}
