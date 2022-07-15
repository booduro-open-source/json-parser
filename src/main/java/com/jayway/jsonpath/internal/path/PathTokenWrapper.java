/*
 * Copyright 2019 BOODURO inc.
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

package com.jayway.jsonpath.internal.path;

/**
 * @author saifeddine.benchahed
 */
public class PathTokenWrapper {
    private final PathToken pathToken;

    public PathTokenWrapper(PathToken pathToken) {
        this.pathToken = pathToken;
    }

    public String getPathFragment() {
        return pathToken.getPathFragment();
    }

    public PathToken next() {
        return pathToken.next();
    }
}
