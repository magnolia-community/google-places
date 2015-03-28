/*
 * Licensed to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neatresults.mgnlextensions.googleplaces;

import info.magnolia.jcr.util.NodeTypes;

/**
 * This class is optional and represents the configuration for the google-places-app module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/google-places-app</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
public class GooglePlacesModule {

    public static final String PLACE_NODE_TYPE = NodeTypes.MGNL_PREFIX + "google-place";

}
