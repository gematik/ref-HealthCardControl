/*
 * Copyright (c) 2020 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.ti.healthcard.control.entities;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class AbstractReadXmlsTest {
    protected Document readDocument(final String documentPath) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(documentPath);
        if (resourceAsStream == null) {
            resourceAsStream = getClass().getClassLoader().getResourceAsStream(documentPath.replace("resources", "test"));
        }
        Document document = builder.parse(resourceAsStream);
        Assert.assertNotNull(document);
        Assert.assertNotNull(document.getDocumentElement());
        return document;
    }
}
