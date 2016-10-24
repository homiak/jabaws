package compbio.data.msa;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import compbio.ws.client.Services;

/**
 * Class that splits {@link Services} to categories. Services themselves have no
 * knowledge which category they belongs to.
 * 
 * This class is responsible for initialization of all the categories (done
 * statically) and holds the category names as constrains.
 * 
 * Two categories considered equals if their names are equals.
 * 
 * @author pvtroshin
 * @version 1.0 September 2011
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Category {
	/*
	 * TODO refactor initialization and constrains into separate classes if
	 * further complexity is expected.
	 */

	/**
	 * All of the Category names
	 */
	public static final String CATEGORY_ALIGNMENT = "Alignment";
	public static final String CATEGORY_DISORDER = "Protein Disorder";
	public static final String CATEGORY_CONSERVATION = "Conservation";

	public String name;
	Set<Services> services;

	private Category(String name, Set<Services> services) {
		this.name = name;
		this.services = services;
	}

	private Category() {
		// Default constructor for JAXB
	}

	public Set<Services> getServices() {
		return new TreeSet<Services>(services);
	}

	public static Set<Category> getCategories() {
		return init();
	}

	private static Set<Category> init() {
		Set<Services> align_services = new HashSet<Services>();
		align_services.add(Services.ClustalOWS);
		align_services.add(Services.ClustalWS);
		align_services.add(Services.MafftWS);
		align_services.add(Services.MuscleWS);
		align_services.add(Services.ProbconsWS);
		align_services.add(Services.TcoffeeWS);
		Category alignment = new Category(CATEGORY_ALIGNMENT, align_services);

		Set<Services> disorder_services = new HashSet<Services>();
		disorder_services.add(Services.DisemblWS);
		disorder_services.add(Services.GlobPlotWS);
		disorder_services.add(Services.IUPredWS);
		disorder_services.add(Services.JronnWS);

		Category disorder = new Category(CATEGORY_DISORDER, disorder_services);
		Set<Services> conservation_services = new HashSet<Services>();
		conservation_services.add(Services.AAConWS);

		Category conservation = new Category(CATEGORY_CONSERVATION,
				conservation_services);

		Set<Category> categories = new HashSet<Category>();
		categories.add(alignment);
		categories.add(disorder);
		categories.add(conservation);

		return categories;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
