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
package com.neatresults.mgnlextensions.googleplaces.column;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnlextensions.googleplaces.GooglePlacesModule;
import com.vaadin.ui.Table;

/**
 * Column formatter for places.
 */
public class PlaceNameColumn extends AbstractColumnFormatter<PlaceNameColumnDefinition> {

    private static final Logger log = LoggerFactory.getLogger(PlaceNameColumn.class);

    public PlaceNameColumn(PlaceNameColumnDefinition definition) {
        super(definition);
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        final Item jcrItem = getJcrItem(source, itemId);
        if (jcrItem != null && jcrItem.isNode()) {
            Node node = (Node) jcrItem;

            try {
                if (NodeUtil.isNodeType(node, NodeTypes.Folder.NAME)) {
                    return node.getName();
                }
            } catch (RepositoryException e) {
                log.warn("Unable to get name of folder for column", e);
            }

            try {
                if (NodeUtil.isNodeType(node, GooglePlacesModule.PLACE_NODE_TYPE)) {
                    // if the node has been mark as deleted, most of its properties has been removed
                    String result = null;
                    if (node.hasProperty("place")) {
                        result = node.getProperty("place").getString();
                    }
                    if (StringUtils.isNotBlank(result)) {
                        return result;
                    } else {
                        return node.getName();
                    }
                }
            } catch (RepositoryException e) {
                log.warn("Unable to get name of contact for column", e);
            }
        }
        return StringUtils.EMPTY;
    }
}

