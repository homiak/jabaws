
package compbio.data.msa.jaxws;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "analize", namespace = "http://msa.data.compbio/01/12/2010/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "analize", namespace = "http://msa.data.compbio/01/12/2010/")
public class Analize {

    @XmlElement(name = "fastaSequences", namespace = "")
    private List<compbio.data.sequence.FastaSequence> fastaSequences;

    /**
     * 
     * @return
     *     returns List<FastaSequence>
     */
    public List<compbio.data.sequence.FastaSequence> getFastaSequences() {
        return this.fastaSequences;
    }

    /**
     * 
     * @param fastaSequences
     *     the value for the fastaSequences property
     */
    public void setFastaSequences(List<compbio.data.sequence.FastaSequence> fastaSequences) {
        this.fastaSequences = fastaSequences;
    }

}
