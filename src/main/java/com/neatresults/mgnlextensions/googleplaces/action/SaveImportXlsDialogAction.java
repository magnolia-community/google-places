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

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnlextensions.googleplaces.action.SaveImportXlsDialogAction.Definition;
import com.vaadin.data.Item;

/**
 * Call Import Command in order to perform the import action.
 */
public class SaveImportXlsDialogAction extends AbstractAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(SaveImportXlsDialogAction.class);

    private final Item item;
    private CommandsManager commandsManager;
    private EditorValidator validator;
    private EditorCallback callback;
    private Map<String, Object> params;

    @Inject
    public SaveImportXlsDialogAction(Definition definition, final Item item, final CommandsManager commandsManager, final EditorValidator validator, final EditorCallback callback) {
        super(definition);
        this.item = item;
        this.commandsManager = commandsManager;
        this.validator = validator;
        this.callback = callback;
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {
            final JcrNodeAdapter itemChanged = (JcrNodeAdapter) item;
            JcrNodeAdapter importXml = (JcrNodeAdapter) itemChanged.getChild("import");
            if (importXml != null) {
                executeCommand(itemChanged);
            }
            callback.onSuccess(getDefinition().getName());
        } else {
            log.info("Validation error(s) occurred. No Import performed.");
        }

    }

    public final void executeCommand(JcrNodeAdapter itemChanged) throws ActionExecutionException {

        final String commandName = getDefinition().getCommand();
        final String catalog = getDefinition().getCatalog();
        final Command command = this.commandsManager.getCommand(catalog, commandName);

        if (command == null) {
            throw new ActionExecutionException(String.format("Could not find command [%s] in any catalog", commandName));
        }

        long start = System.currentTimeMillis();
        try {
            // Set the parameter used by the command.
            setParams(itemChanged);
            log.debug("Executing command [{}] from catalog [{}] with the following parameters [{}]...", commandName, catalog, params);
            commandsManager.executeCommand(command, params);
            log.debug("Command executed successfully in {} ms ", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.debug("Command execution failed after {} ms ", System.currentTimeMillis() - start);
            throw new ActionExecutionException(e);
        }
    }

    private void setParams(JcrNodeAdapter itemChanged) throws RepositoryException {
        params = new HashMap<String, Object>();
        JcrNodeAdapter importXml = (JcrNodeAdapter) itemChanged.getChild("import");
        params.put("xlsStream", ((DefaultProperty<BinaryImpl>) importXml.getItemProperty(JcrConstants.JCR_DATA)).getValue().getStream());
        params.put("xlsFileName", importXml.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue());
        params.put("startRow", getDefinition().getStartRow());
        params.put("endRow", getDefinition().getEndRow());
        params.put("repository", itemChanged.getWorkspace());
        params.put("path", itemChanged.getJcrItem().getPath());
        Map<String, String> mappings = new HashMap<String, String>();
        for (Map.Entry<String, AbstractJcrNodeAdapter> mapping : itemChanged.getChild("mappings").getChildren().entrySet()) {
            AbstractJcrNodeAdapter node = mapping.getValue();
            mappings.put(String.valueOf(node.getItemProperty("columnNumber").getValue()), String.valueOf(node.getItemProperty("fieldName").getValue()));
        }
        if (mappings.isEmpty()) {
            params.put("columnToPropertyMapping", getDefinition().getColumnToPropertyMapping());
        } else {
            params.put("columnToPropertyMapping", mappings);
        }
        params.put("startRow", ((Boolean) itemChanged.getItemProperty("skipZeroRow").getValue()) ? 1 : 0);

        Object val = itemChanged.getItemProperty("categories").getValue();
        if (val instanceof String) {
            params.put("categories", new String[] { (String) val });
        } else if (val != null) {
            params.put("categories", ((Collection) val).toArray(new String[] {}));
        }
    }

    /**
     * Definition for the import action. Defaults to google-places/importXls command if no command name is provided.
     */
    public static class Definition extends CommandActionDefinition {

        private static final String COMMAND_NAME = "importXls";
        private static final String CATALOG_NAME = "google-places";

        private int startRow = 0;
        private int endRow;
        private Map<String, String> columnToPropertyMapping = new HashMap<String, String>();

        public Definition() {
            setImplementationClass(SaveImportXlsDialogAction.class);
            setCatalog(CATALOG_NAME);
            setCommand(COMMAND_NAME);
        }

        public Map<String, String> getColumnToPropertyMapping() {
            return columnToPropertyMapping;
        }

        public void setColumnToPropertyMapping(Map<String, String> columnToPropertyMapping) {
            this.columnToPropertyMapping = columnToPropertyMapping;
        }

        public int getStartRow() {
            return startRow;
        }

        public void setStartRow(int startRow) {
            this.startRow = startRow;
        }

        public int getEndRow() {
            return endRow;
        }

        public void setEndRow(int endRow) {
            this.endRow = endRow;
        }
    }

}
