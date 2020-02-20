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

package de.gematik.ti.healthcard.control.protocol;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get first <code>TEST_PORT</code> from the configuration of g2-cardsim <br/>
 * socket opens in constructor <br/>
 * socket closes after save-action. <br/>
 * File will be overwritten if exists <br/>
 * Default save-dir is "java.io.tmpdir" <br/>
 * Directory and Imagename could be explicit determined
  */
public class SaveImageCommand {

    private static final Logger LOG = LoggerFactory.getLogger(SaveImageCommand.class);
    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 12345;
    private final String host;
    private final int port;
    private String dirName;
    private String imageName = "newImage.xml";

    /**
     * open socket to "localhost" on port 12345
     */
    public SaveImageCommand() {
        this(TEST_HOST, TEST_PORT);
    }

    /**
     * open socket to host on testPort
     * @param host
     * @param testPort
     */
    public SaveImageCommand(String host, int testPort) {
        this.host = host;
        this.port = testPort;
        dirName = System.getProperty("java.io.tmpdir");
    }

    /**
     * saveCardImage
     * save to: dirName + imageName
     * @param dirName
     * @param imageName
     */
    public void requestSaveCardImage(String dirName, String imageName) {
        this.dirName = dirName;
        if (!this.dirName.endsWith("\\") && !this.dirName.endsWith("/")) {
            this.dirName = this.dirName + "/";
        }
        File tmpDir = new File(dirName);
        if (!tmpDir.exists() || !tmpDir.isDirectory()) {
            LOG.error("saveCardImage failed. " + dirName + " is not a Directory or not existed");
            return;
        }
        this.imageName = imageName;
        if (!this.imageName.toLowerCase().endsWith(".xml")) {
            this.imageName = this.dirName + ".xml";
        }
        requestSaveCardImage();
    }

    /**
     * send the xml to g2-cardsim
     */
    public String requestSaveCardImage() {

        try (Socket clientSocket = new Socket(host, port);
                OutputStream outputStream = clientSocket.getOutputStream();
                InputStream inputStream = clientSocket.getInputStream()) {
            outputStream.write(
                    ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                            "<testsystemcommand>" +
                            "<name>saveCardImage</name>" +
                            "<parameters>" +
                            "<param><name>imagePath</name><value>" + dirName + imageName + "</value></param>" +
                            "</parameters></testsystemcommand>")
                                    .getBytes("UTF-8"));
            outputStream.flush();

            LOG.debug("inputStream) " + inputStream);
            LOG.debug("inputStream.available " + inputStream.available());

            byte[] bytes = new byte[1024];
            inputStream.read(bytes);
            LOG.debug("inputStream.response " + new String(bytes));
            LOG.info("Image is saved to " + dirName + imageName);
            return dirName + imageName;
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

}
