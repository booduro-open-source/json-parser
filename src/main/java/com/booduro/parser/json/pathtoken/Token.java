/*
 * Copyright 2018 BOODURO inc.
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
 *
 */

package com.booduro.parser.json.pathtoken;

import com.jayway.jsonpath.internal.Path;
import com.jayway.jsonpath.internal.path.CompiledPath;
import com.jayway.jsonpath.internal.path.PathCompiler;
import com.jayway.jsonpath.internal.path.PathToken;
import com.jayway.jsonpath.internal.path.PathTokenWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author saifeddine.benchahed
 */
public class Token {
    public final String literal;

    public Token(String literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return literal;
    }

    @Override
    public boolean equals(Object that) {
        return that.getClass().isInstance(this) && this.literal.equals(((Token) that).literal);
    }

    @Override
    public int hashCode() {
        return getClass().getSimpleName().hashCode();
    }

    public static Token from(PathToken jaywayPathToken) {
        PathTokenWrapper pathTokenWrapper = new PathTokenWrapper(jaywayPathToken);
        String pathFragment = pathTokenWrapper.getPathFragment();
        switch (jaywayPathToken.getClass().getSimpleName()) {
            case "RootPathToken":
                return new Root(pathFragment);
            case "WildcardPathToken":
                return new Wildcard(pathFragment.substring(1, pathTokenWrapper.getPathFragment().length() - 1));
            case "PropertyPathToken":
                return new Property(pathFragment.substring(2, pathTokenWrapper.getPathFragment().length() - 2));
            default:
                throw new IllegalArgumentException("Json path token " + pathFragment + "of type" +
                        jaywayPathToken.getClass().getSimpleName() + "is not supported");
        }
    }

    public static List<Token> fullPath(String jsonPath) {
        try {
            Path path = PathCompiler.compile(jsonPath);
            Field rootField = CompiledPath.class.getDeclaredField("root");
            rootField.setAccessible(true);
            List<Token> tokens = new ArrayList<>();
            PathToken current = (PathToken) rootField.get(path);
            while (current != null) {
                tokens.add(from(current));
                current = nextPathToken(current);
            }
            return tokens;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PathToken nextPathToken(PathToken token) {
        try {
            return new PathTokenWrapper(token).next();
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Current path token is a leaf")) {
                return null;
            } else {
                throw e;
            }
        }
    }
}
