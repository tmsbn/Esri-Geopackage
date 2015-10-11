/*******************************************************************************
 * Copyright 2015 Esri
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package gistec.geopackage.util;

import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities that don't belong anywhere else.
 */
public class Utilities {

    /**
     * Private constructor because Utilities isn't intended to be instantiated.
     */
    private Utilities() {}

    /**
     * Copies an asset to a directory.
     * @param assets the AssetManager containing the asset to be copied.
     * @param assetName the name of the asset file to copy to the directory. This may include a directory
     *                  before the filename.
     * @param destDir the directory to which the asset should be copied.
     * @param overwrite if true, this method overwrites an existing file of the same name already in
     *                  destDir. If false, this method does not overwrite an existing file.
     */
    public static void copyAsset(AssetManager assets, String assetName, File destDir, boolean overwrite) throws IOException {
        InputStream in = null;
        FileOutputStream out = null;

        try {
            in = assets.open(assetName);
            File outFile = new File(destDir, assetName);
            if (overwrite && outFile.exists()) {
                outFile.delete();
            }
            if (overwrite || !outFile.exists()) {
                out = new FileOutputStream(outFile);
                byte[] buf = new byte[8192];
                int len;
                while (0 <= (len = in.read(buf))) {
                    out.write(buf, 0, len);
                }
            }
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Throwable t) {}
            }
            if (null != out) {
                try {
                    out.close();
                } catch (Throwable t) {}
            }
        }
    }

}
