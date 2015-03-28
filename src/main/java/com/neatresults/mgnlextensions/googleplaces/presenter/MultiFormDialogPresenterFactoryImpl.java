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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom form presenter passing on multiple items to a dialog.
 */
public class MultiFormDialogPresenterFactoryImpl implements MultiFormDialogPresenterFactory {

    private static final Logger log = LoggerFactory.getLogger(MultiFormDialogPresenterFactoryImpl.class);

    private DialogDefinitionRegistry registry;

    private ComponentProvider componentProvider;

    @Inject
    public MultiFormDialogPresenterFactoryImpl(DialogDefinitionRegistry registry, ComponentProvider componentProvider) {
        this.registry = registry;
        this.componentProvider = componentProvider;
    }

    @Override
    public MultiFormDialogPresenter createFormDialogPresenter(FormDialogDefinition definition) {
        return (MultiFormDialogPresenter) componentProvider.newInstance(definition.getPresenterClass());
    }

    @Override
    public MultiFormDialogPresenter createFormDialogPresenter(String dialogId) {
        try {
            return (MultiFormDialogPresenter) componentProvider.newInstance(registry.getPresenterClass(dialogId));
        } catch (RegistrationException e) {
            log.error("Failed to retrieve form dialog definition from registry: {}", e.getMessage(), e);
        }
        return null;
    }

}
