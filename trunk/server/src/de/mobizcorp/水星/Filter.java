/*
 * 水星 - Water Star.
 * Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.mobizcorp.水星;

import java.util.ArrayList;

import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Filter interface, for implementors of the ignore file.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public interface Filter {
    public static class Cascade implements Filter {
        private final ArrayList<Filter> delegates = new ArrayList<Filter>();

        public boolean accept(final String path) {
            for (final Filter filter : delegates) {
                if (filter.accept(path)) {
                    return true;
                }
            }
            return false;
        }

        public void add(final Filter filter) {
            delegates.add(filter);
        }
    }

    public static class Glob implements Filter {
        private static final PatternCompiler COMPILER = new GlobCompiler();

        private static final PatternMatcher MATCHER = new Perl5Matcher();

        private final Pattern pattern;

        public Glob(final String pattern) throws MalformedPatternException {
            synchronized (COMPILER) {
                this.pattern = COMPILER.compile(pattern);
            }
        }

        public boolean accept(String path) {
            synchronized (MATCHER) {
                return MATCHER.matches(path, pattern);
            }
        }

    }

    public static class Regexp implements Filter {
        private static final PatternCompiler COMPILER = new Perl5Compiler();

        private static final PatternMatcher MATCHER = new Perl5Matcher();

        private final Pattern pattern;

        public Regexp(final String pattern) throws MalformedPatternException {
            synchronized (COMPILER) {
                this.pattern = COMPILER.compile(pattern);
            }
        }

        public boolean accept(String path) {
            synchronized (MATCHER) {
                return MATCHER.matches(path, pattern);
            }
        }

    }
    
    public static class Inverted implements Filter {
        private final Filter delegate;

        public Inverted(final Filter delegate) {
            this.delegate = delegate;
            
        }

        public boolean accept(final String path) {
            return !delegate.accept(path);
        }
    }

    public boolean accept(String path);
}
