/*
 * Copyright 2021 the original author or authors.
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

package io.github.gradlenexus.publishplugin.internal

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.UncheckedIOException
import java.time.Duration
import kotlin.IllegalStateException

internal class BasicActionRetrierTest {

    @Test
    internal fun `test basic retry`() {
        val retrier = BasicActionRetrier<Boolean>(2, Duration.ofSeconds(1), Throwable::class.java) { it }

        var count = 0
        val result = retrier.execute {
            if (count < 2) {
                count++
                throw RuntimeException("error!")
            } else {
                true
            }
        }
        assertTrue(result)
    }

    @Test
    internal fun `test wont retry on exception that is not included in the scope of the supplied handle class`() {
        val retrier = BasicActionRetrier<Boolean>(2, Duration.ofSeconds(1), UncheckedIOException::class.java) { it }
        var count = 0
        assertThrows<IllegalStateException> {
            retrier.execute { if (count < 2) {
                count++
                throw IllegalStateException("illegal state!")
            } else {
                true
            }
            }
        }
    }

    @Test
    internal fun `test will retry on exception that is subclass of the supplied handle class`() {
        val retrier = BasicActionRetrier<Boolean>(2, Duration.ofSeconds(1), RuntimeException::class.java) { it }
        var count = 0
        val result = retrier.execute {
            if (count < 2) {
                count++
                throw IllegalStateException("illegal state!")
            } else {
                true
            }
        }
        assertTrue(result)
    }
}
