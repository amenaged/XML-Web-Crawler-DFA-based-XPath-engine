package test.edu.upenn.cis455;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class XPathEngineImplTest extends TestCase {

	private XPathEngineImpl xPathEngine;

	// This test is copied from Piazza, to better ensure correctness of my code
	// Thanks to Nicholas!
	private String[] invalidTest = {/* random ']' */
	"/catalog]",
	/* random '[' */
	"/catalog[",
	/* missing start quote */
	"/catalog/cd[@title=Empire Burlesque\"]",
	/* missing @ on artist */
	"/catalog/cd[@title=\"Empire Burlesque\"][artist=\"Bob Dylan\"]",
	/* unmatched brackets: extra ']' */
	"/catalog/cd[@title=Empire Burlesque\"]]",
	/* unmatched brackets: extra ']' */
	"/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]]",
	/* unmatched brackets: extra '[' */
	"/catalog/cd[[@title=Empire Burlesque\"]",
	/* unmatched brackets: extra '[' */
	"/catalog/cd[@year=\"1988\"][[@price=\"9.90\"]/country[text()=\"UK\"]",
	/* illegal start character */
	"/catalog/!badelem",
	/* illegal start character */
	"/@frenchbread/unicorns",
	/* illegal start character */
	"/abc/123bad", "/hello world", "/check(these)chars", "/xmlillegal",
			"/XMLillegal",
			/* illegal attribute name */
			"/abc/ab[@,illegalattribute=\"hello\"]",
			/* illegal attribute name */
			"/abc/ab[@<illegalattribute=\"hello\"]",
			/* text after close quote */
			"/abc/ab[text()=\"abc\"  pqr]",
			/* bad quote placememnt */
			"/abc/ab[@attname\"=\"abc\"]",
			/* no attname */
			"/abc/ab[@=\"hello\"]" };
	// set of valid xpaths
	String[] validXPaths = new String[] { "/foo/bar[@att=\"123\"]",
			"/xyz/abc[contains(text(),\"someSubstring\")]",
			"/a/b/c[text()=\"theEntireText\"]", "/ blah [anotherElement]",
			"/this/that[something/else]",
			"/d/e/f[foo[text()=\"something\"]][bar]",
			"/a/b/c[text() = \"whiteSpacesShouldNotMatter\"]" };

	// set of invalid xpaths
	String[] invalidXPaths = new String[] {
			"/xmla213/abc[contains (text()  ,  \"someSubstring\")]",
			"/a/b/c[]", "/a/b/c[/asd]", "/" };

	// set of complicated xpaths to make sure DFA-based XPathEnginer is
	// implemented without bugs
	private String[] xPaths1 = {
			"/company/ staff [firstname][@  attr=\"asd\"][@asd=\"123\"][lastname[text()=\"mook kim\"]]/firstname[text() = \"yong\"][text()=\"yong\"]  ",
			"/company", "/company/staff[@asd=\"456\"]", "/compan/staff" };
	private String[] xPaths2 = { "/a[e]/b[c]/d", "/a/b[c]/d", "/a/c" };

	protected void setUp() throws Exception {
		xPathEngine = new XPathEngineImpl();
	}

	public void testIsValid() {
		xPathEngine.setXPaths(validXPaths);
		for (int i = 0; i < validXPaths.length; i++)
			assertEquals(true, xPathEngine.isValid(i));
		xPathEngine.setXPaths(invalidXPaths);
		for (int i = 0; i < invalidXPaths.length; i++)
			assertEquals(false, xPathEngine.isValid(i));
		xPathEngine.setXPaths(invalidTest);
		for (int i = 0; i < invalidXPaths.length; i++)
			assertEquals(false, xPathEngine.isValid(i));
	}

	public void testEvaluate() throws ParserConfigurationException,
			SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		File testFile1 = new File("test1.xml");
		File testFile2 = new File("test2.xml");
		xPathEngine.setXPaths(xPaths1);
		parser.parse(testFile1, xPathEngine);
		assertArrayEquals(new boolean[] { true, true, false, false },
				xPathEngine.evaluate(null));
		xPathEngine.setXPaths(xPaths2);
		parser.parse(testFile2, xPathEngine);
		assertArrayEquals(new boolean[] { true, true, false },
				xPathEngine.evaluate(null));
	}

}
