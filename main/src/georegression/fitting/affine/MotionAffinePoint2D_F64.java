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

package georegression.fitting.affine;

import georegression.fitting.MotionTransformPoint;
import georegression.struct.affine.Affine2D_F64;
import georegression.struct.point.Point2D_F64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.linsol.LinearSolver;

import java.util.List;


/**
 * Finds the best fit model parameters in the least squares sense which can describe the transform
 * from the 'fromPts' list to the 'toPts' list.
 *
 * @author Peter Abeles
 */
public class MotionAffinePoint2D_F64 implements MotionTransformPoint<Affine2D_F64, Point2D_F64> {

	private LinearSolver<DenseMatrix64F> solver;
	private DenseMatrix64F A;
	protected DenseMatrix64F x;
	private DenseMatrix64F y;

	Affine2D_F64 model = new Affine2D_F64();

	public MotionAffinePoint2D_F64() {
		solver = LinearSolverFactory.leastSquares(100, 2);
		x = new DenseMatrix64F( 3, 2 );
		A = new DenseMatrix64F( 0, 3 );
		y = new DenseMatrix64F( 0, 2 );
	}

	@Override
	public Affine2D_F64 getTransformSrcToDst() {
		return model;
	}

	@Override
	public boolean process( List<Point2D_F64> srcPts, List<Point2D_F64> dstPts) {
		// grow or shrink the matrix sizes
		int N = srcPts.size();

		if( N != dstPts.size() ) {
			throw new IllegalArgumentException( "From and to lists must be the same size" );
		} else if( N < 3 ) {
			throw new IllegalArgumentException( "Must be at least 3 points" );
		}

		if( A.data.length < N * 3 ) {
			A.reshape( N, 3, true );
			y.reshape( N, 2, true );
			for( int i = 0; i < N; i++ ) {
				A.set( i, 2, 1 );
			}
		} else {
			A.reshape( N, 3, false );
			y.reshape( N, 2, false );
		}

		// put the data into the matrices
		for( int i = 0; i < N; i++ ) {
			Point2D_F64 pt2 = srcPts.get( i );
			Point2D_F64 pt1 = dstPts.get( i );


			A.set( i, 0, pt2.x );
			A.set( i, 1, pt2.y );

			y.set( i, 0, pt1.x );
			y.set( i, 1, pt1.y );
		}

		// decompose A
		if( !solver.setA( A ) )
			return false;

		// solve
		solver.solve( y, x );

		// write it into the model
		model.a11 = (double) x.data[0];
		model.a12 = (double) x.data[2];
		model.tx = (double) x.data[4];
		model.a21 = (double) x.data[1];
		model.a22 = (double) x.data[3];
		model.ty = (double) x.data[5];

		return true;
	}

	@Override
	public int getMinimumPoints() {
		return 3;
	}
}
