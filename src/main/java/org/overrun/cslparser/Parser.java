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

package org.overrun.cslparser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * The parser.
 * <h2>Split string</h2>
 * The parser {@linkplain #parse(String) splits} the given source string into an unmodifiable list.
 * <p>
 * The string is split with a delimiter, by default a whitespace character. This can be set with {@link #setDelimiter(IntPredicate)}.
 * <p>
 * A quoted string will not be split. An escape might be there in a quoted string or outside.
 *
 * @author squid233
 * @since 0.1.0
 */
public class Parser {
    private static final IntPredicate DEFAULT_DELIMITER = Character::isWhitespace;
    private IntPredicate delimiter;

    /**
     * Creates a parser with the {@link Character#isWhitespace(int) default delimiter}.
     *
     * @see #Parser(IntPredicate)
     */
    public Parser() {
        this(DEFAULT_DELIMITER);
    }

    /**
     * Creates a parser with the given delimiter.
     *
     * @param delimiter the delimiter.
     * @see #Parser()
     */
    public Parser(IntPredicate delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * The state of a parser.
     *
     * @author squid233
     * @since 0.1.0
     */
    private enum State {
        NONE,
        SINGLE_QUOTE,
        DOUBLE_QUOTE
    }

    /**
     * Parses the given source with {@linkplain #setDelimiter(IntPredicate) delimiter} of this parser into an unmodifiable list.
     *
     * @param src the source to be parsed.
     * @return the list that contains the split string.
     */
    @NotNull
    @UnmodifiableView
    public List<String> parse(String src) {
        int prevCp = 0;
        boolean escaping = false;
        State state = State.NONE;
        StringBuilder sb = new StringBuilder(32);
        final var list = new ArrayList<String>();

        for (int i = 0, len = src.codePointCount(0, src.length()); i < len; i++) {
            final int cp = src.codePointAt(i);
            if (state == State.NONE) {
                switch (cp) {
                    // enter single quote
                    case '\'' -> state = State.SINGLE_QUOTE;
                    // enter double quote
                    case '"' -> state = State.DOUBLE_QUOTE;
                    default -> {
                        if (delimiter.test(cp) && sb.length() > 0) {
                            list.add(sb.toString().translateEscapes());
                            sb = new StringBuilder(32);
                        } else {
                            sb.appendCodePoint(cp);
                        }
                    }
                }
            } else {
                // in quote
                final int quote = switch (state) {
                    case SINGLE_QUOTE -> '\'';
                    case DOUBLE_QUOTE -> '"';
                    default -> throw new IllegalStateException("Unexpected value: " + state);
                };
                // the other quote
                if (cp == quote) {
                    if (escaping) {
                        sb.appendCodePoint(cp);
                        escaping = false;
                    } else {
                        // exit quote
                        state = State.NONE;
                    }
                } else {
                    if (cp == '\\') {
                        escaping = prevCp != '\\';
                    } else {
                        if (prevCp == '\\') escaping = false;
                    }
                    sb.appendCodePoint(cp);
                }
            }
            prevCp = cp;
        }

        // end of string
        if (sb.length() > 0) {
            list.add(sb.toString().translateEscapes());
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * Sets the delimiter of this parser.
     *
     * @param delimiter the delimiter mapper. Use {@code null} to reset to the default delimiter.
     * @throws IllegalArgumentException if <i>{@code delimiter}</i> accepts a single quote or a double quote.
     */
    public void setDelimiter(IntPredicate delimiter) throws IllegalArgumentException {
        if (delimiter != null) {
            if (delimiter.test('\'') || delimiter.test('"')) {
                throw new IllegalArgumentException("delimiter can't be quote!");
            }
            this.delimiter = delimiter;
        } else {
            this.delimiter = DEFAULT_DELIMITER;
        }
    }
}
