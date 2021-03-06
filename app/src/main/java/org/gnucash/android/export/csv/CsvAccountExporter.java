/*
 * Copyright (c) 2018 Semyannikov Gleb <nightdevgame@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gnucash.android.export.csv;

import android.database.sqlite.SQLiteDatabase;
import com.crashlytics.android.Crashlytics;
import org.gnucash.android.export.ExportParams;
import org.gnucash.android.export.Exporter;
import org.gnucash.android.model.Account;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a GnuCash CSV account representation of the accounts and transactions
 *
 * @author Semyannikov Gleb <nightdevgame@gmail.com>
 */
public class CsvAccountExporter extends Exporter{
    private char mCsvSeparator;

    /**
     * Construct a new exporter with export parameters
     * @param params Parameters for the export
     */
    public CsvAccountExporter(ExportParams params) {
        super(params, null);
        mCsvSeparator = params.getCsvSeparator();
        LOG_TAG = "GncXmlExporter";
    }

    /**
     * Overloaded constructor.
     * Creates an exporter with an already open database instance.
     * @param params Parameters for the export
     * @param db SQLite database
     */
    public CsvAccountExporter(ExportParams params, SQLiteDatabase db) {
        super(params, db);
        mCsvSeparator = params.getCsvSeparator();
        LOG_TAG = "GncXmlExporter";
    }

    @Override
    public List<String> generateExport() throws ExporterException {
        OutputStreamWriter writerStream = null;
        CsvWriter writer = null;
        String outputFile = getExportCacheFilePath();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            writerStream = new OutputStreamWriter(bufferedOutputStream);
            writer = new CsvWriter(writerStream);
            generateExport(writer);
        } catch (IOException ex){
            Crashlytics.log("Error exporting CSV");
            Crashlytics.logException(ex);
        } finally {
            if (writerStream != null) {
                try {
                    writerStream.close();
                } catch (IOException e) {
                    throw new ExporterException(mExportParams, e);
                }
            }
        }

        List<String> exportedFiles = new ArrayList<>();
        exportedFiles.add(outputFile);

        return exportedFiles;
    }

    public void generateExport(final CsvWriter writer) throws ExporterException {
        try {
            String separator = mCsvSeparator + "";
            List<String> names = new ArrayList<String>();
            names.add("type");
            names.add("full_name");
            names.add("name");
            names.add("code");
            names.add("description");
            names.add("color");
            names.add("notes");
            names.add("commoditym");
            names.add("commodityn");
            names.add("hidden");
            names.add("tax");
            names.add("place_holder");

            List<Account> accounts = mAccountsDbAdapter.getAllRecords();

            for(int i = 0; i < names.size(); i++) {
                writer.write(names.get(i) + separator);
            }
            writer.write("\n");
            for(int i = 0; i < accounts.size(); i++) {
                Account account = accounts.get(i);

                writer.write(account.getAccountType().toString() + separator);
                writer.write(account.getFullName() + separator);
                writer.write(account.getName() + separator);

                //Code
                writer.write(separator);

                writer.write(account.getDescription() + separator);
                writer.write(account.getColor() + separator);

                //Notes
                writer.write(separator);

                writer.write(account.getCommodity().getCurrencyCode() + separator);
                writer.write("CURRENCY" + separator);
                writer.write(account.isHidden()?"T":"F" + separator);

                writer.write("F" + separator);

                writer.write(account.isPlaceholderAccount()?"T":"F" + separator);

                writer.write("\n");
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            throw new ExporterException(mExportParams, e);
        }
    }
}
