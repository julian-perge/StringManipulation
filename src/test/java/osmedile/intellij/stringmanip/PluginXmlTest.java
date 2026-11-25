package osmedile.intellij.stringmanip;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PluginXmlTest {

	/**
	 * The IDE loads an action class only when it first shows the action, and the plugin verifier does not report
	 * a missing action class. This test finds such an entry at build time.
	 */
	@Test
	public void everyClassInPluginXmlExists() throws Exception {
		NodeList elements = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new File("src/main/resources/META-INF/plugin.xml"))
				.getElementsByTagName("*");

		List<String> classNames = new ArrayList<>();
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);
			// Action ids look like class names, so only these places count.
			for (String attribute : List.of("class", "implementation", "implementationClass", "serviceImplementation", "factoryClass")) {
				classNames.add(element.getAttribute(attribute));
			}
			if (element.getTagName().equals("className")) {
				classNames.add(element.getTextContent().trim());
			}
		}
		classNames.removeIf(name -> !name.startsWith("osmedile."));

		List<String> missing = new ArrayList<>();
		for (String className : classNames) {
			try {
				Class.forName(className, false, getClass().getClassLoader());
			} catch (ClassNotFoundException e) {
				missing.add(className);
			}
		}

		assertTrue("found only " + classNames.size() + " class names", classNames.size() > 100);
		assertEquals(List.of(), missing);
	}
}
