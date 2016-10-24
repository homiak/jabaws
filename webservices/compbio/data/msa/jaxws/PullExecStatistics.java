
package compbio.data.msa.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "pullExecStatistics", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pullExecStatistics", namespace = "http://msa.data.compbio/01/12/2010/", propOrder = {
    "jobId",
    "position"
})
public class PullExecStatistics {

    @XmlElement(name = "jobId", namespace = "")
    private String jobId;
    @XmlElement(name = "position", namespace = "")
    private long position;

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

    /**
     * 
     * @return
     *     returns long
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * 
     * @param position
     *     the value for the position property
     */
    public void setPosition(long position) {
        this.position = position;
    }

}
