/*
 * Copyright 2013 the original author or authors.
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
package org.openehealth.ipf.modules.cda;

import org.eclipse.mdht.uml.cda.CDAPackage;

public final class CDAR2Utils {

    private static final CDAPackage CDA_PACKAGE = CDAPackage.eINSTANCE;

    private CDAR2Utils() {
    }

    public static CDAPackage init() {
        return CDA_PACKAGE;
    }

    /**
     * @deprecated use {@link #init()}.
     */
    @Deprecated
    public static CDAPackage initCCD() {
        return CDA_PACKAGE;
    }

    /**
     * @deprecated use {@link #init()}.
     */
    @Deprecated
    public static CDAPackage initHITSPC32() {
        return CDA_PACKAGE;
    }

    /**
     * @deprecated use {@link #init()}.
     */
    @Deprecated
    public static CDAPackage initConsolCDA() {
        return CDA_PACKAGE;
    }

    /**
     * @deprecated use {@link #init()}.
     */
    @Deprecated
    public static CDAPackage initIHEPCC() {
        return CDA_PACKAGE;
    }

}
