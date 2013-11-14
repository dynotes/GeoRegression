/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Geometric Regression Library (GeoRegression).
 *
 * GeoRegression is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * GeoRegression is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with GeoRegression.  If not, see <http://www.gnu.org/licenses/>.
 */

package georegression.fitting.plane;

import georegression.misc.GrlConstants;
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestFitPlane3D_F64 {

	Random rand = new Random(234);

	Vector3D_F64 axisX,axisY,axisZ;
	Point3D_F64 center;
	List<Point3D_F64> cloud;

	@Test
	public void svd() {
		createCloud();

		// compute the plane's equation
		Point3D_F64 foundCenter = new Point3D_F64();
		Vector3D_F64 foundNorm = new Vector3D_F64();

		FitPlane3D_F64 alg = new FitPlane3D_F64();

		alg.svd(cloud,foundCenter,foundNorm);

		// see if the found center is on the plane
		assertEquals(0,
				(foundCenter.x-center.x)*axisZ.x +
				(foundCenter.y-center.y)*axisZ.y +
				(foundCenter.z-center.z)*axisZ.z,
				GrlConstants.DOUBLE_TEST_TOL);

		// see if the found normal is valid
		foundNorm.normalize();
		double dot = foundNorm.dot(axisZ);
		assertEquals(0, Math.abs(dot) - 1, GrlConstants.DOUBLE_TEST_TOL);
	}

	@Test
	public void svdPoint() {
		createCloud();

		// compute the plane's equation
		Vector3D_F64 foundNorm = new Vector3D_F64();

		FitPlane3D_F64 alg = new FitPlane3D_F64();

		alg.svdPoint(cloud,cloud.get(10),foundNorm);

		// see if the found normal is valid
		foundNorm.normalize();
		double dot = foundNorm.dot(axisZ);
		assertEquals(0, Math.abs(dot) - 1, GrlConstants.DOUBLE_TEST_TOL);
	}

	private void createCloud() {
		// define a plane and its coordinate system
		axisX = new Vector3D_F64(1,2,3);
		axisY = new Vector3D_F64(3,-2,1);
		axisZ = axisX.cross(axisY);

		axisX.normalize();
		axisZ.normalize();
		axisY = axisX.cross(axisZ);

		center = new Point3D_F64(2,-1,0.5);

		// randomly generate points on the plane
		cloud = new ArrayList<Point3D_F64>();
		for( int i = 0; i < 100; i++ ) {
			double x = rand.nextGaussian()*5;
			double y = rand.nextGaussian()*5;

			Point3D_F64 p = new Point3D_F64();
			p.x = center.x + x*axisX.x + y*axisY.x;
			p.y = center.y + x*axisX.y + y*axisY.y;
			p.z = center.z + x*axisX.z + y*axisY.z;

			cloud.add(p);
		}
	}

}