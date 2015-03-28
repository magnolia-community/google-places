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

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.framework.action.AbstractMultiItemAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnlextensions.googleplaces.action.AddCategoryFormAction.Definition;

/**
 * Action to save newly created blog and to create appropriate blog page in website.
 */
public class AddCategoryFormAction extends AbstractMultiItemAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(AddCategoryFormAction.class);

    protected EditorCallback callback;
    protected final EditorValidator validator;

    private String setId;

    @Inject
    public AddCategoryFormAction(Definition definition, List<JcrItemAdapter> items, UiContext uiContext, EditorCallback callback, EditorValidator validator) {
        super(definition, items, uiContext);
        this.callback = callback;
        this.validator = validator;
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (getItems().size() == 0) {
            return;
        }
        if (validator.isValid()) {
            try {
                JcrItemAdapter item = getItems().get(0);
                Session placesSession = MgnlContext.getJCRSession("googlePlaces");
                setId = (String) item.getItemProperty("category").getValue();
                super.execute();
                placesSession.save();
            } catch (RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            callback.onSuccess(getDefinition().getName());
        }
    }

    @Override
    protected void executeOnItem(JcrItemAdapter item) throws ActionExecutionException {
        try {
            Node jcrItem = (Node) item.getJcrItem();
            if (jcrItem.hasProperty("categories")) {
                Value[] vals = jcrItem.getProperty("categories").getValues();
                List<String> stringVals = new ArrayList<String>();
                for (Value val : vals) {
                    if (setId.equals(val.getString())) {
                        // category is already assigned to this item
                        return;
                    }
                    stringVals.add(val.getString());
                }
                stringVals.add(setId);
                jcrItem.setProperty("categories", stringVals.toArray(new String[] {}));
            } else {
                jcrItem.setProperty("categories", new String[] { setId });
            }
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }

    @Override
    protected String getSuccessMessage() {
        return "";
    }

    @Override
    protected String getFailureMessage() {
        return "Unfortunately, it was not possible to add given category to selected places.";
    }
    /**
     * Generic Action Definition that needs to exist just to expose the related action.
     */
    public static class Definition extends ConfiguredActionDefinition {

        public Definition() {
            setImplementationClass(AddCategoryFormAction.class);
        }

    }

}

