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

package georegression.transform.twist;

import georegression.geometry.ConvertRotation3D_F32;
import georegression.geometry.GeometryMath_F32;
import georegression.misc.GrlConstants;
import georegression.struct.point.Vector3D_F32;
import georegression.struct.se.Se3_F32;
import georegression.struct.so.Rodrigues_F32;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;

/**
 * Operations related to twists.
 *
 * @author Peter Abeles
 */
public class TwistOps_F32 {

	/**
	 * Converts the {@link Se3_F32} object into homogenous notation.<br>
	 *
	 * [ R , T; 0 , 1]
	 *
	 * @param transform transform in regular SE3 format.  If null a new matrix will be declared
	 * @param H (Optional) Storage for homogenous 4x4 matrix.  If null a new matrix is declared.
	 * @return Homogenous matrix
	 */
	public static DenseMatrix64F homogenous( Se3_F32 transform , DenseMatrix64F H ) {
		if( H == null ) {
			H = new DenseMatrix64F(4,4);
		} else {
			H.reshape(4,4);
		}
		CommonOps.insert(transform.R,H,0,0);
		H.data[3] = transform.T.x;
		H.data[7] = transform.T.y;
		H.data[11] = transform.T.z;

		H.data[12] =0;H.data[13] =0;H.data[14] =0;H.data[15] =1;

		return H;
	}

	/**
	 * Converts the twist coordinate into homogenous format.<br>
	 * H = [hat(w), v; 0 , 0]
	 *
	 * @param twist Twist coordinate
	 * @param H (Optional) Storage for homogenous 4x4 matrix.  If null a new matrix is declared.
	 * @return Homogenous matrix
	 */
	public static DenseMatrix64F homogenous( TwistCoordinate_F32 twist , DenseMatrix64F H ) {
		if( H == null ) {
			H = new DenseMatrix64F(4,4);
		} else {
			H.reshape(4,4);
			H.data[12] = 0; H.data[13] = 0; H.data[14] = 0; H.data[15] = 0;
		}

		H.data[0] =  0;         H.data[1] = -twist.w.z; H.data[2]  =  twist.w.y; H.data[3]  = twist.v.x;
		H.data[4] =  twist.w.z; H.data[5] = 0;          H.data[6]  = -twist.w.x; H.data[7]  = twist.v.y;
		H.data[8] = -twist.w.y; H.data[9] =  twist.w.x; H.data[10] =  0;         H.data[11] = twist.v.z;

		return H;
	}

	/**
	 * <p>
	 *     Computes the exponential map for a twist:<br>
	 *     exp(hat(xi)*theta) = [ SO , (I-SO)*(w cross v) + w*w<sup>T</sup>v&Theta; 0 , 1]<br>
	 *     SO = exp(hat(w)*theta)
	 * </p>
	 *
	 * See page 42 in R. Murray, et. al. "A Mathematical Introduction to ROBOTIC MANIPULATION" 1994
	 *
	 * @param twist Twist coordinate
	 * @param theta Magnitude of rotation
	 * @param motion Storage for SE(3).  If null a new instance will be returned.
	 * @return The transformation.
	 */
	public static Se3_F32 exponential(TwistCoordinate_F32 twist , float theta , Se3_F32 motion ) {
		if( motion == null ) {
			motion = new Se3_F32();
		}

		float w_norm = twist.w.norm();

		if( w_norm == 0.0f ) {
			CommonOps.setIdentity(motion.R);
			motion.T.x = twist.v.x*theta;
			motion.T.y = twist.v.y*theta;
			motion.T.z = twist.v.z*theta;
			return motion;
		}

		DenseMatrix64F R = motion.getR();

		// First handle the SO region.  This Rodrigues equation
		float wx = twist.w.x/w_norm, wy = twist.w.y/w_norm, wz = twist.w.z/w_norm;

		ConvertRotation3D_F32.rodriguesToMatrix(wx,wy,wz, theta*w_norm, R);

		theta *= w_norm;

		// Now compute the translational component
		// (I - SO)*(w cross v) + w*w'*v*theta
		float vx = twist.v.x, vy = twist.v.y, vz = twist.v.z;

		float wv_x = wy*vz - wz*vy;
		float wv_y = wz*vx - wx*vz;
		float wv_z = wx*vy - wy*vx;

		/**/double left_x = (1 - R.data[0])*wv_x -      R.data[1]*wv_y  -      R.data[2]*wv_z;
		/**/double left_y =     -R.data[3]*wv_x  + (1 - R.data[4])*wv_y -      R.data[5]*wv_z;
		/**/double left_z =     -R.data[6]*wv_x  -      R.data[7]*wv_y  + (1 - R.data[8])*wv_z;

		float right_x = (wx*wx*vx + wx*wy*vy + wx*wz*vz)*theta;
		float right_y = (wy*wx*vx + wy*wy*vy + wy*wz*vz)*theta;
		float right_z = (wz*wx*vx + wz*wy*vy + wz*wz*vz)*theta;

		motion.T.x = (float)left_x + right_x;
		motion.T.y = (float)left_y + right_y;
		motion.T.z = (float)left_z + right_z;
		motion.T.divide(w_norm);

		return motion;
	}

	/**
	 * Converts a rigid body motion into a twist coordinate.  The value of theta used to generate the motion
	 * is assumed to be one.
	 *
	 * @param motion (Input) The SE(3) transformation
	 * @param twist (Output) Storage for twist.
	 * @return magnitude of the motion
	 */
	public static TwistCoordinate_F32 twist( Se3_F32 motion , TwistCoordinate_F32 twist ) {
		if( twist == null )
			twist = new TwistCoordinate_F32();

		if(MatrixFeatures.isIdentity(motion.R, GrlConstants.FLOAT_TEST_TOL)) {
			twist.w.set(0,0,0);
			twist.v.set(motion.T);
		} else {
			Rodrigues_F32 rod = new Rodrigues_F32();
			ConvertRotation3D_F32.matrixToRodrigues(motion.R,rod);

			twist.w.set(rod.unitAxisRotation);
			float theta = rod.theta;

			// A = (I-SO)*hat(w) + w*w'*theta
			DenseMatrix64F A = CommonOps.identity(3);
			CommonOps.subtract(A,motion.R, A);

			DenseMatrix64F w_hat = GeometryMath_F32.crossMatrix(twist.w,null);
			DenseMatrix64F tmp = A.copy();
			CommonOps.mult(tmp,w_hat,A);

			Vector3D_F32 w = twist.w;
			A.data[0] += w.x*w.x*theta; A.data[1] += w.x*w.y*theta; A.data[2] += w.x*w.z*theta;
			A.data[3] += w.y*w.x*theta; A.data[4] += w.y*w.y*theta; A.data[5] += w.y*w.z*theta;
			A.data[6] += w.z*w.x*theta; A.data[7] += w.z*w.y*theta; A.data[8] += w.z*w.z*theta;

			DenseMatrix64F y = new DenseMatrix64F(3,1);
			y.data[0] = motion.T.x;
			y.data[1] = motion.T.y;
			y.data[2] = motion.T.z;

			DenseMatrix64F x = new DenseMatrix64F(3,1);

			CommonOps.solve(A,y,x);

			twist.w.scale(rod.theta);
			twist.v.x = (float) x.data[0];
			twist.v.y = (float) x.data[1];
			twist.v.z = (float) x.data[2];
			twist.v.scale(rod.theta);
		}
		return twist;
	}
}
