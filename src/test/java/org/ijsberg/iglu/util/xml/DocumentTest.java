package org.ijsberg.iglu.util.xml;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DocumentTest {

    private static String contents = "I think that XML has great potential. It will work very well and will help many people to make much better use of the internet.";

    private static String pieceOfXml =
            "<message>\n" +
                    "<header>\n" +
                    "<from>webmaster@gowansnet.com</from>\n" +
                    "<to>webmaster@xml.org</to>\n" +
                    "<subject>Comments on XML</subject>\n" +
                    "</header>\n" +
                    "<body attr=\"2\">\n" +
                    contents + "\n" +
                    "</body>\n" +
                    "</message>";


    private static String pieceOfXml2 =
            "<message>\n" +
                    "<header>\n" +
                    "<from>webmaster@gowansnet.com</from>\n" +
                    "<to>webmaster@xml.org</to>\n" +
                    "<subject>Comments on XML</subject>\n" +
                    "</header>\n" +
                    "<body attr=\"2\">\n" +
                    "<!-- COMMENT -->" + contents + "\n" +
                    "</body>\n" +
                    "</message>";



    @Test
    public void test() throws Exception {
        Document doc = new Document();
        doc.load(pieceOfXml);

        assertEquals(1, doc.getNodes().size());
        assertEquals("message", doc.getNodes().get(0).getName());
        assertEquals("message", doc.getFirstNode().getName());

        assertEquals(6, doc.getAllNodes().size());

        Node node = doc.getFirstNodeByName("body");
        assertEquals("2", node.getAttribute("attr"));

        assertEquals(contents, node.contentsToString());


    }

    @Test
    public void test2() throws Exception {
        Document doc = new Document();
        doc.load(pieceOfXml2);

        assertEquals(1, doc.getNodes().size());
        assertEquals("message", doc.getNodes().get(0).getName());
        assertEquals("message", doc.getFirstNode().getName());

        assertEquals(6, doc.getAllNodes().size());

        Node node = doc.getFirstNodeByName("body");
        assertEquals("2", node.getAttribute("attr"));

        //	System.out.println("[" + node.contentsToString() + "]");

        assertEquals(contents, node.contentsToString().trim());
    }


    //TODO CDATA, comment, directives
}
