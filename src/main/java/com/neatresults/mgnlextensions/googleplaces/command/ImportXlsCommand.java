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
package com.neatresults.mgnlextensions.googleplaces.command;

import info.magnolia.cms.core.Path;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.value.ValueFactoryImpl;

import com.neatresults.mgnlextensions.googleplaces.ExcelReader;
import com.neatresults.mgnlextensions.googleplaces.GooglePlacesModule;

/**
 * Command to import content from excel file in places workspace.
 */
public class ImportXlsCommand extends BaseRepositoryCommand {

    private ExcelReader excelReader;
    private Map<String, String> columnToPropertyMapping = new HashMap<String, String>();
    private int startRow;
    private int endRow;
    private InputStream xlsStream;
    private String[] categories;

    @Override
    public boolean execute(Context context) throws Exception {
        excelReader = new ExcelReader(xlsStream, columnToPropertyMapping);
        List<Map<String, Object>> rows = excelReader.getRows(getStartRow(), getEndRow());
        for (Map<String, Object> row : rows) {
            createPlace(row);
        }
        return true;
    }

    protected void createPlace(Map<String, Object> values) throws RepositoryException {
        Session session = MgnlContext.getJCRSession("googlePlaces");
        Node root = session.getRootNode();
        String placeName = String.valueOf(values.get("place"));
        String name = Path.getValidatedLabel(placeName);
        Node place = root.addNode(Path.getUniqueLabel(session, root.getPath(), name), GooglePlacesModule.PLACE_NODE_TYPE);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                place.setProperty(entry.getKey(), PropertyUtil.createValue(value, ValueFactoryImpl.getInstance()));
            }
        }
        if (categories != null) {
            place.setProperty("categories", categories);
        }
        session.save();
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

    public InputStream getXlsStream() {
        return xlsStream;
    }

    public void setXlsStream(InputStream xlsStream) {
        this.xlsStream = xlsStream;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }
}
