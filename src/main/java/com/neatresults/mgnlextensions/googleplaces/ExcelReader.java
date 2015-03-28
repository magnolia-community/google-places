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
package com.neatresults.mgnlextensions.googleplaces;

import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads excel file.
 */
public class ExcelReader {

    private static final Logger log = LoggerFactory.getLogger(ExcelReader.class);
    private final Map<String, String> columnToPropertyMapping;

    private Workbook workbook;
    private Sheet sheet;
    private Header header;
    int rowsCount;

    public ExcelReader(InputStream inputStream, Map<String, String> columnToPropertyMapping) throws IOException, InvalidFormatException {
        this.columnToPropertyMapping = columnToPropertyMapping;
        try {
            workbook = WorkbookFactory.create(inputStream);
            sheet = workbook.getSheetAt(0);
            header = sheet.getHeader();
            rowsCount = sheet.getLastRowNum(); // 65535
        } finally {
            inputStream.close();
        }
    }

    public List<Map<String, Object>> getRows(int startRowNumber, int endRowNumber) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        if (getRowsCount() == 0) {
            return result;
        }
        int count = 0;
        endRowNumber = endRowNumber == 0 ? getRowsCount() : endRowNumber;
        for (int i = 0; i < endRowNumber; i++) {
            Map<String, Object> values = new LinkedHashMap<String, Object>();
            Row valueRow = sheet.getRow(i);
            final int colCounts = valueRow.getLastCellNum();
            StringBuilder all = new StringBuilder();
            for (int j = 0; j < colCounts; j++) {
                if (columnToPropertyMapping.containsKey(String.valueOf(j))) {
                    final Cell valueCell = valueRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                    Object val = getValueFromCell(valueCell);
                    all.append(val);
                    values.put(columnToPropertyMapping.get(String.valueOf(j)), val);
                }
            }
            if (!StringUtils.isBlank(all.toString())) {
                if (count >= startRowNumber) {
                    result.add(values);
                }
                count++;
            }
        }
        return result;
    }

    /**
     * Get the actual row count by checking whether cell are empty or not.
     * {@link org.apache.poi.ss.usermodel.Sheet#getLastRowNum()} will always
     * return <code>65535</code>.
     *
     * @return The actual row count
     */
    public int getRowsCount() {
        int nonEmptyRowCount = 0;
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                    if (cell.getCellType() != Cell.CELL_TYPE_STRING || cell.getStringCellValue().length() > 0) {
                        nonEmptyRowCount++;
                        break;
                    }
                }
            }
        }
        return nonEmptyRowCount;
    }

    /**
     * Maps Excel cell types to java types.
     *
     * @see org.apache.poi.ss.usermodel.Cell#CELL_TYPE_BLANK
     * @see org.apache.poi.ss.usermodel.Cell#CELL_TYPE_NUMERIC
     * @see org.apache.poi.ss.usermodel.Cell#CELL_TYPE_STRING
     * @see org.apache.poi.ss.usermodel.Cell#CELL_TYPE_FORMULA
     * @see org.apache.poi.ss.usermodel.Cell#CELL_TYPE_BOOLEAN
     * @see org.apache.poi.ss.usermodel.Cell#CELL_TYPE_ERROR
     */
    public Class<?> getClassFromCellType(final Cell cell, final int type) {
        switch (type) {
        case CELL_TYPE_NUMERIC:
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                return Date.class;
            }
            return Double.class;
        case CELL_TYPE_STRING:
            return String.class;
        case CELL_TYPE_BOOLEAN:
            return Boolean.class;
        case CELL_TYPE_FORMULA:
            return getClassFromCellType(cell, cell.getCachedFormulaResultType());
        default:
            return String.class;
        }
    }

    public Class<?> getClassFromCellType(final Cell cell) {
        return getClassFromCellType(cell, cell.getCellType());
    }

    /**
     * Returns the correctly typed value from a cell by identifying the
     * type of cell contents.
     *
     * @param cell The cell
     * @param type The cell type
     * @return The cell value
     */
    public Object getValueFromCell(final Cell cell, final int type) {
        switch (type) {
        case CELL_TYPE_NUMERIC:
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            }
            return cell.getNumericCellValue();
        case CELL_TYPE_STRING:
            return cell.getStringCellValue();
        case CELL_TYPE_BOOLEAN:
            return cell.getBooleanCellValue();
        case CELL_TYPE_FORMULA:
            return getValueFromCell(cell, cell.getCachedFormulaResultType());
        default:
            return cell.getStringCellValue();
        }
    }

    public Object getValueFromCell(final Cell cell) {
        return getValueFromCell(cell, cell.getCellType());
    }

}
