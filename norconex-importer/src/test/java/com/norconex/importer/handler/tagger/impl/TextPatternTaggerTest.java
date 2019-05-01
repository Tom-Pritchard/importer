/* Copyright 2015-2019 Norconex Inc.
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

import com.norconex.commons.lang.text.RegexKeyValueExtractor;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.TestUtil;
import com.norconex.importer.doc.ImporterMetadata;
import com.norconex.importer.handler.ImporterHandlerException;

/**
 * @author Pascal Essiembre
 * @since 2.3.0
 */
public class TextPatternTaggerTest {

    @Test
    public void testTagTextDocument()
            throws IOException, ImporterHandlerException {
        TextPatternTagger t = new TextPatternTagger();
        t.addPattern("headings", "<h2>(.*?)</h2>" , 1);
        t.addPattern("country", "\\w+\\sZealand");
        File htmlFile = TestUtil.getAliceHtmlFile();
        InputStream is = new BufferedInputStream(new FileInputStream(htmlFile));

        ImporterMetadata metadata = new ImporterMetadata();
        metadata.set(ImporterMetadata.DOC_CONTENT_TYPE, "text/html");
        t.tagDocument(htmlFile.getAbsolutePath(), is, metadata, false);

        is.close();

        List<String> headings = metadata.getStrings("headings");
        List<String> countries = metadata.getStrings("country");

        Assertions.assertEquals(2, headings.size(), "Wrong <h2> count.");
        Assertions.assertEquals("CHAPTER I", headings.get(0),
                "Did not extract first heading");
        Assertions.assertEquals("Down the Rabbit-Hole", headings.get(1),
                "Did not extract second heading");

        Assertions.assertEquals(1, countries.size(), "Wrong country count.");
        Assertions.assertEquals("New Zealand", countries.get(0),
                "Did not extract country");
    }

    @Test
    public void testExtractFirst100ContentChars()
            throws IOException, ImporterHandlerException {
        TextPatternTagger t = new TextPatternTagger();
        t.addPattern("mytitle", "^.{0,100}");
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
        TextPatternTagger tagger = new TextPatternTagger();
        tagger.addPattern("field1", "123.*890");
        tagger.addPattern("field2", "abc.*xyz", 3);
        tagger.addPattern(new RegexKeyValueExtractor("blah")
                .setCaseSensitive(true)
                .setKey("field3")
                .setKeyGroup(3)
                .setValueGroup(6));
        tagger.setMaxReadSize(512);
        XML.assertWriteRead(tagger, "handler");
    }
}
