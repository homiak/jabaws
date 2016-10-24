package compbio.data.msa;

import java.net.ConnectException;
import java.util.Set;

import javax.xml.ws.WebServiceException;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import compbio.metadata.AllTestSuit;
import compbio.ws.client.Jws2Client;
import compbio.ws.client.Services;
import compbio.ws.client.WSTesterTester;

public class CategoryTester {

	// test category membership from string constants
	RegistryWS registry = null;

	@BeforeTest(groups = AllTestSuit.test_group_webservices)
	public void setupTest() {
		try {
			/*
			 * registry = Jws2Client .connectToRegistry(
			 * "http://webserv1.cluster.lifesci.dundee.ac.uk:8089/jaba");
			 */
			registry = Jws2Client.connectToRegistry(WSTesterTester.SERVER);

		} catch (ConnectException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} catch (WebServiceException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}

	/*
	 * This test will FAIL unless a connection to a running JABAWS web server is
	 * made!
	 */
	@Test(groups = AllTestSuit.test_group_webservices)
	public void categoryTest() {
		Set<Category> servicecategories = registry.getServiceCategories();

		boolean found = false;
		for (Category svccategory : servicecategories) {

			Set<Services> catservices;

			for (String category : new String[]{Category.CATEGORY_ALIGNMENT,
					Category.CATEGORY_CONSERVATION, Category.CATEGORY_DISORDER}) {
				if (category.equals(svccategory.name)) {
					found = true;
					catservices = svccategory.getServices();
					System.out.println("Found " + catservices.size()
							+ " services in category " + category
							+ "(service category string " + svccategory + ")");
				}
			}
		}
		if (!found) {
			Assert.fail("Could not match any category to one of the given category constants");
		}
	}

}
