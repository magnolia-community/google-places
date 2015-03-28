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

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.Dialog;
import info.magnolia.ui.dialog.DialogCloseHandler;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.actionarea.DialogActionExecutor;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.dialog.formdialog.FormView;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Item;

/**
 * Custom form presenter passing on multiple items to a dialog.
 */
public class MultiFormDialogPresenterImpl extends BaseDialogPresenter implements MultiFormDialogPresenter, EditorValidator {

    private FormView formView;
    private FormBuilder formBuilder;
    private DialogDefinitionRegistry dialogDefinitionRegistry;
    private EditorCallback callback;
    private List<? extends Item> items;

    @Inject
    public MultiFormDialogPresenterImpl(final DialogDefinitionRegistry dialogDefinitionRegistry, FormBuilder formBuilder, ComponentProvider componentProvider, DialogActionExecutor executor, FormView view, I18nizer i18nizer, SimpleTranslator i18n, AvailabilityChecker checker,
            ContentConnector contentConnector) {
        super(componentProvider, executor, view, i18nizer, i18n);
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.formBuilder = formBuilder;
        this.componentProvider = componentProvider;
        this.formView = view;
    }

    @Override
    public FormView getView() {
        return formView;
    }

    @Override
    public DialogView start(Item item, FormDialogDefinition dialogDefinition, UiContext uiContext, EditorCallback callback) {
        throw new UnsupportedOperationException("Don't use multi form presenter for single items.");
    }

    @Override
    public DialogView start(Item item, String dialogId, UiContext uiContext, EditorCallback callback) {
        throw new UnsupportedOperationException("Don't use multi form presenter for single items.");
    }

    @Override
    public DialogView start(Item item, FormDialogDefinition dialogDefinition, UiContext uiContext, EditorCallback callback, ContentConnector contentConnector) {
        throw new UnsupportedOperationException("Don't use multi form presenter for single items.");
    }


    @Override
    public DialogView start(List<? extends Item> items, FormDialogDefinition dialogDefinition, UiContext uiContext, EditorCallback callback) {
        this.callback = callback;
        this.items = items;

        start(dialogDefinition, uiContext);
        getExecutor().setDialogDefinition(getDefinition());
        buildView(getDefinition());

        final OverlayCloser overlayCloser = uiContext.openOverlay(getView(), getView().getModalityLevel());
        getView().addDialogCloseHandler(new DialogCloseHandler() {
            @Override
            public void onDialogClose(DialogView dialogView) {
                overlayCloser.close();
            }
        });
        getView().setClosable(true);
        return getView();
    }

    @Override
    public DialogView start(List<? extends Item> items, String dialogId, UiContext uiContext, EditorCallback callback) {
        try {
            FormDialogDefinition dialogDefinition = dialogDefinitionRegistry.getDialogDefinition(dialogId);
            return start(items, dialogDefinition, uiContext, callback);
        } catch (RegistrationException e) {
            throw new RuntimeException("No dialogDefinition found for " + dialogId, e);
        }
    }

    @Override
    public DialogView start(List<? extends Item> items, FormDialogDefinition dialogDefinition, UiContext uiContext, EditorCallback callback, ContentConnector contentConnector) {
        return start(items, dialogDefinition, uiContext, callback);
    }

    @Override
    protected DialogActionExecutor getExecutor() {
        return (DialogActionExecutor) super.getExecutor();
    }

    private void buildView(FormDialogDefinition dialogDefinition) {
        final Dialog dialog = new Dialog(dialogDefinition);

        // shall we pass whole list of items as PropertysetItem?
        formBuilder.buildForm(getView(), dialogDefinition.getForm(), items.get(0), dialog);

        final String description = dialogDefinition.getDescription();
        final String label = dialogDefinition.getLabel();

        if (StringUtils.isNotBlank(description) && !isMessageKey(description)) {
            getView().setDescription(description);
        }

        if (StringUtils.isNotBlank(label) && !isMessageKey(label)) {
            getView().setCaption(label);
        }
    }

    @Override
    protected FormDialogDefinition getDefinition() {
        return (FormDialogDefinition) super.getDefinition();
    }

    /**
     * @deprecated is a hack and should not be used. See MGNLUI-2207.
     */
    @Deprecated
    private boolean isMessageKey(final String text) {
        return !text.contains(" ") && text.contains(".") && !text.endsWith(".");
    }

    @Override
    protected Object[] getActionParameters(String actionName) {
        // and for this one method we had to create presenter factory, presenter, their impls and action to call them
        return new Object[] { this, items, callback };
    }

    @Override
    public void showValidation(boolean visible) {
        getView().showValidation(visible);
    }

    @Override
    public boolean isValid() {
        return getView().isValid();
    }

}
