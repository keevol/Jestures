/*******************************************************************************
 * Copyright (c) 2018 Giulianini Luca
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
 *******************************************************************************/

package jestures.sensor.kinect;

import java.io.IOException;
import java.util.stream.IntStream;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.Logger;

import edu.ufl.digitalworlds.j4k.J4KSDK;
import edu.ufl.digitalworlds.j4k.Skeleton;
import jestures.core.file.FileManager;
import jestures.sensor.Joint;

/**
 * An adapter tries to make our sensor suitable with the J4KSDK library.
 */
class KinectAdapterImpl extends J4KSDK implements KinectAdapter {
    private static final Logger LOG = Logger.getLogger(KinectAdapterImpl.class);
    static {
        FileManager.createKinectNativeFolderLib();
        try {
            FileManager.getAllUserFolder();
        } catch (final IOException e) {
            KinectAdapterImpl.LOG.error(e);
        }
    }
    private KinectAdapterObserver kinect;
    private boolean firstSkeletonPerson;
    private final Joint primaryJoint;
    private final KinectSensors kinectStartingSensors;
    private final KinectVersion kinectVersion;
    private final KinectSensibility kinectSensibility;

    /**
     * The @link{KinectAdapter.java} constructor.
     *
     * @param primaryJoint
     *            the main {@link Joint}
     * @param secondaryJoint
     *            the alternative {@link Joint}
     * @param kinectSensibility
     *            the Kinect sensibility.
     *            <p>
     *            Higher values sensibility produce higher value vectors along with minimum hand movement
     *
     */
    KinectAdapterImpl(final Joint primaryJoint, final KinectSensors kinectStartingSensors,
            final KinectVersion kinectVersion, final KinectSensibility kinectSensibility) {
        super(kinectVersion.getVersion());
        this.primaryJoint = primaryJoint;
        this.kinectStartingSensors = kinectStartingSensors;
        this.kinectVersion = kinectVersion;
        this.kinectSensibility = kinectSensibility;
        this.firstSkeletonPerson = true;
    }

    @Override
    public void onColorFrameEvent(final byte[] arg0) {
        // DO NOTHING
    }

    @Override
    public void onDepthFrameEvent(final short[] arg0, final byte[] arg1, final float[] arg2, final float[] arg3) { // NOPMD
        // DO NOTHING
    }

    @Override
    public void onSkeletonFrameEvent(final boolean[] skeletonTracked, final float[] jointPosition,
            final float[] jointOrientation, final byte[] jointStatus) {

        Skeleton skeleton;
        for (int i = 0; i < this.getMaxNumberOfSkeletons(); i++) {
            if (skeletonTracked[i] && this.firstSkeletonPerson) {
                this.firstSkeletonPerson = false;
                skeleton = Skeleton.getSkeleton(i, skeletonTracked, jointPosition, jointOrientation, jointStatus, this);
                Vector3D joint1;
                Vector3D joint2;
                final Vector3D acceleration;

                // JOINT WITHOUT FOOT
                switch (this.primaryJoint) {
                case RIGHT_HAND:
                    joint1 = new Vector3D(
                            (int) (skeleton.get3DJoint(Skeleton.HAND_RIGHT)[0] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_RIGHT)[1] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_RIGHT)[2] * this.kinectSensibility.getValue()));
                    joint2 = new Vector3D(
                            (int) (skeleton.get3DJoint(Skeleton.HAND_LEFT)[0] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_LEFT)[1] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_LEFT)[2] * this.kinectSensibility.getValue()));
                    break;
                case LEFT_HAND:
                    joint2 = new Vector3D(
                            (int) (skeleton.get3DJoint(Skeleton.HAND_RIGHT)[0] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_RIGHT)[1] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_RIGHT)[2] * this.kinectSensibility.getValue()));
                    joint1 = new Vector3D(
                            (int) (skeleton.get3DJoint(Skeleton.HAND_LEFT)[0] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_LEFT)[1] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_LEFT)[2] * this.kinectSensibility.getValue()));
                    break;
                default:
                    joint1 = new Vector3D(
                            (int) (skeleton.get3DJoint(Skeleton.HAND_RIGHT)[0] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_RIGHT)[1] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_RIGHT)[2] * this.kinectSensibility.getValue()));
                    joint2 = new Vector3D(
                            (int) (skeleton.get3DJoint(Skeleton.HAND_LEFT)[0] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_LEFT)[1] * this.kinectSensibility.getValue()),
                            (int) (skeleton.get3DJoint(Skeleton.HAND_LEFT)[2] * this.kinectSensibility.getValue()));
                    break;
                }
                // ACCLEROMETER
                final double[] doubleAcceleration = new double[3];
                final float[] floatAccleration = this.getAccelerometerReading();
                IntStream.range(0, floatAccleration.length)
                         .forEach(index -> doubleAcceleration[index] = floatAccleration[index]);
                acceleration = new Vector3D(doubleAcceleration);

                // NOTIFY
                this.kinect.notifyOnSkeletonChange(joint1, joint2);
                this.kinect.notifyOnAccelerometerChange(acceleration);
            }
        }
        this.firstSkeletonPerson = true;
    }

    @Override
    public void attacheKinectAdapterObserver(final KinectAdapterObserver kinect) {
        this.kinect = kinect;
    }

    @Override
    public void start() {
        this.start(this.kinectStartingSensors.getStartingSensors());
    }

    @Override
    public String printKinectInfo() {
        final String version = "Kinect Version: " + this.kinectVersion;
        final String sensors = "Kinect active sensors: " + this.kinectStartingSensors;
        return version + '\n' + sensors;
    }
}