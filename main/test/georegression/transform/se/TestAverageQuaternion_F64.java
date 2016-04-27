/*
 * Copyright (C) 2011-2015, Peter Abeles. All Rights Reserved.
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

package georegression.transform.se;

import georegression.geometry.ConvertRotation3D_F64;
import georegression.misc.GrlConstants;
import georegression.struct.EulerType;
import georegression.struct.so.Quaternion_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestAverageQuaternion_F64 {

	Random rand = new Random(234);

	/**
	 * Find the average of one quaternion.  Which should be the same as the input quaternion.
	 */
	@Test
	public void one() {
		Quaternion_F64 q = ConvertRotation3D_F64.eulerToQuaternion(EulerType.XYZ,0.1,-0.5,1.5,null);

		List<Quaternion_F64> list = new ArrayList<Quaternion_F64>();
		list.add(q);

		AverageQuaternion_F64 alg = new AverageQuaternion_F64();
		Quaternion_F64 found = new Quaternion_F64();

		assertTrue( alg.process(list,found) );

		checkEquals(q,found, GrlConstants.DOUBLE_TEST_TOL);
	}

	@Test
	public void two_same() {
		Quaternion_F64 q = ConvertRotation3D_F64.eulerToQuaternion(EulerType.XYZ,0.1,-0.5,1.5,null);

		List<Quaternion_F64> list = new ArrayList<Quaternion_F64>();
		list.add(q);
		list.add(q);

		AverageQuaternion_F64 alg = new AverageQuaternion_F64();
		Quaternion_F64 found = new Quaternion_F64();

		assertTrue( alg.process(list,found) );

		checkEquals(q,found, GrlConstants.DOUBLE_TEST_TOL);
	}

	/**
	 * Generate a bunch of quaternions, but noise them up on one axis and see if the result is close to the expected.
	 */
	@Test
	public void noiseOnOneAxis() {

		double rotX = 0.1;
		double rotY = -0.5;
		double rotZ = 1.5;

		List<Quaternion_F64> list = new ArrayList<Quaternion_F64>();
		for (int i = 0; i < 40; i++) {
			double noise = rand.nextGaussian()*0.03;
			list.add( ConvertRotation3D_F64.eulerToQuaternion(EulerType.XYZ,rotX,rotY+noise,rotZ,null));
		}
		Quaternion_F64 expected = ConvertRotation3D_F64.eulerToQuaternion(EulerType.XYZ,rotX,rotY,rotZ,null);

		AverageQuaternion_F64 alg = new AverageQuaternion_F64();
		Quaternion_F64 found = new Quaternion_F64();

		assertTrue( alg.process(list,found) );

		checkEquals(expected,found, Math.sqrt(GrlConstants.DOUBLE_TEST_TOL));
	}

	/**
	 * Sees if two quaternions are equal up to a sign ambiguity
	 */
	public static void checkEquals( Quaternion_F64 expected , Quaternion_F64 found , double errorTol ) {
		double sumAdd = 0;
		sumAdd += expected.w + found.w;
		sumAdd += expected.x + found.x;
		sumAdd += expected.y + found.y;
		sumAdd += expected.z + found.z;
		sumAdd = Math.abs(sumAdd);

		double sumMinus = 0;
		sumMinus += expected.w - found.w;
		sumMinus += expected.x - found.x;
		sumMinus += expected.y - found.y;
		sumMinus += expected.z - found.z;
		sumMinus = Math.abs(sumMinus);

		if( sumAdd < sumMinus ) {
			assertEquals( 0 , sumAdd/4   , errorTol );
		} else {
			assertEquals( 0 , sumMinus/4 , errorTol );
		}
	}
}
