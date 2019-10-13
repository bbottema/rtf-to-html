/*
 * Copyright (C) ${project.inceptionYear} Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bbottema.rtftohtml.impl;

import java.io.InputStream;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class TestUtils {
    public static String classpathFileToString(String classPathFile) {
        InputStream resourceAsStream = RTF2HTMLConverterClassic.class.getClassLoader().getResourceAsStream(classPathFile);
        return new Scanner(requireNonNull(resourceAsStream), UTF_8.name()).useDelimiter("\\A").next();
    }

    public static String normalizeText(String text) {
        return text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
    }
}
