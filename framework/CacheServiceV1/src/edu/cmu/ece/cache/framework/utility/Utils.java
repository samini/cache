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

package edu.cmu.ece.cache.framework.utility;

import edu.cmu.ece.cache.framework.constants.Constants;

public class Utils {
	
	// returns -1 if invalid
	public static int binarySearchInBetween(double[] dArray, double value, int low, int high)
	{
		if (high < low)
			return -1;
		int mid = low + ((high - low) / 2);
		if (dArray[mid] > value)
		{
			if (mid != 0 && dArray[mid - 1] < value)
			{
				return (mid - 1);
			}
			else
			{
				return binarySearchInBetween(dArray, value, low, mid - 1);
			}
		}
		else if (dArray[mid] < value)
		{
			if (mid != dArray.length - 1 && dArray[mid + 1] > value)
			{
				return mid;
			}
			else
			{
				return binarySearchInBetween(dArray, value, mid + 1, high);
			}
		}
		else
		{
			// the value is equal to the value of dArray[mid]
			return mid;
		}
	}
	
	// extended to include '.'
	public static boolean isNumeric(char c)
	{
		if (c >= '0' && c <= '9')
			return true;
		else if (c == '.')
			return true;
		else if (c == '-')
			return true;
		else
			return false;
	}
	
	// reads how many consecutive numerical characters there are in
	// string S starting at index start
	public static int numNumerics(String s, int start)
	{
		int ret = 0;
		
		int index = start;
		
		while(index < s.length() && isNumeric(s.charAt(index)))
		{
			ret++;
			index++;
		}
		
		return ret;
	}
	
	
}
