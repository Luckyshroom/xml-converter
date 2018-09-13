package ru.cft;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException,
            TransformerException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document src = builder.parse(new File("src.xml"));

        XMLConverter xmlConverter = new XMLConverter(src);

        Document dst = xmlConverter.getDocument();

        boolean flag = true;
        Scanner scanner = new Scanner(System.in);

        while (flag) {
            System.out.println("Enter URL: ");
            String input = scanner.nextLine();
            try {
                URL url = new URL(input);
                System.out.println(url.getPath());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/xml");
                connection.setDoOutput(true);

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(dst), new StreamResult(writer));

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8));
                outputStream.close();

                int code = connection.getResponseCode();
                System.out.println("Sending 'POST' request to URL : " + url.toString());
                System.out.println("Response code : " + code);
            } catch (Exception e) {
                if (e instanceof ConnectException) {
                    System.out.println("Connection refused!");
                } else {
                    System.out.println("Invalid input format!");
                }
            } finally {
                System.out.println("Try again? y/n");
                input = scanner.nextLine();
                flag = input.equals("y");
            }
        }
    }
}
