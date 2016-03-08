/*
 *   __               .__       .__  ._____.
 * _/  |_  _______  __|__| ____ |  | |__\_ |__   ______
 * \   __\/  _ \  \/  /  |/ ___\|  | |  || __ \ /  ___/
 *  |  | (  <_> >    <|  \  \___|  |_|  || \_\ \\___ \
 *  |__|  \____/__/\_ \__|\___  >____/__||___  /____  >
 *                   \/       \/             \/     \/
 *
 * Copyright (c) 2006-2011 Karsten Schmidt
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * http://creativecommons.org/licenses/LGPL/2.1/
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */

package toxi.volume;

import java.util.logging.Logger;

import toxi.geom.AABB;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Mesh3D;
import toxi.geom.mesh.TriangleMesh;

/**
 * IsoSurface class based on C version by Paul Bourke and Lingo version by
 * myself.
 */
public class ArrayIsoBoxes implements IsoSurface {

    protected static final Logger logger = Logger
            .getLogger(ArrayIsoSurface.class.getName());

    protected Vec3D cellSize;
    protected Vec3D centreOffset;
    protected VolumetricSpace volume;

    public float isoValue;

    protected int resX, resY, resZ;
    protected int resX1, resY1, resZ1;

    protected int sliceRes;
    protected int nextXY;

    protected Vec3D[] edgeVertices;

    public ArrayIsoBoxes(VolumetricSpace volume) {
        this.volume = volume;
        cellSize = new Vec3D(volume.scale.x / volume.resX1,
                volume.scale.y / volume.resY1, volume.scale.z / volume.resZ1);

        resX = volume.resX;
        resY = volume.resY;
        resZ = volume.resZ;
        resX1 = volume.resX1;
        resY1 = volume.resY1;
        resZ1 = volume.resZ1;

        sliceRes = volume.sliceRes;
        nextXY = resX + sliceRes;

        centreOffset = volume.halfScale.getInverted();

        edgeVertices = new Vec3D[3 * volume.numCells];
    }

    /**
     * Computes the surface mesh for the given volumetric data and iso value.
     */
    public Mesh3D computeSurfaceMesh(Mesh3D mesh, final float iso) {
        if (mesh == null) {
            mesh = new TriangleMesh("isosurface-" + iso);
        } else {
            mesh.clear();
        }
        isoValue = iso;
        float offsetZ = centreOffset.z;
        for (int z = 0; z < resZ1; z++) {
            int sliceOffset = sliceRes * z;
            float offsetY = centreOffset.y;
            for (int y = 0; y < resY1; y++) {
                float offsetX = centreOffset.x;
                int offset = resX * y + sliceOffset;
                for (int x = 0; x < resX1; x++) {
                    float offsetData = volume.getVoxelAt(offset);
                    if (offsetData > isoValue) {
                        AABB boxer = new AABB(
                                new Vec3D(offsetX, offsetY, offsetZ),
                                volume.voxelSize);
                        mesh.addMesh(boxer.toMesh());
                    }
                    offsetX += cellSize.x;
                    offset++;
                }
                offsetY += cellSize.y;
            }
            offsetZ += cellSize.z;
        }
        return mesh;
    }

    /**
     * Resets mesh vertices to default positions and clears face index. Needs to
     * be called inbetween successive calls to
     * {@link #computeSurfaceMesh(Mesh3D, float)}
     */
    public void reset() {
        for (int i = 0; i < edgeVertices.length; i++) {
            edgeVertices[i] = null;
        }
    }
}
