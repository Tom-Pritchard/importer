/* Copyright 2010-2019 Norconex Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.norconex.importer.handler.tagger.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.TestUtil;
import com.norconex.importer.doc.ImporterMetadata;
import com.norconex.importer.handler.ImporterHandlerException;

public class TextBetweenTaggerTest {

    @Test
    public void testExtractMatchingRegex()
            throws IOException, ImporterHandlerException {
        // use it in a way that one of the end point is all we want to match
        TextBetweenTagger t = new TextBetweenTagger();
        t.addTextEndpoints("field", "http://www\\..*?02a\\.gif", "\\b");
        t.setInclusive(true);
        File htmlFile = TestUtil.getAliceHtmlFile();
        InputStream is = new BufferedInputStream(new FileInputStream(htmlFile));

        ImporterMetadata metadata = new ImporterMetadata();
        metadata.set(ImporterMetadata.DOC_CONTENT_TYPE, "text/html");
        t.tagDocument(htmlFile.getAbsolutePath(), is, metadata, false);

        is.close();

        String field = metadata.getString("field");

        Assertions.assertEquals("http://www.cs.cmu.edu/%7Ergs/alice02a.gif", field);
    }

    @Test
    public void testTagTextDocument()
            throws IOException, ImporterHandlerException {
        TextBetweenTagger t = new TextBetweenTagger();
        t.addTextEndpoints("headings", "<h1>", "</H1>");
        t.addTextEndpoints("headings", "<h2>", "</H2>");
        t.addTextEndpoints("strong", "<b>", "</B>");
        t.addTextEndpoints("strong", "<i>", "</I>");
        t.setCaseSensitive(false);
        t.setInclusive(true);
        File htmlFile = TestUtil.getAliceHtmlFile();
        InputStream is = new BufferedInputStream(new FileInputStream(htmlFile));

        ImporterMetadata metadata = new ImporterMetadata();
        metadata.set(ImporterMetadata.DOC_CONTENT_TYPE, "text/html");
        t.tagDocument(htmlFile.getAbsolutePath(), is, metadata, false);

        is.close();

        List<String> headings = metadata.getStrings("headings");
        List<String> strong = metadata.getStrings("strong");

        Assertions.assertTrue(
                headings.contains("<h2>Down the Rabbit-Hole</h2>"),
                "Failed to return: <h2>Down the Rabbit-Hole</h2>");
        Assertions.assertTrue(headings.contains("<h2>CHAPTER I</h2>"),
                "Failed to return: <h2>CHAPTER I</h2>");
        Assertions.assertTrue(strong.size() == 17,
                "Should have returned 17 <i> and <b> pairs");
    }

    @Test
    public void testExtractFirst100ContentChars()
            throws IOException, ImporterHandlerException {
        TextBetweenTagger t = new TextBetweenTagger();
        t.addTextEndpoints("mytitle", "^", ".{0,100}");
        t.setCaseSensitive(false);
        t.setInclusive(true);
        File htmlFile = TestUtil.getAliceHtmlFile();
        InputStream is = new BufferedInputStream(new FileInputStream(htmlFile));

        ImporterMetadata metadata = new ImporterMetadata();
        metadata.set(ImporterMetadata.DOC_CONTENT_TYPE, "text/html");
        t.tagDocument(htmlFile.getAbsolutePath(), is, metadata, false);

        is.close();

        String myTitle = metadata.getString("mytitle");
        Assertions.assertEquals(100, myTitle.length());
    }

    @Test
    public void testWriteRead() throws IOException {
        TextBetweenTagger tagger = new TextBetweenTagger();
        tagger.addTextEndpoints("headings", "<h1>", "</h1>");
        tagger.addTextEndpoints("headings", "<h2>", "</h2>");
        tagger.setMaxReadSize(512);
        XML.assertWriteRead(tagger, "handler");
    }
}
