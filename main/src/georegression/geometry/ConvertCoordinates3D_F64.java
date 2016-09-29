/*
 * Copyright (C) 2011-2016, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Geometric Regression Library (GeoRegression).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package georegression.geometry;

import georegression.struct.point.Vector3D_F64;

/**
 * Contains code for converting between different 3D coordinate systems
 *
 * @author Peter Abeles
 */
public class ConvertCoordinates3D_F64 {

	/**
	 * Converts latitude and longitude coordinates into a unit vector
	 *
	 * @param lat Radian value from -pi/2 to pi/2
	 * @param lon Radian value from pi to -pi
	 * @param vector (output) Storage for unit vector.  If null is passed in a new instance will be created.
	 * @return Unit vector
	 */
	public static Vector3D_F64 latlonToUnitVector( double lat , double lon , Vector3D_F64 vector ) {
		if( vector == null )
			vector = new Vector3D_F64();

		vector.x = Math.cos(lat) * Math.cos(lon);
		vector.y = Math.cos(lat) * Math.sin(lon);
		vector.z = -Math.sin(lat);

		return vector;
	}
}