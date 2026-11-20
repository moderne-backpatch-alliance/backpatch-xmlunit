/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package org.xmlunit.transform;

import javax.xml.transform.TransformerFactory;

import org.junit.Test;
import org.xmlunit.XMLUnitException;
import org.xmlunit.builder.Input;
import org.xmlunit.util.TransformerFactoryConfigurer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TransformationExtensionFunctionsTest {

    private static final String STYLESHEET =
        "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'"
        + " xmlns:math='http://xml.apache.org/xalan/java/java.lang.Math' exclude-result-prefixes='math'>"
        + "<xsl:template match='/'><r><xsl:value-of select='math:abs(-1)'/></r></xsl:template>"
        + "</xsl:stylesheet>";

    private static Transformation transformation() {
        Transformation t = new Transformation(Input.fromString("<a/>").build());
        t.setStylesheet(Input.fromString(STYLESHEET).build());
        return t;
    }

    // By name, so that this class still compiles when the fix is reverted for the control.
    private static TransformerFactoryConfigurer addedConfigurer(String name) throws Exception {
        return (TransformerFactoryConfigurer) TransformerFactoryConfigurer.class.getField(name).get(null);
    }

    private static void assertRefused(Transformation t) {
        try {
            String result = t.transformToString();
            fail("extension function was executed, result: " + result);
        } catch (XMLUnitException expected) {
            // the processor refused the stylesheet
        }
    }

    private static void assertAllowed(TransformerFactoryConfigurer configurer) {
        Transformation t = transformation();
        t.setFactory(configurer.configure(TransformerFactory.newInstance()));
        String result = t.transformToString();
        assertTrue(result, result.contains("<r>1</r>"));
    }

    @Test
    public void secureProcessingRefusesExtensionFunctions() throws Exception {
        Transformation t = transformation();
        t.setFactory(addedConfigurer("SecureProcessing").configure(TransformerFactory.newInstance()));
        assertRefused(t);
    }

    @Test
    public void noDtdButExtensionFunctionsAllowsThem() throws Exception {
        assertAllowed(addedConfigurer("NoDtdButExtensionFunctions"));
    }

    @Test
    public void noExternalAccessButExtensionFunctionsAllowsThem() throws Exception {
        assertAllowed(addedConfigurer("NoExternalAccessButExtensionFunctions"));
    }
}
