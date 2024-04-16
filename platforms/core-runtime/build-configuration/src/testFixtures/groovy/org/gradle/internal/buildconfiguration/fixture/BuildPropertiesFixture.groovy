/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.buildconfiguration.fixture

import groovy.transform.SelfType
import org.gradle.api.JavaVersion
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.AvailableJavaHomes
import org.gradle.internal.buildconfiguration.BuildPropertiesDefaults
import org.gradle.internal.jvm.Jvm
import org.gradle.test.fixtures.file.TestFile

@SelfType(AbstractIntegrationSpec)
trait BuildPropertiesFixture {
    void assertDaemonUsedJvm(Jvm expectedJvm) {
        assert file("javaHome.txt").text == expectedJvm.javaHome.canonicalPath
    }

    void captureJavaHome() {
        buildFile << """
            def javaHome = org.gradle.internal.jvm.Jvm.current().javaHome.canonicalPath
            println javaHome
            file("javaHome.txt").text = javaHome
        """
    }

    TestFile getBuildPropertiesFile() {
        return file(BuildPropertiesDefaults.BUILD_PROPERTIES_FILE)
    }

    void assertJvmCriteria(JavaVersion version, String vendor = null, String implementation = null) {
        Map<String, String> properties = buildPropertiesFile.properties
        assert properties.get(BuildPropertiesDefaults.TOOLCHAIN_VERSION_PROPERTY) == version.majorVersion
        assert properties.get(BuildPropertiesDefaults.TOOLCHAIN_VENDOR_PROPERTY) == vendor
        assert properties.get(BuildPropertiesDefaults.TOOLCHAIN_IMPLEMENTATION_PROPERTY) == implementation
    }

    void writeJvmCriteria(Jvm jvm) {
        def otherMetadata = AvailableJavaHomes.getJvmInstallationMetadata(jvm)
        writeJvmCriteria(jvm.javaVersion, otherMetadata.vendor.knownVendor.name())
    }

    void writeJvmCriteria(JavaVersion version, String vendor = null, String implementation = null) {
        Properties properties = new Properties()
        properties.put(BuildPropertiesDefaults.TOOLCHAIN_VERSION_PROPERTY, version.majorVersion)
        if (vendor) {
            properties.put(BuildPropertiesDefaults.TOOLCHAIN_VENDOR_PROPERTY, vendor)
        }
        if (implementation) {
            properties.put(BuildPropertiesDefaults.TOOLCHAIN_IMPLEMENTATION_PROPERTY, implementation)
        }
        buildPropertiesFile.writeProperties(properties)
        assertJvmCriteria(version, vendor, implementation)
    }
}
