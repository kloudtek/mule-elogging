package org.mule.extension;

import org.junit.Test;
import org.mule.extension.internal.EloggerOperations;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shapeless.DataT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EloggerOperationsTestCase
//        extends MuleArtifactFunctionalTestCase
{

    // Mule unit tests can't be used due to classpath dependencies conflict between standard spring classes and the
    // fact that mule-test-runner includes those same spring files. It typically doesn't cause problem but the second
    // logj2 dependency is added it breaks with error java.lang.NoSuchMethodError: org.springframework.expression.spel.SpelParserConfiguration.<init>(Lorg/springframework/expression/spel/SpelCompilerMode;Ljava/lang/ClassLoader;)V


//    /**
//     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
//     */
//    @Override
//    protected String getConfigFile() {
//        return "test-mule-config.xml";
//    }
//
//    @Test
//    public void testLog() throws Exception {
//        String payloadValue = ((String) flowRunner("testLog").withPayload("{\"foo\":\"bar\"}").withMediaType(MediaType.APPLICATION_JSON)
//                .withAttributes(new CustomAttribute())
//                .run()
//                .getMessage()
//                .getPayload()
//                .getValue());
//        assertThat(payloadValue, is("{\"foo\":\"bar\"}"));
//    }
}
