package ru.cft;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class XMLConverter {

    private static List<Map<String, String>> resultList = new ArrayList<>();

    XMLConverter(Document document) throws IOException {

        Path path = Paths.get("result");
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        Element element = document.getDocumentElement();
        NodeList nodeList = element.getChildNodes();
        transform(nodeList);
    }

    private static void transform(NodeList nodeList) {

        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                Element element = (Element) nodeList.item(i);
                if (element.getTagName().equals("Field")) {
                    Map<String, String> map = new HashMap<>();
                    NodeList elementNodeList = element.getChildNodes();
                    for (int j = 0; j < elementNodeList.getLength(); j++) {
                        if (!elementNodeList.item(j).getNodeName().equals("#text")) {
                            map.put(elementNodeList.item(j).getNodeName(), elementNodeList.item(j).getTextContent());
                        }
                    }
                    if (map.containsKey("type")) {
                        resultList.add(map);
                    }
                }
                if (element.hasChildNodes() && !element.getTagName().equals("Field")) {
                    transform(nodeList.item(i).getChildNodes());
                }
            }
        }
    }

    Document getDocument() throws ParserConfigurationException, TransformerException,
            FileNotFoundException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element data = document.createElement("Data");

        resultList.forEach(result -> {
            Element type = document.createElement(result.get("type"));
            result.keySet().stream().filter(x -> !x.equals("type")).forEach(x -> {
                if (x.equals("digitOnly") || x.equals("readOnly") || x.equals("required")) {
                    type.setAttribute(x, String.valueOf(result.get(x).equals("1")));
                } else {
                    if (result.get("type").equals("Address")) {
                        if (!x.equals("value")) {
                            type.setAttribute(x, result.get(x));
                        } else {
                            String value = result.get("value");
                            String[] array = value.split(",");
                            if (array.length < 3) {
                                type.setAttribute(x, result.get(x));
                            } else {
                                type.setAttribute("street", array[0]);
                                type.setAttribute("house", array[1]);
                                type.setAttribute("flat", array[2]);
                            }
                        }
                    } else {
                        type.setAttribute(x, result.get(x));
                    }
                }
            });
            data.appendChild(type);
        });

        document.appendChild(data);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream("result/dst.xml")));

        return document;
    }
}
