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

import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;

/**
 * Presenter producer for multiple item editing.
 */
public interface MultiFormDialogPresenterFactory extends FormDialogPresenterFactory {

    @Override
    public MultiFormDialogPresenter createFormDialogPresenter(FormDialogDefinition definition);

    @Override
    public MultiFormDialogPresenter createFormDialogPresenter(String dialogId);
}
