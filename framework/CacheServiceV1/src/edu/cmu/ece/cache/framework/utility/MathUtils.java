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

public class MathUtils {
	public static double isoscelesHypotenuse(double side) {
		return (Constants.SQRT_TWO * side);
	}
	
	public static double overlappingCircleRadius(double side) {
		return isoscelesHypotenuse(side / 2.0);
	}
	
	public static double overlappingCircleRadius(double width, double height) {
		double m = Math.max(width, height);
		return isoscelesHypotenuse(m / 2.0);
	}
	
	// gives half of the diagonal of the square overlapping the circle with the input radius
	// just a wrapper over isoscelesHypotenuse
	public static double overlappingSquareHalfDiagonal(double radius) {
		return isoscelesHypotenuse(radius);
	}
}
