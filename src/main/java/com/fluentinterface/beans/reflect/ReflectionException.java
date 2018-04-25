/*
 * Copyright 2015 Fabio Piro (minimalcode.org).
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
package com.fluentinterface.beans.reflect;

/**
 * <p> Reflect exception is thrown by the {@link Property} to indicate
 * that a requested reflection action is denied.
 *
 * @author Fabio Piro
 * @see Property#get(Object)
 * @see Property#set(Object, Object)
 **/
public class ReflectionException extends RuntimeException {

    /**
     * Constructs an {@link ReflectionException} with the
     * specified, detailed message and a throwable cause.
     *
     * @param message the detail message
     * @param cause   the throwable cause message
     */
    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@link ReflectionException} with the
     * specified, detailed message.
     *
     * @param message the detail message
     */
    public ReflectionException(String message) {
        super(message);
    }
}
