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

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.framework.action.AbstractMultiItemAction;
import info.magnolia.ui.framework.action.OpenEditDialogActionDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.neatresults.mgnlextensions.googleplaces.action.OpenMultiEditDialogAction.Definition;
import com.neatresults.mgnlextensions.googleplaces.presenter.MultiFormDialogPresenter;
import com.neatresults.mgnlextensions.googleplaces.presenter.MultiFormDialogPresenterFactory;

/**
 * Opens a dialog for editing multiple nodes.
 */
public class OpenMultiEditDialogAction extends AbstractMultiItemAction<Definition> {

    private final MultiFormDialogPresenterFactory formDialogPresenterFactory;
    private final UiContext uiContext;
    private final EventBus eventBus;
    private final SimpleTranslator i18n;
    private final ContentConnector contentConnector;

    @Inject
    public OpenMultiEditDialogAction(Definition definition, List<JcrItemAdapter> items, UiContext uiContext, MultiFormDialogPresenterFactory formDialogPresenterFactory, @Named(AdmincentralEventBus.NAME) final EventBus eventBus, SimpleTranslator i18n,
            ContentConnector contentConnector) {
        super(definition, items, uiContext);
        this.formDialogPresenterFactory = formDialogPresenterFactory;
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        this.contentConnector = contentConnector;
        this.i18n = i18n;
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (getItems().size() == 0) {
            return;
        }
        final String dialogName = getDefinition().getDialogName();
        if (StringUtils.isBlank(dialogName)) {
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, i18n.translate("ui-framework.actions.no.dialog.definition", getDefinition().getName()));
            return;

        }

        final MultiFormDialogPresenter formDialogPresenter = formDialogPresenterFactory.createFormDialogPresenter(dialogName);
        if (formDialogPresenter == null) {
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, i18n.translate("ui-framework.actions.dialog.not.registered", dialogName));
            return;
        }
        formDialogPresenter.start(getItems(), getDefinition().getDialogName(), uiContext, new EditorCallback() {

            @Override
            public void onSuccess(String actionName) {
                for (JcrItemAdapter item : getItems()) {
                    eventBus.fireEvent(new ContentChangedEvent(contentConnector.getItemId(item)));
                }
                formDialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        });
    }

    @Override
    protected void executeOnItem(JcrItemAdapter item) throws Exception {
        // nothing do to here
    }

    @Override
    protected String getSuccessMessage() {
        return getDefinition().getSuccessMessage();
    }

    @Override
    protected String getFailureMessage() {
        return getDefinition().getFailureMessage();
    }

    /**
     * Definition for the outer class.
     */
    public static class Definition extends OpenEditDialogActionDefinition {
        public Definition() {
            setImplementationClass(OpenMultiEditDialogAction.class);
        }
    }
}
