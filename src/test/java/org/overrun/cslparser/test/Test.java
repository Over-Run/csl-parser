/*
 * MIT License
 *
 * Copyright (c) 2023 Overrun Organization
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package org.overrun.cslparser.test;

import org.overrun.cslparser.Parser;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Test {
    public static void main(String[] args) {
        final Parser parser = new Parser();
        final String text = """
            test a b 'single quote' "double quote" 'escaping \\' quote' "escaping \\" quote \\\\" \\\\
            line2
            line3 "double quote"
            rawJson '{"type":"json"}'
            """;
        System.out.println(parser.parse("java Main.java"));
        System.out.println(parser.parse(text));

        final Parser dot = new Parser();
        dot.setDelimiter(value -> value == '.');
        System.out.println(dot.parse("java Main.java"));
        System.out.println(dot.parse(text));
        System.out.println(dot.parse("System.out.println(\"Hello.world\");"));
    }
}
