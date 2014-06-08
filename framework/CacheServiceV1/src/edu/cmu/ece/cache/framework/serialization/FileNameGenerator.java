/*
   Copyright 2010 Shahriyar Amini

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package edu.cmu.ece.cache.framework.serialization;

import edu.cmu.ece.cache.framework.constants.Constants;

public class FileNameGenerator {
	public static String fileName(String appName, int rowId, int level, int overlay) {
		return new StringBuilder().append(appName)
			.append(Constants.UNDERSCORE_SEPARATOR)
			.append(rowId)
			.append(Constants.UNDERSCORE_SEPARATOR)
			.append(level)
			.append(Constants.UNDERSCORE_SEPARATOR)
			.append(overlay)
			.toString();
	}
}
