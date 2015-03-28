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
package com.neatresults.mgnlextensions.googleplaces.presenter;

import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.List;

import com.vaadin.data.Item;

/**
 * Multi item editing form presenter.
 */
public interface MultiFormDialogPresenter extends FormDialogPresenter {

    /**
     * Starts (builds and renders) a form dialog component.
     *
     */
    DialogView start(List<? extends Item> item, FormDialogDefinition dialogDefinition, UiContext uiContext, EditorCallback callback);

    /**
     * Starts (builds and renders) a form dialog component.
     *
     */
    DialogView start(List<? extends Item> item, String dialogId, UiContext uiContext, EditorCallback callback);

    /**
     * Starts (builds and renders) a form dialog component. {@link ContentConnector} parameter allows for specifying concrete connector to be used within the dialog.
     *
     */
    DialogView start(List<? extends Item> item, FormDialogDefinition dialogDefinition, UiContext uiContext, EditorCallback callback, ContentConnector contentConnector);
}
