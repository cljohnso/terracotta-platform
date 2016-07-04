/*
 * Copyright Terracotta, Inc.
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
package org.terracotta.voltron.management.common.providers;

import org.terracotta.management.call.Parameter;
import org.terracotta.management.context.Context;

/**
 * Call action provider interface. This is the required interface for the <i>management service</i>
 * to support management action(s) that can be invoked by a management entity (For example,
 * {@code clearCache}).
 * <p>
 * Managed entities must implement this interface if they have any management action that can
 * be invoked by a management entity to control the state of a managed object.
 * <p>
 * Entities that do not support any managed action for any of its managed object types can
 * safely ignore this interface.
 *
 * @param <O> the managed object
 *           
 * @author RKAV
 */
public interface ActionProvider<O> extends ManagementProvider<O> {
  /**
   * An action provider MUST implement the callAction interface for all the management action(s) that
   * it supports.
   * <p>
   * The {@code comtext} parameter has sufficient information to identify the managed object instance
   * on which the action must be taken.
   *
   * @param context the context.
   * @param methodName the method name.
   * @param parameters the action method's parameters (objects and class names).
   * @param returnType The expected return type
   * @return the object instance on which the action was called, based on the context information.
   */
  O callAction(Context context, String methodName, Class<O> returnType, Parameter... parameters);
}
