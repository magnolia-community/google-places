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
package com.neatresults.mgnlextensions.googleplaces.action;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import com.vaadin.data.Item;

/**
 * Save action for place.
 */
public class SavePlaceDialogAction extends SaveDialogAction<SaveDialogActionDefinition> {

    public SavePlaceDialogAction(SaveDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback) {
        super(definition, item, validator, callback);
    }

    @Override
    protected void setNodeName(Node node, JcrNodeAdapter item) throws RepositoryException {
        String propertyName = "place";
        if (node.hasProperty(propertyName) && !node.hasProperty(ModelConstants.JCR_NAME)) {
            Property property = node.getProperty(propertyName);
            String newNodeName = Path.getUniqueLabel(node.getSession(), node.getParent().getPath(), property.getString());
            if (!node.getName().equals(Path.getValidatedLabel(newNodeName))) {
                newNodeName = Path.getUniqueLabel(node.getSession(), node.getParent().getPath(), Path.getValidatedLabel(newNodeName));
                item.setNodeName(newNodeName);
                NodeUtil.renameNode(node, newNodeName);
            }
        }
    }
}
